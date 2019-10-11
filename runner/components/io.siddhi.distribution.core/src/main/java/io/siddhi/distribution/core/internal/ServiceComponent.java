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
package io.siddhi.distribution.core.internal;

import io.siddhi.core.SiddhiManager;
import io.siddhi.core.config.StatisticsConfiguration;
import io.siddhi.core.exception.SiddhiAppCreationException;
import io.siddhi.core.util.SiddhiComponentActivator;
import io.siddhi.core.util.persistence.IncrementalPersistenceStore;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.distribution.common.common.EventStreamService;
import io.siddhi.distribution.common.common.SiddhiAppRuntimeService;
import io.siddhi.distribution.common.common.utils.config.FileConfigManager;
import io.siddhi.distribution.core.DeploymentMode;
import io.siddhi.distribution.core.NodeInfo;
import io.siddhi.distribution.core.internal.exception.SiddhiAppAlreadyExistException;
import io.siddhi.distribution.core.internal.util.SiddhiAppProcessorConstants;
import io.siddhi.distribution.core.persistence.PersistenceManager;
import io.siddhi.distribution.core.persistence.beans.PersistenceConfigurations;
import io.siddhi.distribution.core.persistence.exception.PersistenceStoreConfigurationException;
import io.siddhi.distribution.metrics.core.SiddhiMetricsFactory;
import io.siddhi.distribution.metrics.core.internal.service.MetricsServiceComponent;
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
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.siddhi.distribution.core.persistence.util.PersistenceConstants.STATE_PERSISTENCE_NS;
import static io.siddhi.distribution.core.persistence.util.PersistenceConstants.STREAMLINED_STATE_PERSISTENCE_NS;

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

        String siddhiAppsReference = System.getProperty(SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_SIDDHI_APPS);
        ConfigProvider configProvider = StreamProcessorDataHolder.getInstance().getConfigProvider();
        // Create Stream Processor Service
        StreamProcessorDataHolder.setStreamProcessorService(new StreamProcessorService());
        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
        FileConfigManager fileConfigManager = new FileConfigManager(configProvider);
        siddhiManager.setConfigManager(fileConfigManager);

        PersistenceConfigurations persistenceConfigurations;
        Map persistenceConfigurationMap = (Map) configProvider.getConfigurationObject(STREAMLINED_STATE_PERSISTENCE_NS);
        if (persistenceConfigurationMap != null) {
            persistenceConfigurations = configProvider.getConfigurationObject(STREAMLINED_STATE_PERSISTENCE_NS,
                    PersistenceConfigurations.class);
        } else {
            persistenceConfigurationMap = (Map) configProvider.getConfigurationObject(STATE_PERSISTENCE_NS);
            persistenceConfigurations = configProvider.getConfigurationObject
                    (PersistenceConfigurations.class);
        }

        if (persistenceConfigurations != null && persistenceConfigurations.isEnabled()) {
            String persistenceStoreClassName = persistenceConfigurations.getPersistenceStore();
            try {
                if (Class.forName(persistenceStoreClassName).newInstance() instanceof PersistenceStore) {
                    PersistenceStore persistenceStore =
                            (PersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    persistenceStore.setProperties(persistenceConfigurationMap);
                    siddhiManager.setPersistenceStore(persistenceStore);
                } else if (Class.forName(persistenceStoreClassName).newInstance()
                        instanceof IncrementalPersistenceStore) {
                    IncrementalPersistenceStore incrementalPersistenceStore =
                            (IncrementalPersistenceStore) Class.forName(persistenceStoreClassName).newInstance();
                    incrementalPersistenceStore.setProperties(persistenceConfigurationMap);
                    siddhiManager.setIncrementalPersistenceStore(incrementalPersistenceStore);
                } else {
                    throw new PersistenceStoreConfigurationException("Persistence Store class with name "
                            + persistenceStoreClassName + " is invalid. The given class has to implement either " +
                            "io.siddhi.core.util.persistence.PersistenceStore or " +
                            "io.siddhi.core.util.persistence.IncrementalPersistenceStore.");
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

        File siddhiAppFileReference;

        if (siddhiAppsReference != null) {
            if (siddhiAppsReference.trim().equals("")) {
                // Can't Continue. We shouldn't be here. that means there is a bug in the startup script.
                log.error("Error: Can't get target file to run. System property {} is not set.",
                        SiddhiAppProcessorConstants.SYSTEM_PROP_RUN_SIDDHI_APPS);
            } else {
                // Change relative paths to absolute
                Path siddhiAppsGivenPath = Paths.get(siddhiAppsReference);
                if (!siddhiAppsGivenPath.isAbsolute()) {
                    if (System.getProperty(
                            SiddhiAppProcessorConstants.SYSTEM_PROP_CURRENT_DIRECTORY
                    ) != null) {
                        Path currentWorkingDirectory = Paths.get(
                                System.getProperty(
                                        SiddhiAppProcessorConstants.SYSTEM_PROP_CURRENT_DIRECTORY
                                )
                        ).toAbsolutePath();
                        siddhiAppsReference = currentWorkingDirectory.resolve(
                                siddhiAppsGivenPath.toString()
                        ).normalize().toString();
                    }
                }
                siddhiAppFileReference = new File(siddhiAppsReference);

                if (!siddhiAppFileReference.exists()) {
                    log.error("Error: File " + siddhiAppFileReference.getName() + " not found in the given location.");
                }

                if (siddhiAppFileReference.isDirectory()) {
                    File[] siddhiAppFileArray = siddhiAppFileReference.listFiles();
                    if (siddhiAppFileArray != null) {
                        for (File siddhiApp : siddhiAppFileArray) {
                            try {
                                StreamProcessorDeployer.deploySiddhiQLFile(siddhiApp);
                            } catch (Exception e) {
                                log.error("Exception occurred when deploying the Siddhi App" + siddhiApp.getName(), e);
                            }
                        }
                    }
                } else {
                    try {
                        StreamProcessorDeployer.deploySiddhiQLFile(siddhiAppFileReference);
                    } catch (Exception e) {
                        log.error("Exception occurred when deploying the Siddhi App" +
                                siddhiAppFileReference.getName(), e);
                    }
                }
            }
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

        RetrySiddhiAppDeployment retrySiddhiAppDeployment = new RetrySiddhiAppDeployment();
        retrySiddhiAppDeployment.start();
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

    @Reference(
            name = "siddhi-manager-service",
            service = SiddhiManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiManager"
    )
    protected void setSiddhiManager(SiddhiManager siddhiManager) {
        StreamProcessorDataHolder.setSiddhiManager(siddhiManager);
    }

    protected void unsetSiddhiManager(SiddhiManager siddhiManager) {
        StreamProcessorDataHolder.setSiddhiManager(null);
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
     * Class which handles the Siddhi app deployment retry mechanism.
     */
    public static class RetrySiddhiAppDeployment extends Thread {
        public void run() {
            int i = 1;
            while (i <= 3) {
                try {
                    Thread.sleep(10000);
                    Map<String, String> siddhiApps = StreamProcessorDataHolder.getWaitingForDependencyApps();
                    if (siddhiApps != null) {
                        Iterator<Map.Entry<String, String>> iter = siddhiApps.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry<String, String> entry = iter.next();
                            String siddhiAppFileName = entry.getKey();
                            String siddhiApp = entry.getValue();
                            String siddhiAppName = StreamProcessorDeployer
                                    .getFileNameWithoutExtenson(siddhiAppFileName);
                            try {
                                StreamProcessorDataHolder.getStreamProcessorService().deploySiddhiApp(siddhiApp,
                                        siddhiAppName);
                                StreamProcessorDataHolder.getWaitingForDependencyApps().remove(siddhiAppFileName);
                            } catch (SiddhiAppAlreadyExistException e) {
                                log.error("Siddhi App " + siddhiAppFileName + " is already exists.", e);
                                StreamProcessorDataHolder.getWaitingForDependencyApps().remove(siddhiAppFileName);
                            } catch (Exception e) {
                                if (e instanceof SiddhiAppCreationException &&
                                        e.getMessage().contains("No extension exist for") && i < 3) {
                                    log.debug("Siddhi App deployment retry #" + i + " failed " + siddhiAppFileName, e);
                                } else {
                                    SiddhiAppData siddhiAppData = new SiddhiAppData(siddhiApp, false);
                                    StreamProcessorDataHolder.getStreamProcessorService().
                                            addSiddhiAppFile(siddhiAppName, siddhiAppData);
                                    StreamProcessorDataHolder.getWaitingForDependencyApps().remove(siddhiAppFileName);
                                    iter.remove();
                                    log.error("Error occurred in the retry app deployment task for Siddhi App "
                                            + siddhiAppFileName, e);
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    log.error("Retry App deployment task interrupted ", e);
                } finally {
                    i++;
                }
            }
        }
    }
}
