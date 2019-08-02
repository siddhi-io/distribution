/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.distribution.common.common.utils.config;

import io.siddhi.core.util.SiddhiConstants;
import io.siddhi.core.util.config.ConfigManager;
import io.siddhi.core.util.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.siddhi.distribution.common.common.utils.SPConstants.DATA_PARTITIONING_NAMESPACE;
import static io.siddhi.distribution.common.common.utils.SPConstants.EXTENSIONS_NAMESPACE;
import static io.siddhi.distribution.common.common.utils.SPConstants.REFS_NAMESPACE;

/**
 * Siddhi File Configuration Manager.
 */
public class FileConfigManager implements ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileConfigManager.class);

    private ConfigProvider configProvider;

    public FileConfigManager(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public ConfigReader generateConfigReader(String namespace, String name) {
        if (configProvider != null) {
            try {

                Object extensions = configProvider.getConfigurationObject(EXTENSIONS_NAMESPACE);

                if (extensions == null || extensions instanceof List) {
                    List extensionsListTemp = ((List) extensions);
                    ExtensionsRootConfiguration extensionsRootConfig = getExtensionRootConfig(extensionsListTemp);

                    if (extensionsRootConfig.getExtensions().size() > 0) {
                        ConfigReader childConfiguration = getConfigReader(extensionsRootConfig.getExtensions(),
                                namespace, name, EXTENSIONS_NAMESPACE);
                        if (childConfiguration != null) {
                            return childConfiguration;
                        }
                    } else {
                        RootConfiguration rootConfiguration = configProvider.
                                getConfigurationObject(RootConfiguration.class);
                        if (rootConfiguration.getExtensions().size() > 0) {
                            ConfigReader childConfiguration = getConfigReader(
                                    rootConfiguration.getExtensions(), namespace, name, "siddhi.extensions");
                            if (childConfiguration != null) {
                                return childConfiguration;
                            }
                        }
                    }
                } else {
                    throw new ConfigurationException("The first level under 'extensions' namespace should " +
                            "be a list of type 'extension'");
                }
            } catch (Exception e) {
                LOGGER.error("Could not initiate the extensions configuration object, " + e.getMessage(), e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Could not find a matching configuration for name: " + name + "and namespace: " +
                    namespace + "!");
        }
        return new FileConfigReader(new HashMap<>());
    }

    @Override
    public Map<String, String> extractSystemConfigs(String name) {
        if (configProvider != null) {
            try {

                Object references = configProvider.getConfigurationObject(REFS_NAMESPACE);
                if (references == null || references instanceof List) {
                    List referencesListTemp = ((List) references);
                    RefsRootConfiguration refsRootConf = getReferencesRootConfig(referencesListTemp);

                    if (refsRootConf.getRefs().size() > 0) {
                        Map<String, String> referenceConfigs = getReference(refsRootConf.getRefs(), name,
                                REFS_NAMESPACE);
                        if (referenceConfigs != null) {
                            return referenceConfigs;
                        }
                    } else {
                        RootConfiguration rootConfiguration = configProvider
                                .getConfigurationObject(RootConfiguration.class);
                        if (rootConfiguration.getRefs().size() > 0) {
                            Map<String, String> referenceConfigs = getReference(rootConfiguration.getRefs(),
                                    name, "siddhi.refs");
                            if (referenceConfigs != null) {
                                return referenceConfigs;
                            }
                        }
                    }
                } else {
                    throw new ConfigurationException("The first level under 'refs' namespace should " +
                            "be a list of type 'ref'");
                }

            } catch (Exception e) {
                LOGGER.error("Could not initiate the refs configuration object, " + e.getMessage(), e);
            }
        }
        return new HashMap<>();
    }

    @Override
    public String extractProperty(String name) {
        String property = null;
        if (configProvider != null) {
            try {
                Object dataPartitioningConf = configProvider.getConfigurationObject(DATA_PARTITIONING_NAMESPACE);
                LinkedHashMap dataPartitioningMap;
                if (dataPartitioningConf == null || dataPartitioningConf instanceof Map) {
                    dataPartitioningMap = ((LinkedHashMap) dataPartitioningConf);
                    if (dataPartitioningMap != null && dataPartitioningMap.size() > 0) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Matching property for name: '" + name + "' is looked for under name space '" +
                                    DATA_PARTITIONING_NAMESPACE + "'.");
                        }
                        property = dataPartitioningMap.get(name).toString();
                    } else {
                        RootConfiguration rootConfiguration =
                                configProvider.getConfigurationObject(RootConfiguration.class);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Matching property for name: '" + name + "' is looked for under name space " +
                                    "'siddhi.properties'.");
                        }
                        property = rootConfiguration.getProperties().get(name);
                    }
                } else {
                    throw new ConfigurationException("The first level under 'dataPartitioning' namespace should " +
                            "be a map of type <sting, string>");
                }
            } catch (ConfigurationException e) {
                LOGGER.error("Could not initiate the siddhi configuration object, " + e.getMessage(), e);
            }

            if (property == null && "shardId".equalsIgnoreCase(name)) {
                try {
                    ClusterConfig clusterConfig =
                            configProvider.getConfigurationObject(ClusterConfig.class);
                    if (clusterConfig != null) {
                        if (clusterConfig.getGroupId() != null && clusterConfig.isEnabled()) {
                            return clusterConfig.getGroupId();
                        }
                    }
                } catch (ConfigurationException e) {
                    LOGGER.error("Could not initiate the cluster.config configuration object, " + e.getMessage(), e);
                }

                try {
                    CarbonConfiguration carbonConfiguration =
                            configProvider.getConfigurationObject(CarbonConfiguration.class);
                    if (carbonConfiguration != null && carbonConfiguration.getId() != null) {
                        return carbonConfiguration.getId();
                    }
                } catch (ConfigurationException e) {
                    LOGGER.error("Could not initiate the wso2.carbon configuration object, " + e.getMessage(), e);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Could not find a matching configuration for property name: " + name + "");
        }
        return property;
    }

    private static ConfigReader getConfigReader(List<Extension> extensions, String namespace,
                                                String name, String configNamespace) {
        for (Extension extension : extensions) {
            ExtensionChildConfiguration childConfiguration = extension.getExtension();
            if (childConfiguration.getNamespace().equals(namespace) &&
                    childConfiguration.getName().equals(name) &&
                    childConfiguration.getProperties() != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matching configuration for name: '" + name + "' and namespace: '" + namespace +
                            "' is found under name space '" + configNamespace + "'.");
                }
                return new FileConfigReader(childConfiguration.getProperties());
            }
        }
        return null;
    }

    private ExtensionsRootConfiguration getExtensionRootConfig(List<HashMap> extensionsList) {
        if (extensionsList == null) {
            return new ExtensionsRootConfiguration();
        }
        Yaml yamlTemp = new Yaml();
        String finalYaml = "extensions:\n" + yamlTemp.dumpAs(extensionsList, Tag.SEQ, DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(
                ExtensionsRootConfiguration.class, ExtensionsRootConfiguration.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(finalYaml, ExtensionsRootConfiguration.class);
    }

    private static Map<String, String> getReference(List<Reference> references,
                                                    String name, String configNamespace) {
        for (Reference reference : references) {
            ReferenceChildConfiguration childConf = reference.getReference();
            if (childConf.getName().equals(name)) {
                Map<String, String> referenceConfigs = new HashMap<>();
                referenceConfigs.put(SiddhiConstants.ANNOTATION_ELEMENT_TYPE, childConf.getType());
                if (childConf.getProperties() != null) {
                    referenceConfigs.putAll(childConf.getProperties());
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Matching reference for name: '" + name + "' is found under name space '" +
                            configNamespace + "'.");
                }
                return referenceConfigs;
            }
        }
        return null;
    }

    private RefsRootConfiguration getReferencesRootConfig(List<HashMap> referencesList) {
        if (referencesList == null) {
            return new RefsRootConfiguration();
        }
        Yaml yamlTemp = new Yaml();
        String finalYaml = "refs:\n" + yamlTemp.dumpAs(referencesList, Tag.SEQ, DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(
                RefsRootConfiguration.class, RefsRootConfiguration.class.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(finalYaml, RefsRootConfiguration.class);
    }
}
