package io.siddhi.distribution.core.persistence.exception;

/**
 * Exception class for S3-bucket configuration.
 */
public class S3ConfigurationException extends RuntimeException {

    public S3ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public S3ConfigurationException(String message) {
        super(message);
    }
}
