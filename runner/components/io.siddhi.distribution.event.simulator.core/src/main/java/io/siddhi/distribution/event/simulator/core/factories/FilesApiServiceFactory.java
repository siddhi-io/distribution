package io.siddhi.distribution.event.simulator.core.factories;

import io.siddhi.distribution.event.simulator.core.api.FilesApiService;
import io.siddhi.distribution.event.simulator.core.impl.FilesApiServiceImpl;

/**
 * Factory class of Files API service.
 */
public class FilesApiServiceFactory {
    private static final FilesApiService service = new FilesApiServiceImpl();

    public static FilesApiService getFilesApi() {
        return service;
    }
}
