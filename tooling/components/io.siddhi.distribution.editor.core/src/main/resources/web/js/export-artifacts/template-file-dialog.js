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

define(['require', 'lodash', 'jquery', 'log', 'ace/ace', 'app/source-editor/editor'],
    function (require, _, $, log, ace, SiddhiEditor) {

        var TemplateFileDialog = function (options) {
            this.app = options.app;
            this.appNames = options.siddhiAppNames;
            this.templateContainer = options.templateHeader;
            this.appArr = this.readSiddhiApps(this.appNames);
            this.editorObjArr = [];

            // var editor = ace.edit("siddhi-app-templates");
            // editor.setTheme("ace/theme/monokai");
            // editor.getSession().setMode("ace/mode/javascript");

        };

        TemplateFileDialog.prototype.constructor = TemplateFileDialog;

        TemplateFileDialog.prototype.render = function () {
            var self = this;
            var i;
            for (i = 0; i < self.appArr.length; i++) {
                var entry = self.appArr[i];
                var divId = "siddhiAppcontentId".concat(i);

                var templateEntry = "<h3>".concat(entry.name).concat("</h3>").concat("<div id='")
                    .concat(divId).concat("' style=\\\"height: 100%;\\\"></div>");
                self.templateContainer.append(templateEntry);

                this._mainEditor = new SiddhiEditor({
                    divID: divId,
                    realTimeValidation: true,
                    autoCompletion: true
                });

                this._editor = ace.edit(divId);
                this._editor.getSession().setValue(entry.content);
                this._editor.resize(true);
                self.editorObjArr.push(this._editor);
            }
            self.templateContainer.accordion();
        };

        TemplateFileDialog.prototype.readSiddhiApps = function (appNames) {
            var self = this;
            var apps = [];
            var i;
            for (i = 0; i < appNames.length; i++) {
                var fileName = appNames[i];
                var fileRelativeLocation = "workspace" + self.app.getPathSeperator() +
                    fileName;
                var defaultView = {configLocation: fileRelativeLocation};
                var workspaceServiceURL = self.app.config.services.workspace.endpoint;
                var openServiceURL = workspaceServiceURL + "/read";

                var path = defaultView.configLocation;
                $.ajax({
                    url: openServiceURL,
                    type: "POST",
                    data: path,
                    contentType: "text/plain; charset=utf-8",
                    async: false,
                    success: function (data, textStatus, xhr) {
                        if (xhr.status == 200) {
                            var pathArray = _.split(path, self.app.getPathSeperator()),
                                fileName = _.last(pathArray),
                                folderPath = _.join(_.take(pathArray, pathArray.length - 1), self.app
                                    .getPathSeperator());
                            var siddhiApp = {
                                name: fileName,
                                content: data.content
                            };
                            apps.push(siddhiApp);
                        } else {
                            // openFileWizardError.text(data.Error);
                            // openFileWizardError.show();
                        }
                    },
                    error: function (res, errorCode, error) {
                        var msg = _.isString(error) ? error : res.statusText;
                        if (isJsonString(res.responseText)) {
                            var resObj = JSON.parse(res.responseText);
                            if (_.has(resObj, 'Error')) {
                                msg = _.get(resObj, 'Error');
                            }
                        }
                        // openFileWizardError.text(msg);
                        // openFileWizardError.show();
                    }
                });
            }
            return apps;
        };

        TemplateFileDialog.prototype.show = function () {
            this._fileOpenModal.modal('show');
        };

        TemplateFileDialog.prototype.getTemplatedApps = function () {
            var self = this;
            var templatedApps = [];
            var i;
            for (i = 0; i < self.editorObjArr.length; i++) {
                templatedApps.push(self.editorObjArr[i].session.getValue());
            }
            return templatedApps;
        };
        return TemplateFileDialog;
    });


