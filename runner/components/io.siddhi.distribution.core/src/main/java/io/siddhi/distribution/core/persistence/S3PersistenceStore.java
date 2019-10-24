package io.siddhi.distribution.core.persistence;

import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.distribution.core.impl.utils.CompressionUtil;
import io.siddhi.distribution.core.persistence.exception.S3ConfigurationException;
import io.siddhi.distribution.core.persistence.util.PersistenceConstants;
import org.apache.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Persistence Store that would persist snapshots to the s3 bucket.
 */
public class S3PersistenceStore implements PersistenceStore {
    private static final Logger log = Logger.getLogger(S3PersistenceStore.class);
    private S3Client s3;
    private String bucketName;
    private int numberOfRevisionsToSave;

    @Override
    public void save(String siddhiAppName, String revision, byte[] snapshot) {
        RequestBody requestBody = null;
        byte[] compressedSnapshot;
        try {
            compressedSnapshot = CompressionUtil.compressGZIP(snapshot);
        } catch (IOException e) {
            log.error("Error occurred while trying to compress the snapshot. Failed to " +
                    "persist revision: " + revision + " of Siddhi app: " + siddhiAppName);
            return;
        }
        requestBody = RequestBody.fromBytes(compressedSnapshot);
        PutObjectResponse putObjectResponse =
                s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(revision)
                        .build(), requestBody);
        cleanOldRevisions(siddhiAppName);
        log.debug(putObjectResponse.eTag());
    }

    @Override
    public void setProperties(Map properties) {
        Map configurationMap = (Map) properties.get(PersistenceConstants.STATE_PERSISTENCE_CONFIGS);
        Object numberOfRevisionsObject = properties.get(PersistenceConstants.STATE_PERSISTENCE_REVISIONS_TO_KEEP);
        String regionId;

        if (numberOfRevisionsObject == null || !(numberOfRevisionsObject instanceof Integer)) {
            numberOfRevisionsToSave = 3;
            if (log.isDebugEnabled()) {
                log.debug("Number of revisions to keep is not set or invalid. Default value will be used.");
            }
        } else {
            numberOfRevisionsToSave = Integer.parseInt(String.valueOf(numberOfRevisionsObject));
        }

        if (configurationMap != null) {
            if (!(configurationMap.containsKey(PersistenceConstants.ACCESS_KEY) || configurationMap.containsKey(
                    PersistenceConstants.SECRET_KEY))) {
                throw new SiddhiAppCreationException("The Access key and the Secret Key should be provided in the " +
                        "configuration file");
            }
            Object accessKeyObject = configurationMap.get(PersistenceConstants.ACCESS_KEY);
            String accessKey = String.valueOf(accessKeyObject);
            Object secretKeyObject = configurationMap.get(PersistenceConstants.SECRET_KEY);
            String secretKey = String.valueOf(secretKeyObject);
            Object regionObject = configurationMap.get(PersistenceConstants.REGION);
            if (regionObject == null || !(regionObject instanceof String)) {
                regionId = "us-west-2";
            } else {
                regionId = String.valueOf(regionObject);
            }
            Region region = getRegion(regionId);
            if (!DynamoDbClient.serviceMetadata().regions().contains(region)) {
                throw new S3ConfigurationException("Invalid region name provided, given region name: " +
                        regionId + ", Expected regions " + DynamoDbClient.serviceMetadata().regions());
            }
            AwsSessionCredentials awsCreds = AwsSessionCredentials.create(accessKey, secretKey, "");
            s3 = S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .region(region).build();
            List<Bucket> buckets;
            try {
                buckets = s3.listBuckets(ListBucketsRequest.builder().build()).buckets();
            } catch (S3Exception e) {
                throw new S3ConfigurationException("Invalid access key or secret key provided, access-key: " +
                        accessKey + ", secret-key: " + secretKey, e);
            }
            Object bucketNameObject = configurationMap.get(PersistenceConstants.BUCKET_NAME);
            if (bucketNameObject == null || !(bucketNameObject instanceof String)) {
                throw new SiddhiAppCreationException("'bucketName' should be provided in the configuration");
                /*this.bucketName = "siddhi-app-persistence-" + System.currentTimeMillis();
                CreateBucketRequest createBucketRequest = CreateBucketRequest
                        .builder()
                        .bucket(this.bucketName)
                        .createBucketConfiguration(CreateBucketConfiguration.builder()
                                .locationConstraint(region.id())
                                .build())
                        .build();
                s3.createBucket(createBucketRequest);
                log.info("S3 bucket name is not set. Created a bucket with default name: " + this.bucketName);*/
            } else {
                this.bucketName = String.valueOf(bucketNameObject);
                int i = Collections.binarySearch(buckets, Bucket.builder().name(this.bucketName).build(),
                        Comparator.comparing(Bucket::name));
                if (i < 0) {
                    throw new S3ConfigurationException("Invalid bucket name provided, given bucket name: " +
                            bucketName);
                }
            }
        }
    }

    @Override
    public byte[] load(String siddhiAppName, String revision) {
        try {
            byte[] bytes = s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(
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
        } catch (Exception e) { //todo Find out exceptions throws from s3
            log.error("Cannot load the revision " + revision + " of SiddhiApp: " + siddhiAppName +
                    " from S3 bucket.", e);
        }
        return new byte[0];
    }

    @Override
    public String getLastRevision(String siddhiAppName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
        ListObjectsV2Response listObjectsV2Response = s3.listObjectsV2(request);
        if (!listObjectsV2Response.contents().isEmpty()) {
            return listObjectsV2Response.contents().get(listObjectsV2Response.contents().size() - 1).key();
        }
        return null;
    }

    @Override
    public void clearAllRevisions(String siddhiAppName) {
        s3.deleteObjects(DeleteObjectsRequest.builder().bucket(bucketName).build());
    }

    private void cleanOldRevisions(String siddhiAppName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
        List<S3Object> objectList = s3.listObjectsV2(request).contents();
        if (!(objectList.isEmpty() || objectList.size() == 1)) {
            for (int i = 0; i < objectList.size() - numberOfRevisionsToSave; i++) {
                String key = objectList.get(i).key();
                DeleteObjectRequest deleteObjectRequest =
                        DeleteObjectRequest.builder().bucket(bucketName).key(key).build();
                s3.deleteObject(deleteObjectRequest);
            }
        }
    }

    private Region getRegion(String region) {
        switch (region) {
            case "aws-global": {
                return Region.AWS_GLOBAL;
            }
            case "aws-cn-global": {
                return Region.AWS_CN_GLOBAL;
            }
            case "aws-us-gov-global": {
                return Region.AWS_US_GOV_GLOBAL;
            }
            default: {
                return Region.of(region);
            }
        }

    }
}
