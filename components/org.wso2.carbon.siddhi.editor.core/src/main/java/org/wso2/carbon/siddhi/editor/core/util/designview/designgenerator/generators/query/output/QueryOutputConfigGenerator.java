/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.output;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.setattributeconfig.SetAttributeConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.output.stream.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create the output part of a Query Config
 */
public class QueryOutputConfigGenerator {
    private Query query;
    private String siddhiAppString;

    public QueryOutputConfigGenerator(Query query, String siddhiAppString) {
        this.query = query;
        this.siddhiAppString = siddhiAppString;
    }

    // TODO: 4/17/18 comment
    public QueryOutputConfig generateQueryOutputConfig(Query query, String siddhiAppString) {
        OutputStream queryOutputStream = query.getOutputStream();
        if (queryOutputStream instanceof InsertIntoStream) {
            return generateInsertOutputConfig((InsertIntoStream)(query.getOutputStream()));
        } else if (queryOutputStream instanceof DeleteStream) {
            return generateDeleteOutputConfig((DeleteStream)(query.getOutputStream()), siddhiAppString);
        } else if (queryOutputStream instanceof UpdateStream) {
            return generateUpdateOutputConfig((UpdateStream) (query.getOutputStream()), siddhiAppString);
        } else if (queryOutputStream instanceof UpdateOrInsertStream) {
            return generateUpdateOrInsertIntoOutputConfig(
                    (UpdateOrInsertStream) (query.getOutputStream()), siddhiAppString);
        }
        throw new IllegalArgumentException("Unknown type of Query Output Stream for generating Query Output Config");
    }

    private QueryOutputConfig generateInsertOutputConfig(InsertIntoStream insertIntoStream) {
        return new QueryOutputConfig(
                QueryOutputType.INSERT.toString(),
                new InsertOutputConfig(insertIntoStream.getOutputEventType().name()),
                insertIntoStream.getId());
    }

    private QueryOutputConfig generateDeleteOutputConfig(DeleteStream deleteStream, String siddhiAppString) {
        return new QueryOutputConfig(
                QueryOutputType.DELETE.toString(),
                new DeleteOutputConfig(
                        deleteStream.getOutputEventType().name(),
                        ConfigBuildingUtilities.getDefinition(deleteStream.getOnDeleteExpression(), siddhiAppString)),
                deleteStream.getId());
    }

    private QueryOutputConfig generateUpdateOutputConfig(UpdateStream updateStream, String siddhiAppString) {
        return new QueryOutputConfig(
                QueryOutputType.UPDATE.toString(),
                new UpdateOutputConfig(
                        updateStream.getOutputEventType().name(),
                        generateSetAttributeConfigsList(
                                updateStream.getUpdateSet().getSetAttributeList(), siddhiAppString),
                        ConfigBuildingUtilities.getDefinition(updateStream.getOnUpdateExpression(), siddhiAppString)),
                updateStream.getId());
    }

    private QueryOutputConfig generateUpdateOrInsertIntoOutputConfig(UpdateOrInsertStream updateOrInsertStream,
                                                                     String siddhiAppString) {
        return new QueryOutputConfig(
                QueryOutputType.UPDATE_OR_INSERT_INTO.toString(),
                new UpdateOrInsertIntoOutputConfig(
                        updateOrInsertStream.getOutputEventType().name(),
                        generateSetAttributeConfigsList(
                                updateOrInsertStream.getUpdateSet().getSetAttributeList(), siddhiAppString),
                        ConfigBuildingUtilities.getDefinition(
                                updateOrInsertStream.getOnUpdateExpression(), siddhiAppString)),
                updateOrInsertStream.getId());
    }

    private List<SetAttributeConfig> generateSetAttributeConfigsList(List<UpdateSet.SetAttribute> setAttributes,
                                                                     String siddhiAppString) {
        List<SetAttributeConfig> setAttributeConfigs = new ArrayList<>();
        for (UpdateSet.SetAttribute setAttribute : setAttributes) {
            // Attribute name and value
            String attributeName = setAttribute.getTableVariable().getAttributeName();
            if (setAttribute.getTableVariable().getStreamId() != null) {
                attributeName = setAttribute.getTableVariable().getStreamId() + "." + attributeName;
            }
            String attributeValue =
                    ConfigBuildingUtilities.getDefinition(setAttribute.getAssignmentExpression(), siddhiAppString);

            setAttributeConfigs.add(new SetAttributeConfig(attributeName, attributeValue));
        }
        return setAttributeConfigs;
    }

    /**
     * Query Output Type
     */
    private enum QueryOutputType {
        INSERT,
        DELETE,
        UPDATE,
        UPDATE_OR_INSERT_INTO
    }
}
