/**
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

define(['require', 'lodash', 'jquery', 'log', 'backbone', '../../../js/event-simulator/simulator-rest-client',
        './query-store-rest-client', 'datatables', 'datatables_bootstrap', 'datatables_wso2'],
    function (require, _, $, log, Backbone, SimulatorRestClient, QueryStoreRestClient) {
        var QueryStoreDialog = Backbone.View.extend(
            /** @lends QueryStoreDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class queryStore
                 * @param {Object} config configuration options for the QueryStoreDialog
                 */
                initialize: function (options) {
                    this._options = options;
                    this.application = _.get(options, "application");
                    this._dialogContainer = $(_.get(options, "application.config.dialog.container"));
                },

                show: function () {
                    this._storeQueryModal.modal('show');
                },

                render: function () {
                    var app = this.application,
                        options = this._options;

                    if (!_.isNil(this._storeQueryModal)) {
                        this._storeQueryModal.remove();
                    }

                    var storeQueryModal = $(options.selector).clone();
                    this._storeQueryModal = storeQueryModal;
                    var errorBox = storeQueryModal.find('#error-box');
                    var resultBox = storeQueryModal.find('#simulator_output');
                    var queryTextArea = storeQueryModal.find("textarea[id='curlEditor']");
                    var appNameSelector = storeQueryModal.find("select[name='siddhi-app-name']");

                    var showError = function (message) {
                        errorBox.text(message).show();
                    };

                    var clearError = function () {
                        errorBox.hide();
                    };

                    SimulatorRestClient.retrieveSiddhiAppNames(
                        function (data) {
                            var template = '<option value="{{dataName}}">{{dataName}}</option>';
                            var options =
                                '<option selected="selected" value = "-1" disabled>-- Please Select a Siddhi App --</option>';
                            if (data) {
                                data.sort();
                                for (var i = 0; i < data.length; i++) {
                                    options += template.replaceAll('{{dataName}}', data[i].siddhiAppName);
                                }
                            }
                            storeQueryModal.find("select[name='siddhi-app-name']").html(options);
                        },
                        function (data) {
                            showError("Error when retrieving siddhi apps. Reason: " + data);
                        }
                    );

                    appNameSelector.on('change', function (event) {
                        var siddhiAppName = storeQueryModal.find("select[name='siddhi-app-name']").val();
                        if (siddhiAppName != 'undefined' && sessionStorage.getItem("onDemandTempStore") != null) {
                            var onDemandTempStore = JSON.parse(sessionStorage.getItem("onDemandTempStore"));
                            if (onDemandTempStore[siddhiAppName] != null) {
                                queryTextArea.val(onDemandTempStore[siddhiAppName]);
                            } else {
                                queryTextArea.val('');
                            }
                        }
                    });

                    queryTextArea.on('input change keyup', function (event) {
                        var siddhiAppName = storeQueryModal.find("select[name='siddhi-app-name']").val();
                        var onDemandQuery = storeQueryModal.find("textarea[id='curlEditor']").val();
                        if (sessionStorage.getItem("onDemandTempStore") !== null) {
                            if (siddhiAppName !== 'undefined' && onDemandQuery !== 'undefined') {
                                var onDemandTempStore = JSON.parse(sessionStorage.getItem("onDemandTempStore"));
                                onDemandTempStore[siddhiAppName] = onDemandQuery;
                                sessionStorage.setItem("onDemandTempStore", JSON.stringify(onDemandTempStore));
                            }
                        } else {
                            var onDemandTempStore = {};
                            onDemandTempStore[siddhiAppName] = onDemandQuery;
                            sessionStorage.setItem("onDemandTempStore", JSON.stringify(onDemandTempStore));
                        }
                        event.preventDefault();
                    });

                    storeQueryModal.submit(function (event) {
                        QueryStoreRestClient.retrieveStoresQuery(
                            storeQueryModal.find("select[name='siddhi-app-name']").val(),
                            storeQueryModal.find("textarea[id='curlEditor']").val(),
                            function (data) {
                                // Add header row
                                var headerColumns = [];
                                data.details.forEach(function (header) {
                                    headerColumns.push('<th>' + header.name + '</th>');
                                });
                                var headerRow = '<tr>' + headerColumns + '</tr>';

                                // Add data rows
                                var rows = [];
                                data.records.forEach(function (record) {
                                    var columns = [];
                                    record.forEach(function (value) {
                                        columns.push('<td>' + value + '</td>');
                                    });
                                    rows.push('<tr>' + columns + '</tr>');
                                });

                                var queryData = storeQueryModal.find("table[id='query_data']");
                                if ($.fn.DataTable.isDataTable(queryData)) {
                                    queryData.DataTable().clear().destroy();
                                }
                                clearError();
                                resultBox.find('thead').html(headerRow);
                                resultBox.find('tbody').html(rows);
                                queryData.DataTable();
                                queryData.removeClass('hidden');
                            },
                            function (data) {
                                var queryData = storeQueryModal.find("table[id='query_data']");
                                if ($.fn.DataTable.isDataTable(queryData)) {
                                    queryData.DataTable().clear().destroy();
                                }
                                resultBox.find('thead').html('');
                                resultBox.find('tbody').html('');
                                queryData.addClass('hidden');

                                showError("Error when executing query on Siddhi Store. Reason: " + data.responseText);
                            }
                        );
                        event.preventDefault();
                    });
                }
            });
        return QueryStoreDialog;
    });
