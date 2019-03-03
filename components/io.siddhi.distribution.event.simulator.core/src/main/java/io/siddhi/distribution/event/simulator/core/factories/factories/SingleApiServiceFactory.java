package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.SingleApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.SingleApiServiceImpl;

public class SingleApiServiceFactory {
    private final static SingleApiService service = new SingleApiServiceImpl();

    public static SingleApiService getSingleApi() {
        return service;
    }
}
