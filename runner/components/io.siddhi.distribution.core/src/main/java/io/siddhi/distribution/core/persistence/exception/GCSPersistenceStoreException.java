package io.siddhi.distribution.core.persistence.exception;

/**
 * Exception class for GCS persistence
 */
public class GCSPersistenceStoreException extends RuntimeException {

    public GCSPersistenceStoreException(String msg) {
        super(msg);
    }
    public GCSPersistenceStoreException(String msg, Throwable t) {
        super(msg, t);
    }
}
