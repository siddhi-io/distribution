/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.siddhi.distribution.test.osgi;

import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.Listeners;
import org.wso2.carbon.container.CarbonContainerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

/**
 * Config Reader Test Case.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class NewConfigReaderTestCase  extends ConfigReaderTestCase {
    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    @Override
    public Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "conf", "configurationnew", CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "runner", CARBON_YAML_FILENAME));
    }
}
