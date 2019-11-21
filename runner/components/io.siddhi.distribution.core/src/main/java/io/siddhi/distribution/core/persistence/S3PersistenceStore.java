/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.distribution.core.persistence;

import io.siddhi.core.exception.CannotClearSiddhiAppStateException;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.distribution.core.impl.utils.CompressionUtil;
import io.siddhi.distribution.core.persistence.exception.S3PersistenceStoreException;
import io.siddhi.distribution.core.persistence.util.PersistenceConstants;
import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Persistence Store that would persist snapshots to the s3 bucket.
 */
public class S3PersistenceStore implements PersistenceStore {
    private static final Logger log = Logger.getLogger(S3PersistenceStore.class);
    private S3Client s3Client;
    private String bucketName;
    private int numberOfRevisionsToSave;

    @Override
    public void save(String siddhiAppName, String revision, byte[] snapshot) {
        byte[] compressedSnapshot;
        try {
            compressedSnapshot = CompressionUtil.compressGZIP(snapshot);
        } catch (IOException e) {
            log.error("Error occurred while trying to compress the snapshot. Failed to " +
                    "persist revision: " + revision + " of Siddhi app: " + siddhiAppName);
            return;
        }
        RequestBody requestBody = RequestBody.fromBytes(compressedSnapshot);
        PutObjectResponse putObjectResponse =
                s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(revision)
                        .build(), requestBody);
        cleanOldRevisions();
        if (log.isDebugEnabled()) {
            log.debug("object has been uploaded to the bucket successfully, ETag: " + putObjectResponse.eTag());
        }
    }

    @Override
    public void setProperties(Map properties) {
        Map configurationMap = (Map) properties.get(PersistenceConstants.STATE_PERSISTENCE_CONFIGS);
        Object numberOfRevisionsObject = properties.get(PersistenceConstants.STATE_PERSISTENCE_REVISIONS_TO_KEEP);
        String regionId;

        if (!(numberOfRevisionsObject instanceof Integer)) { //it also check if it's Null or not
            numberOfRevisionsToSave = PersistenceConstants.DEFAULT_REVISION_NO;
            log.warn("Number of revisions to keep is not set or invalid. Default value will be used.");
        } else {
            numberOfRevisionsToSave = Integer.parseInt(String.valueOf(numberOfRevisionsObject));
        }
        if (configurationMap != null) {
            Object regionObject = configurationMap.get(PersistenceConstants.REGION);
            if (!(regionObject instanceof String)) {
                regionId = PersistenceConstants.DEFAULT_REGION_ID;
                log.info("No region id provided, Hence setting the region to default region(us-west-2)");
            } else {
                regionId = String.valueOf(regionObject);
            }
            Region region = Region.of(regionId.toLowerCase());
            SdkHttpClient httpClient = ApacheHttpClient.builder().build();
            S3ClientBuilder clientBuilder = S3Client.builder()
                    .region(region);
            AwsCredentialsProvider credentialsProvider = getCredentialProvider(configurationMap);
            if (credentialsProvider != null) {
                clientBuilder.credentialsProvider(credentialsProvider);
            }
            s3Client = clientBuilder.httpClient(httpClient).build();
            List<Bucket> buckets;
            try {
                buckets = s3Client.listBuckets(ListBucketsRequest.builder().build()).buckets();
            } catch (SdkClientException e) {
                throw new S3PersistenceStoreException("The region id you provide is invalid, please provide a valid" +
                        " region id, given region id: '" + regionId + "'.", e);
            } catch (S3Exception e) {
                throw new S3PersistenceStoreException("An exception occurs while listing out the buckets.", e);
            }
            Object bucketNameObject = configurationMap.get(PersistenceConstants.BUCKET_NAME);
            if (!(bucketNameObject instanceof String)) {
                throw new S3PersistenceStoreException("'bucketName' should be provided in the configuration");
            } else {
                this.bucketName = String.valueOf(bucketNameObject);
                int i = Collections.binarySearch(buckets, Bucket.builder().name(this.bucketName).build(),
                        Comparator.comparing(Bucket::name));
                if (i < 0) {
                    throw new S3PersistenceStoreException("The bucket name you provided does not exist, please " +
                            "re-check the bucket name, given bucket name: " + bucketName);
                }
            }
        } else {
            throw new S3PersistenceStoreException("Please provide 'config' yaml entry under the 'statePersistence' " +
                    "entry.");
        }
    }

    @Override
    public byte[] load(String siddhiAppName, String revision) {
        try {
            byte[] bytes = s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(
                    revision).build(),
                    ResponseTransformer.toBytes()).asByteArray();
            log.info("State loaded for " + siddhiAppName + " revision " + revision + " from the s3 bucket.");
            byte[] decompressedSnapshot;
            try {
                decompressedSnapshot = CompressionUtil.decompressGZIP(bytes);
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while trying to decompress the snapshot. Failed to " +
                        "load revision: " + revision + " of Siddhi app: " + siddhiAppName, e);
            }
            return decompressedSnapshot;
        } catch (S3Exception e) {
            log.error("Cannot load the revision " + revision + " of SiddhiApp: " + siddhiAppName +
                    " from S3 bucket.", e);
        }
        return null;
    }

    @Override
    public String getLastRevision(String siddhiAppName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(request);
        if (!listObjectsV2Response.contents().isEmpty()) {
            return listObjectsV2Response.contents().get(listObjectsV2Response.contents().size() - 1).key();
        }
        return null;
    }

    @Override
    public void clearAllRevisions(String siddhiAppName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
        ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(request);
        List<S3Object> contents = listObjectsV2Response.contents();
        for (S3Object object : contents) {
            String key = object.key();
            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
            try {
                s3Client.deleteObject(deleteObjectRequest);
            } catch (S3Exception e) {
                throw new CannotClearSiddhiAppStateException("Persisted state with id :" + key + " cannot be deleted.");
            }
        }
    }

    private void cleanOldRevisions() {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
//        only listing out objects detail, not the object content
        List<S3Object> objectList = s3Client.listObjectsV2(request).contents();
        for (int i = 0; i < objectList.size() - numberOfRevisionsToSave; i++) {
            String key = objectList.get(i).key();
            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
            s3Client.deleteObject(deleteObjectRequest);
        }
    }

    private AwsCredentialsProvider getCredentialProvider(Map configurationMap) {
        Object credentialProviderClassName = configurationMap.get(PersistenceConstants.CREDENTIAL_PROVIDER_CLASS);
        if (!(credentialProviderClassName instanceof String)) {
            // only to give support to previous 'credentialProvideClass' property
            credentialProviderClassName = configurationMap.get(PersistenceConstants.CREDENTIAL_PROVIDER_CLASS_OLD);
        }
        if (credentialProviderClassName instanceof String) {
            if (log.isDebugEnabled()) {
                log.debug("Authenticating user via the credential provider class.");
            }
            String className = null;
            try {
                className = String.valueOf(credentialProviderClassName);
                Class credentialProviderClass = Class.forName(className);
                return (AwsCredentialsProvider) credentialProviderClass.getDeclaredMethod("create")
                        .invoke(credentialProviderClass);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new S3PersistenceStoreException("Error while authenticating the user. Please make sure you " +
                        "have given the access key and the secret key as mentioned in the aws documentation", e);
            } catch (ClassNotFoundException e) {
                throw new S3PersistenceStoreException("Unable to find the credential provider class " +
                        className + ", Please provide a valid credential class.", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Authenticating user via the access key and the secret key. ");
            }
            if (configurationMap.containsKey(PersistenceConstants.ACCESS_KEY) || configurationMap.containsKey(
                    PersistenceConstants.SECRET_KEY)) {
                Object accessKeyObject = configurationMap.get(PersistenceConstants.ACCESS_KEY);
                Object secretKeyObject = configurationMap.get(PersistenceConstants.SECRET_KEY);
                String accessKey = String.valueOf(accessKeyObject);
                String secretKey = String.valueOf(secretKeyObject);

                AwsSessionCredentials awsCreds = AwsSessionCredentials.create(
                        accessKey,
                        secretKey,
                        "");
                return StaticCredentialsProvider.create(awsCreds);
            }
            log.info("No credential provider class or keys are provided. Hence falling back to default credential" +
                    " provider chain.");
            return null;
        }
    }
}
