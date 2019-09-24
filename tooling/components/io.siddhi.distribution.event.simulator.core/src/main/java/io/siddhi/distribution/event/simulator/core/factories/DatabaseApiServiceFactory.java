package io.siddhi.distribution.event.simulator.core.factories;

import io.siddhi.distribution.event.simulator.core.api.DatabaseApiService;
import io.siddhi.distribution.event.simulator.core.impl.DatabaseApiServiceImpl;

/**
 * Factory class for Database API service.
 */
public class DatabaseApiServiceFactory {
    private static final DatabaseApiService service = new DatabaseApiServiceImpl();

    public static DatabaseApiService getConnectToDatabaseApi() {
        return service;
    }
}
