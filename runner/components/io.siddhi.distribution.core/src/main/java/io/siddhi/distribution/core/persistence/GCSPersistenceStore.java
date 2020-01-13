/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import io.siddhi.core.exception.CannotClearSiddhiAppStateException;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.exception.SiddhiAppRuntimeException;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.distribution.core.impl.utils.CompressionUtil;
import io.siddhi.distribution.core.persistence.util.PersistenceConstants;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Implementation of Persistence Store that would persist snapshots to the GCS.
 */
public class GCSPersistenceStore implements PersistenceStore {
    private static Logger log = Logger.getLogger(GCSPersistenceStore.class);
    private Storage storage;
    private String bucketName;
    private int numberOfRevisionsToSave;

    @Override
    public void save(String siddhiAppName, String revision, byte[] snapshot) {
        try {
            byte[] compressedSnapshot = CompressionUtil.compressGZIP(snapshot);
            BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, revision)).setContentType(
                    "application/octet-stream").build();
            Blob blob = storage.create(blobInfo, compressedSnapshot);
            if (blob != null && log.isDebugEnabled()) {
                log.debug("object has been uploaded to the bucket successfully." + blob);
            }
            cleanOldRevisions(siddhiAppName);
        } catch (IOException e) {
            log.error("Error occurred while trying to compress the snapshot. Failed to persist revision: " +
                    revision + " of Siddhi app: " + siddhiAppName);
        } catch (StorageException e) {
            log.error("Error occurred while saving the '" + revision + "' revision.", e);
        }
    }

    @Override
    public void setProperties(Map properties) {
        Map configurationMap = (Map) properties.get(PersistenceConstants.STATE_PERSISTENCE_CONFIGS);
        Object numberOfRevisionsObject = properties.get(PersistenceConstants.STATE_PERSISTENCE_REVISIONS_TO_KEEP);

        if (!(numberOfRevisionsObject instanceof Integer)) { //it also check if it's Null or not
            numberOfRevisionsToSave = PersistenceConstants.DEFAULT_REVISION_NO;
            log.warn("Number of revisions to keep is not set or invalid. Default value will be used.");
        } else {
            numberOfRevisionsToSave = Integer.parseInt(String.valueOf(numberOfRevisionsObject));
        }
        if (configurationMap == null) {
            throw new SiddhiAppCreationException("Please provide 'config' yaml entry under the 'statePersistence' " +
                    "entry.");
        }
        Object bucketNameObject;
        bucketNameObject = configurationMap.get(PersistenceConstants.BUCKET_NAME);
        if (!(bucketNameObject instanceof String)) {
            throw new SiddhiAppCreationException("'bucketName' should be provided in the configuration");
        }
        bucketName = String.valueOf(bucketNameObject);
        Object credentialPathObject;
        credentialPathObject = configurationMap.get(PersistenceConstants.CREDENTIAL_FILE_PATH);
        if (!(credentialPathObject instanceof String)) {
            log.info("Credential file is not provided. Hence trying to get the path from Environment variable.");
            try {
                storage = StorageOptions.getDefaultInstance().getService();
            } catch (StorageException e) {
                throw new SiddhiAppCreationException("Error occurred while creating the service.", e); //
            }
        } else {
            try {
                storage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials
                                .fromStream(new FileInputStream(
                                        new File(String.valueOf(credentialPathObject))))).build().getService();
            } catch (IOException e) {
                throw new SiddhiAppCreationException("Given credential file path is invalid.");
            } catch (StorageException e) {
                throw new SiddhiAppCreationException("Error occurred while creating the service.", e);
            }
        }
        try { //validate the bucket name and bucket ACL
            storage.get(bucketName, Storage.BucketGetOption.fields(Storage.BucketField.NAME));
        } catch (StorageException e) {
            throw new SiddhiAppCreationException("Error occurred while validating the '" + bucketName + "' bucket.", e);
        }
    }

    @Override
    public byte[] load(String siddhiAppName, String revision) {
        try {
            Blob blob = storage.get(this.bucketName, revision);
            if (blob != null) {
                byte[] bytes = blob.getContent();
                return CompressionUtil.decompressGZIP(bytes);
            }
            log.error("Unable to load the revision '" + revision + "' of SiddhiApp: '" + siddhiAppName + "'" +
                    "from '" + bucketName + "' bucket.");
            return null;
        } catch (StorageException e) {
            throw new SiddhiAppRuntimeException("Unable to load the revision '" + revision + "' of SiddhiApp: '" +
                    siddhiAppName + "'" + "from '" + bucketName + "' bucket.", e);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while trying to decompress the snapshot. Failed to " +
                    "load revision: " + revision + " of Siddhi app: " + siddhiAppName, e);
        }
    }

    @Override
    public String getLastRevision(String siddhiAppName) {
        try {
            Page<Blob> list = storage.list(bucketName, Storage.BlobListOption.fields(Storage.BlobField.NAME));
            Iterator<Blob> blobIterator = list.getValues().iterator();
            Blob blobLast = null;
            while (blobIterator.hasNext()) {
                blobLast = blobIterator.next();
            }
            if (blobLast != null) {
                return blobLast.getName();
            }
        } catch (StorageException e) {
            log.error("SiddhiAppName: ' " + siddhiAppName + "', Error occurred while loading the last " +
                    "revision.", e);
        }
        return null;
    }

    @Override
    public void clearAllRevisions(String siddhiAppName) {
        try {
            Page<Blob> list = storage.list(bucketName, Storage.BlobListOption.fields(Storage.BlobField.NAME));
            for (Blob blob : list.iterateAll()) {
                if (!storage.delete(blob.getBlobId())) {
                    throw new CannotClearSiddhiAppStateException("'" + blob.getName() + "' persistence state was " +
                            "unable to delete in the '" + siddhiAppName + "' siddhi app.");
                }
            }
        } catch (StorageException e) {
            throw new CannotClearSiddhiAppStateException("Error occurred while deleting clearing objects in '" +
                    siddhiAppName + "' siddhi app. " + e.getMessage(), e);
        }
    }

    private void cleanOldRevisions(String siddhiAppName) {
        Page<Blob> list = storage.list(bucketName, Storage.BlobListOption.fields(Storage.BlobField.NAME));
        int count = 0;
        Queue<Blob> blobQueue = new LinkedList<>();
        for (Blob blob : list.iterateAll()) {
            blobQueue.add(blob);
            if (++count > numberOfRevisionsToSave) {
                BlobId blobId = blobQueue.poll().getBlobId();
                try {
                    if (!storage.delete(blobId)) {
                        log.error("Error occurred when deleting old revision: '" + blobId.getName() + "' in " +
                                "siddhi app: '" + siddhiAppName + "'.");
                    }
                } catch (StorageException e) {
                    log.error("Error occurred while deleting the '" + blobId.getName() + "' revision."
                            + e.getMessage(), e);
                }
            }
        }
    }
}
