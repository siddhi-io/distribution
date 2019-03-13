package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.FeedApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.FeedApiServiceImpl;

/**
 * Factory class for feed API service.
 */
public class FeedApiServiceFactory {
    private static final FeedApiService service = new FeedApiServiceImpl();

    public static FeedApiService getFeedApi() {
        return service;
    }
}
