package io.siddhi.distribution.event.simulator.core.factories.factories;

import io.siddhi.distribution.event.simulator.core.api.api.FilesApiService;
import io.siddhi.distribution.event.simulator.core.impl.impl.FilesApiServiceImpl;

/**
 * Factory class of Files API service.
 */
public class FilesApiServiceFactory {
    private static final FilesApiService service = new FilesApiServiceImpl();

    public static FilesApiService getFilesApi() {
        return service;
    }
}
