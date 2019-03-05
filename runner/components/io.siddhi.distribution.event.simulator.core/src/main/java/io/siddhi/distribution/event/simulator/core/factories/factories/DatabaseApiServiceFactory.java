package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.DatabaseApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.DatabaseApiServiceImpl;

public class DatabaseApiServiceFactory {
    private final static DatabaseApiService service = new DatabaseApiServiceImpl();

    public static DatabaseApiService getConnectToDatabaseApi() {
        return service;
    }
}
