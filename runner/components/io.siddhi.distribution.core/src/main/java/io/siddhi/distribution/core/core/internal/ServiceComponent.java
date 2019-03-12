/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.distribution.core.core.internal;

import io.siddhi.distribution.common.common.EventStreamService;
import io.siddhi.distribution.common.common.SiddhiAppRuntimeService;
import io.siddhi.distribution.common.common.utils.config.FileConfigManager;
import io.siddhi.distribution.core.core.DeploymentMode;
import io.siddhi.distribution.core.core.NodeInfo;
import io.siddhi.distribution.core.core.internal.util.SiddhiAppProcessorConstants;
import io.siddhi.distribution.core.core.persistence.PersistenceManager;
import io.siddhi.distribution.core.core.persistence.beans.PersistenceConfigurations;
import io.siddhi.distribution.core.core.persistence.exception.PersistenceStoreConfigurationException;
import io.siddhi.distribution.core.core.persistence.util.PersistenceConstants;
import io.siddhi.distribution.metrics.core.core.SiddhiMetricsFactory;
import io.siddhi.distribution.metrics.core.core.internal.service.MetricsServiceComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.PermissionManager;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.databridge.commons.ServerEventListener;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.StatisticsConfiguration;
import org.wso2.siddhi.core.util.SiddhiComponentActivator;
import org.wso2.siddhi.core.util.persistence.IncrementalPersistenceStore;
import org.wso2.siddhi.core.util.persistence.PersistenceStore;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 */
@Component(
        name = "siddhi-io-distribution-core-service",
        immediate = true
)
public class ServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(ServiceComponent.class);
    private ServiceRegistration streamServiceRegistration;
    private ServiceRegistration siddhiAppRuntimeServiceRegistration;
    private ScheduledFuture<?> scheduledFuture = null;
    private ScheduledExecutorService scheduledExecutorService = null;
    private boolean serviceComponentActivated;


    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.debug("Service Component is activated");

        String runningFileName = System.getProperty(SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_FILE);
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        // Create Stream Processor Service
        StreamProcessorDataHolder.setStreamProcessorService(new StreamProcessorService());
        SiddhiManager siddhiManager = new SiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
        siddhiManager.setConfigManager(fileConfigManager);
        PersistenceConfigurations persistenceConfigurations = configProvider.getConfigurationObject
                (PersistenceConfigurations.class);

        if (persistenceConfigurations != null && persistenceConfigurations.isEnabled()) {
            String persistenceStoreClassName = persistenceConfigurations.getPersistenceStore();
            try {
                if (Class.forName(persistenceStoreClassName).newInstance() instanceof PersistenceStore) {
                    PersistenceStore persistenceStore =
                            (PersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    persistenceStore.setProperties((Map) configProvider.getConfigurationObject(PersistenceConstants.
                            STATE_PERSISTENCE_NS));
                    siddhiManager.setPersistenceStore(persistenceStore);
                } else if (Class.forName(persistenceStoreClassName).newInstance()
                        instanceof IncrementalPersistenceStore) {
                    IncrementalPersistenceStore incrementalPersistenceStore =
                            (IncrementalPersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    incrementalPersistenceStore.setProperties(
                            (Map) configProvider.getConfigurationObject(PersistenceConstants.STATE_PERSISTENCE_NS));
                    siddhiManager.setIncrementalPersistenceStore(incrementalPersistenceStore);
                } else {
                    throw new PersistenceStoreConfigurationException("Persistence Store class with name "
                            + persistenceStoreClassName + " is invalid. The given class has to implement either " +
                            "org.wso2.siddhi.core.util.persistence.PersistenceStore or " +
                            "org.wso2.siddhi.core.util.persistence.IncrementalPersistenceStore.");
                }
                if (log.isDebugEnabled()) {
                    log.debug(persistenceStoreClassName + " chosen as persistence store");
                }

            } catch (ClassNotFoundException e) {
                throw new PersistenceStoreConfigurationException("Persistence Store class with name "
                        + persistenceStoreClassName + " is invalid. ", e);
            }


            int persistenceInterval = persistenceConfigurations.getIntervalInMin();
            scheduledExecutorService = Executors.newScheduledThreadPool(1);

            if (persistenceInterval > 0) {
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new PersistenceManager(),
                        persistenceInterval, persistenceInterval, TimeUnit.MINUTES);
            }
            StreamProcessorDataHolder.setIsPersistenceEnabled(true);
            log.info("Periodic state persistence started with an interval of " + String.valueOf(persistenceInterval) +
                    " using " + persistenceStoreClassName);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Periodic persistence is disabled");
            }
        }

        StatisticsConfiguration statisticsConfiguration = new StatisticsConfiguration(new SiddhiMetricsFactory());
        siddhiManager.setStatisticsConfiguration(statisticsConfiguration);
        StreamProcessorDataHolder.setSiddhiManager(siddhiManager);

        File runningFile;

        if (runningFileName != null) {
            StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.RUN_FILE);
            if (runningFileName.trim().equals("")) {
                // Can't Continue. We shouldn't be here. that means there is a bug in the startup script.
                log.error("Error: Can't get target file to run. System property {} is not set.",
                        SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_FILE);
                StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                return;
            }
            runningFile = new File(runningFileName);
            if (!runningFile.exists()) {
                log.error("Error: File " + runningFile.getName() + " not found in the given location.");
                StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                return;
            }
            try {
                StreamProcessorDeployer.deploySiddhiQLFile(runningFile);
            } catch (Exception e) {
                StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.ERROR);
                log.error(e.getMessage(), e);
                return;
            }
        } else {
            StreamProcessorDataHolder.getInstance().setRuntimeMode(SiddhiAppProcessorConstants.RuntimeMode.SERVER);
        }

        if (log.isDebugEnabled()) {
            log.debug("Runtime mode is set to : " + StreamProcessorDataHolder.getInstance().getRuntimeMode());
        }

        streamServiceRegistration = bundleContext.registerService(EventStreamService.class.getName(),
                new CarbonEventStreamService(), null);
        siddhiAppRuntimeServiceRegistration = bundleContext.registerService(SiddhiAppRuntimeService.class
                .getCanonicalName(), new CarbonSiddhiAppRuntimeService(), null);

        NodeInfo nodeInfo = new NodeInfo(DeploymentMode.SINGLE_NODE, configProvider.getConfigurationObject(
                CarbonConfiguration.class).getId());
        bundleContext.registerService(NodeInfo.class.getName(), nodeInfo, null);
        StreamProcessorDataHolder.setNodeInfo(nodeInfo);
        StreamProcessorDataHolder.getInstance().setBundleContext(bundleContext);

        serviceComponentActivated = true;
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("Service Component is deactivated");

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();
        for (SiddhiAppData siddhiAppData : siddhiAppMap.values()) {
            if (siddhiAppData.getSiddhiAppRuntime() != null) {
                siddhiAppData.getSiddhiAppRuntime().shutdown();
            }
        }

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }

        streamServiceRegistration.unregister();
        siddhiAppRuntimeServiceRegistration.unregister();
    }

    /**
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        StreamProcessorDataHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        StreamProcessorDataHolder.getInstance().setCarbonRuntime(null);
    }


    /**
     * This bind method will be called when Siddhi ComponentActivator OSGi service is registered.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    @Reference(
            name = "siddhi.component.activator.service",
            service = SiddhiComponentActivator.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiComponentActivator"
    )
    protected void setSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param siddhiComponentActivator The SiddhiComponentActivator instance registered by Siddhi OSGi service
     */
    protected void unsetSiddhiComponentActivator(SiddhiComponentActivator siddhiComponentActivator) {

    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        StreamProcessorDataHolder.getInstance().setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        StreamProcessorDataHolder.getInstance().setConfigProvider(null);
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceListener"
    )
    protected void registerDataSourceListener(DataSourceService dataSourceService) {
        StreamProcessorDataHolder.setDataSourceService(dataSourceService);

    }

    protected void unregisterDataSourceListener(DataSourceService dataSourceService) {
        StreamProcessorDataHolder.setDataSourceService(null);
    }

