package io.siddhi.distribution.event.simulator.core.exception.exception;

import io.siddhi.distribution.common.common.exception.ResourceNotFoundException;

/**
 * Simulation validation exception class.
 */
public class SimulationValidationException extends Exception {

    private String resourceName;
    private ResourceNotFoundException.ResourceType resourceType;

    /**
     * Throws customizes Simulator Initialization exception.
     *
     * @param message Error Message
     */
    public SimulationValidationException(String message) {
        super(message);
    }

    /**
     * Throws customizes Simulator Initialization exception.
     *
     * @param message Error Message
     * @param cause   Throwable which caused the Simulator Initialization exception
     */
    public SimulationValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SimulationValidationException(String message, ResourceNotFoundException.ResourceType resourceType,
                                         String resourceName) {
        super(message);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public SimulationValidationException(String message, ResourceNotFoundException.ResourceType resourceType,
                                         String resourceName,
                                         Throwable cause) {
        super(message, cause);
        this.resourceType = resourceType;
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public ResourceNotFoundException.ResourceType getResourceType() {
        return resourceType;
    }

}
