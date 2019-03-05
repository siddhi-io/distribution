package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.FilesApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.FilesApiServiceImpl;

public class FilesApiServiceFactory {
    private final static FilesApiService service = new FilesApiServiceImpl();

    public static FilesApiService getFilesApi() {
        return service;
    }
}
