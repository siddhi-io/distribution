package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.SingleApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.SingleApiServiceImpl;

/**
 * Factory class for single API service.
 */
public class SingleApiServiceFactory {
    private static final SingleApiService service = new SingleApiServiceImpl();

    public static SingleApiService getSingleApi() {
        return service;
    }
}
