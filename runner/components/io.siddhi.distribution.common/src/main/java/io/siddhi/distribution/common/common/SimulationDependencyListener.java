package io.siddhi.distribution.common.common;

/**
 * SimulationDependencyListener interface is implemented by simulation config deployer to listen to
 * resource deployments.
 */
public interface SimulationDependencyListener {

    void onDeploy();

    void onUpdate();

    void onDelete();

}
