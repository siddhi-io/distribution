package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.FeedApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.FeedApiServiceImpl;

public class FeedApiServiceFactory {
    private final static FeedApiService service = new FeedApiServiceImpl();

    public static FeedApiService getFeedApi() {
        return service;
    }
}
