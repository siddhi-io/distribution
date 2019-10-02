/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(['require', 'lodash', 'jquery'],
    function (require, _, $) {

        var DockerConfigDialog = function (options) {
            this.container = options.templateHeader;
            this.payload = options.payload;
            this.exportType = options.exportType;
            this.dockerDetailsForm = options.templateHeader.find('#docker-details')
        };

        DockerConfigDialog.prototype.constructor = DockerConfigDialog;

        DockerConfigDialog.prototype.render = function () {
            let self = this;
            let dockerDownloadCheckboxInput = self.container.find("#download-docker-artifacts");
            if (self.exportType === "kubernetes") {
                dockerDownloadCheckboxInput.prop('checked', true);
                dockerDownloadCheckboxInput.attr("disabled", true);
            } else {
                dockerDownloadCheckboxInput.prop('checked', true);
            }

            self.container.find("#docker-push-checkbox").change(function(event){
                if (event.target.checked){
                    self.dockerDetailsForm.show();
                } else {
                    self.dockerDetailsForm.hide();
                }
            });
        };

        DockerConfigDialog.prototype.getDockerConfigs = function () {
            var self = this;
            var templateKeyValue = {};
            templateKeyValue["imageName"] = self.container.find("#docker-img-name-input-field").val();
            templateKeyValue["userName"] = self.container.find("#userName").val();
            templateKeyValue["password"] = self.container.find("#password").val();
            templateKeyValue["email"] = self.container.find("#email").val();
            templateKeyValue["downloadDocker"] = self.container.find("#download-docker-artifacts").is(":checked");
            templateKeyValue["pushDocker"] = self.container.find("#docker-push-checkbox").is(":checked");
            return templateKeyValue;
        };

        return DockerConfigDialog;
    });
