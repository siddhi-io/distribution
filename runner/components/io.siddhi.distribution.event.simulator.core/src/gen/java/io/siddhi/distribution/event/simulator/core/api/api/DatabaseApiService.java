package io.siddhi.distribution.event.simulator.core.api.api;

import io.siddhi.distribution.event.simulator.core.model.model.DBConnectionModel;

import org.wso2.msf4j.Request;


import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public abstract class DatabaseApiService {
    public abstract Response getDatabaseTableColumns(DBConnectionModel body, String tableName, Request request) throws
            NotFoundException;

    public abstract Response getDatabaseTables(DBConnectionModel body, Request request) throws NotFoundException;

    public abstract Response testDBConnection(DBConnectionModel body, Request request) throws NotFoundException;
}