//    @Reference(
//            name = "io.siddhi.distribution.core.distribution.DistributionService",
//            service = DistributionService.class,
//            cardinality = ReferenceCardinality.MANDATORY,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unregisterDistributionService"
//    )
//    protected void registerDistributionService(DistributionService distributionService) {
//        StreamProcessorDataHolder.setDistributionService(distributionService);
//
//    }
//
//    protected void unregisterDistributionService(DistributionService distributionService) {
//        StreamProcessorDataHolder.setDistributionService(null);
//    }

    @Reference(
            name = "MetricsServiceComponent",
            service = MetricsServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterMetricsManager"
    )
    protected void registerMetricsManager(MetricsServiceComponent serviceComponent) {
        //do nothing
    }

    protected void unregisterMetricsManager(MetricsServiceComponent serviceComponent) {
        //do nothing
    }

    @Reference(
            name = "permission-manager",
            service = PermissionManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetPermissionManager"
    )
    protected void setPermissionManager(PermissionManager permissionManager) {
        StreamProcessorDataHolder.setPermissionProvider(permissionManager.getProvider());
    }

    protected void unsetPermissionManager(PermissionManager permissionManager) {
        StreamProcessorDataHolder.setPermissionProvider(null);
    }

    /**
     * Get the ServerEventListener service.
     * This is the bind method that gets called for ServerEventListener service
     * registration that satisfy the policy.
     *
     * @param serverEventListener the server listeners that is registered as a service.
     */
    @Reference(
            name = "org.wso2.carbon.databridge.commons.ServerEventListener",
            service = ServerEventListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServerListener"
    )
    protected void registerServerListener(ServerEventListener serverEventListener) {
        StreamProcessorDataHolder.setServerListener(serverEventListener);
        serverEventListener.start();
    }

    protected void unregisterServerListener(ServerEventListener serverEventListener) {
        StreamProcessorDataHolder.removeServerListener(serverEventListener);
    }
}
