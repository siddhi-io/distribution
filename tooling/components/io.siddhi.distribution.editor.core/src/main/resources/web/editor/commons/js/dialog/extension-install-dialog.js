/**
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'lodash', 'jquery', 'log', 'backbone'],
    function (require, _, $, log, Backbone) {
        var ExtensionInstallDialog = Backbone.View.extend(
            /** @lends ExtensionInstallDialog.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class ExtensionInstallDialogExtensionInstallDialog
                 * @param {Object} config configuration options for ExtensionInstallDialog
                 */
                initialize: function (options) {
                    // this.app = options;
                    this.dialog_containers = $(_.get(options.config.dialog, 'container'));
                    this.notification_container = _.get(options.config.tab_controller.tabs.tab.das_editor.notifications,
                        'container');
                },

                show: function () {
                    this._sampleFileOpenModal.modal('show');
                },

                render: function () {
                    var self = this;
                    // var app = this.app;
                    var notification_container = this.notification_container;

                    if (!_.isNil(this._sampleFileOpenModal)) {
                        this._sampleFileOpenModal.remove();
                    }

                    var extensionModelOpen = $(
                        "<div class='modal fade' id='extenInstallConfigModal' tabindex='-1' role='dialog' " +
                        "aria-hidden='true'>" + "<div class='modal-dialog file-dialog' role='document'>" +
                        "<div class='modal-content' id='sampleDialog'>" +
                        "<div class='modal-header'>" +
                        "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                        "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                        "</button>" +
                        "<h4 class='modal-title file-dialog-title'>Extension Details</h4>" +
                        "<hr class='style1'>" +
                        "</div>" +
                        "<div class='modal-body'>" +
                        "<div class='container-fluid'>" +
                        "<form class='form-horizontal' onsubmit='return false'>" +
                        "<div class='form-group'>" +
                        "<label for='locationSearch' class='col-sm-2 file-dialog-label'>Search :</label>" +
                        "<input type='text' placeholder='enter the extension name'" +
                        " class='search-file-dialog-form-control'" +
                        " id='locationSearch' autofocus>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-scrollable-block' style='padding: 10px 4px; margin-left:35px;'>" +
                        "<div id='noResults' style='display:none;'>No extension has found</div>" +
                        "<div id='extensionTable' class='samples-pane'>" +
                        "</div>" +
                        "<div id='file-browser-error' class='alert alert-danger' style='display: none;'>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "<div class='form-group'>" +
                        "<div class='file-dialog-form-btn'>" +
                        "<button type='button' class='btn btn-default' data-dismiss='modal'>cancel</button>" +
                        "</div>" +
                        "</div>" +
                        "</form>" +
                        "<div id='extensionInstallError' class='alert alert-danger'>" +
                        "<strong>Error!</strong> Something went wrong." +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>" +
                        "</div>");

                    // function getSuccesNotification(detailedSuccesMsg) {
                    //     return $(
                    //         "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' " +
                    //         "id='error-alert'>" +
                    //         "<span class='notification'>" +
                    //         detailedSuccesMsg +
                    //         "</span>" +
                    //         "</div>");
                    // }
                    //
                    // function getErrorNotification(detailedErrorMsg) {
                    //     return $(
                    //         "<div style='z-index: 9999;' style='line-height: 20%;' class='alert alert-danger' " +
                    //         "id='error-alert'>" +
                    //         "<span class='notification'>" +
                    //         detailedErrorMsg +
                    //         "</span>" +
                    //         "</div>");
                    // }

                    var extenInstallConfigModal = extensionModelOpen.filter("#extenInstallConfigModal");
                    var extensionInstallError = extensionModelOpen.find("#extensionInstallError");
                    var locationSearch = extensionModelOpen.find("input").filter("#locationSearch");

                    extenInstallConfigModal.on('shown.bs.modal', function () {
                        locationSearch.focus();
                    });
                    var treeContainer = extensionModelOpen.find("div").filter("#extensionTable");

                    // var extensionList = getExtensionDetail();

                    //extension array from backend which has details about extensions.
                    var extensionLists = [{name: "ex1", status: "installed"},
                        {name: "ex2fgdfgdfg", status: "installed"},
                        {
                            name: "ex7tyjyjyukuu", status: "partially-installed",
                            info: {
                                description: "this ex7 extension gives the string conversion features to Siddhi" +
                                    " app",
                                install: "To install this ex7 extension you have to set  all dependency of it."
                            }
                        },
                        {name: "ex3ffgfgfgfgfgffgfgfgfgtjj", status: "not-installed"},
                        {
                            name: "ex4aerertrtrt", status: "partially-installed",
                            info: {
                                description: " this ex4 extension gives the string conversion features to Siddhi" +
                                    " app",
                                install: "To install this ex4 extension you have to set  all dependency of it "
                            }
                        },
                        {name: "ex5rtyyjuju", status: "not-installed"},
                        {
                            name: "ex6tyjyjyukuu", status: "partially-installed",
                            info: {
                                description: "this ex6 extension gives the string conversion features to Siddhi" +
                                    " app",
                                install: "To install this ex6 extension you have to set  all dependency of it."
                            }
                        },
                    ];

                    var extensionTable = $('<table class="table table-hover data-table"' +
                        ' id="extenTable"><tbody></tbody></table>');
                    //define the map to store Partially extension modal based on key
                    var partialExtensionDetailModal = new Map();
                    extensionLists.forEach(function (extension) {
                        var extensionTableBodyData;
                        if (extension.status.trim() === "installed") {
                            extensionTableBodyData = $('<tr><td>' + extension.name + '</td><td>Installed</td><td><button' +
                                ' class="btn btn-block btn' +
                                ' btn-primary">UnInstall</button></td></tr>');
                            extensionTableBodyData.find("button").click(function () {
                                extensionUpdate(extension);
                            });
                        } else if (extension.status.trim() === "partially-installed") {
                            var partialExtenIndex = extensionLists.indexOf(extension);
                            extensionTableBodyData = $('<tr><td>' + extension.name + '</td><td>Partially-Installed' +
                                '&nbsp; &nbsp;<a data-toggle="modal"' +
                                ' id="' + partialExtenIndex + '"><i class="fw' +
                                ' fw-info"></i></a></td><td><button' +
                                ' class="btn btn-block btn' +
                                ' btn-primary">UnInstall</button></td></tr>');

                            var paritailModel = $(
                                '<div class="modal fade" id="' + partialExtenIndex + '">' +
                                '<div class="modal-dialog">' +
                                '<div class="modal-content">' +
                                '<div class="modal-header"> ' +
                                "<button type='button' class='close' data-dismiss='modal' aria-label='Close'>" +
                                "<i class=\"fw fw-cancel  about-dialog-close\"></i>" +
                                "</button>" +
                                '<h3 class="modal-title file-dialog-title" id="partialExtenName">'
                                + extension.name +
                                '</h3> ' +
                                '<hr class="style1"> ' +
                                '</div>' +
                                ' <div class="modal-body">' +
                                ' <h3>Description</h3>' +
                                '<div id="partialExtenDescription">'
                                + extension.info.description +
                                '</div>' +
                                '<h3>Install</h3>' +
                                '<div id="partialExtenInstall">'
                                + extension.info.install +
                                '</div>' +
                                '</div>' +
                                ' <div class="modal-footer"> ' +
                                '<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>' +
                                ' </div> ' +
                                '</div>' +
                                '</div> ' +
                                '</div>');

                            partialExtensionDetailModal.set(partialExtenIndex, paritailModel);

                            extensionTableBodyData.find("a").filter("#" + partialExtenIndex).click(function () {
                                extensionPartialModelDisplay(partialExtensionDetailModal.get(partialExtenIndex));
                            });

                            extensionTableBodyData.find("button").click(function () {
                                extensionUpdate(extension);
                            });

                        } else {
                            extensionTableBodyData = $('<tr><td>' + extension.name + '</td><td>Not-Installed</td><td><button' +
                                ' class="btn btn-block btn' +
                                ' btn-primary">Install</button></td></tr>');
                            extensionTableBodyData.find("button").click(function () {
                                extensionUpdate(extension);
                            });
                        }
                        extensionTable.append(extensionTableBodyData);
                    });

                    treeContainer.append(extensionTable);

                    locationSearch.keyup(function () {
                        searchExtension(extensionTable, locationSearch);
                    });

                    $(this.dialog_containers).append(extensionModelOpen);
                    extensionInstallError.hide();
                    this._sampleFileOpenModal = extensionModelOpen;

                    // /**
                    //  * provide the success alert for the success msg.
                    //  * @param successMsg
                    //  */
                    // function alertSuccess(successMsg) {
                    //     var successNotification = getSuccesNotification(successMsg);
                    //     $(notification_container).append(successNotification );
                    //     successNotification .fadeTo(2000, 200).slideUp(1000, function () {
                    //         successNotification .slideUp(1000);
                    //     });
                    // };
                    //
                    // /**
                    //  * show error alert box for the error msg.
                    //  * @param errorMessage
                    //  */
                    // function alertError(errorMessage) {
                    //     var errorNotification = getErrorNotification(errorMessage);
                    //     $(notification_container).append(errorNotification);
                    //     errorNotification.fadeTo(2000, 200).slideUp(1000, function () {
                    //         errorNotification.slideUp(1000);
                    //     });
                    // };
                    //

                    // /**
                    //  *send the updated extension actions to the backend side.
                    //  * @param updateData
                    //  */
                    // function extensionUpdateFunc(updateData) {
                    //     var workspaceServiceURL = app.config.services.workspace.endpoint;
                    //     var updateExtensionURL = workspaceServiceURL + "/updateExtension";
                    //     var browserStorage = app.browserStorage;
                    //
                    //     $.ajax({
                    //         url: updateExtensionURL,
                    //         type: "POST",
                    //         data: path,
                    //         contentType: "text/plain; charset=utf-8",
                    //         async: false,
                    //         success: function (updateData, textStatus, xhr) {
                    //             if (xhr.status == 200) {
                    //                 var file = new File({
                    //                     content: data.content
                    //                 },{
                    //                     storage: browserStorage
                    //                 });
                    //                 alertSuccess("extension"+updateData.name+" "
                    //                     +updateData.action+"ed"+"successfully");
                    //                 extenInstallConfigModal.modal('hide');
                    //
                    //             } else {
                    //                 extensionInstallError.text(data.Error);
                    //                 extensionInstallError.show();
                    //             }
                    //         },
                    //         error: function (res, errorCode, error) {
                    //             // var msg = _.isString(error) ? error : res.statusText;
                    //             // if(isJsonString(res.responseText)){
                    //             //     var resObj = JSON.parse(res.responseText);
                    //             //     if(_.has(resObj, 'Error')){
                    //             //         msg = _.get(resObj, 'Error');
                    //             //     }
                    //             // }
                    //             extensionInstallError.text(msg);
                    //             extensionInstallError.show();
                    //         }
                    //     });
                    // };

                    // /**
                    //  * Get the extension details array from back end.
                    //  */
                    // function getExtensionDetail() {
                    //     var workspaceServiceURL = app.config.services.workspace.endpoint;
                    //     var getExtensionListServiceURL = workspaceServiceURL + "/extensionDetails";
                    //     $.ajax({
                    //         type: "GET",
                    //         contentType: "json",
                    //         url: getExtensionListServiceURL,
                    //         async: false,
                    //         success: function (data) {
                    //             return  data;
                    //         },
                    //         error: function (e) {
                    //             alertError("Unable to load extension details from back end.");
                    //             throw "Unable to load extension details from  back end";
                    //         }
                    //     });
                    // }

                    /**
                     * search function for seek the extensions.
                     * @param extensionTable
                     * @param locationSearch
                     */
                    function searchExtension(extensionTable, locationSearch) {
                        var unmatchedCount = 0, filter, table, tr, td, i, txtValue;
                        var noResultsElement = extensionModelOpen.find("div").filter("#noResults");
                        filter = locationSearch.val().toUpperCase();
                        table = extensionTable[0];
                        tr = table.getElementsByTagName("tr");
                        for (i = 0; i < tr.length; i++) {
                            td = tr[i].getElementsByTagName("td")[0];
                            if (td) {
                                txtValue = td.textContent || td.innerText;
                                if (txtValue.toUpperCase().indexOf(filter) > -1) {
                                    tr[i].style.display = "";
                                } else {
                                    tr[i].style.display = "none";
                                    unmatchedCount += 1;
                                }
                            }
                        }
                        var isMatched = (unmatchedCount === tr.length);
                        noResultsElement.toggle(isMatched);
                    }

                    /**
                     * provide the update details about the extension
                     * @param extension
                     */
                    function extensionUpdate(extension) {
                        var updateData = {
                            "name": extension.name,
                            "action": (extension.status === "not-installed") ? 'install' : 'unInstall'
                        };
                        alert(updateData.name + " " + updateData.action);
                        //send the update data to back end
                        // extensionUpdateFunc(updateData);
                    }

                    /**
                     * display the inner modal box for the partially installed extension.
                     * @param extensionPartialModel
                     */
                    function extensionPartialModelDisplay(extensionPartialModel) {
                        self._extensionPartialModel = extensionPartialModel;
                        self._extensionPartialModel.modal('show');
                    }

                },
            });

        return ExtensionInstallDialog;
    });
