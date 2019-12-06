/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.siddhi.langserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Document Manager is responsible for maintaining the content of the documents.
 */
public class DocumentManager {

    private Map<Path, String> documents;
    public static final DocumentManager INSTANCE = new DocumentManager();

    private DocumentManager() {
        this.documents = new HashMap<>();
    }

    /**
     * Checks whether the given file is open in workspace.
     *
     * @param filePath Path of the file
     * @return True if the given file is open
     */
    public boolean isFileOpen(Path filePath) {
        return getPathEntry(filePath) != null;
    }

    /**
     * Opens the given file in document manager.
     *
     * @param filePath Path of the file
     * @param content  Content of the file
     */
    public void openFile(Path filePath, String content) {
        if (isFileOpen(filePath)) {
            return;
        }
        this.documents.put(filePath, content);
    }

    /**
     * Updates given file in document manager with new content.
     *
     * @param filePath       Path of the file
     * @param updatedContent New content of the file
     */
    public void updateFile(Path filePath, String updatedContent) {
        Path opened = getPathEntry(filePath);
        if (opened == null) {
            return;
        }
        this.documents.put(opened, updatedContent);
    }

    /**
     * Close the given file in document manager.
     *
     * @param filePath Path of the file
     */
    public void closeFile(Path filePath) {
        Path opened = getPathEntry(filePath);
        if (opened == null) {
            return;
        }
        this.documents.remove(opened);
    }

    /**
     * Gets up-to-date content of the file.
     *
     * @param filePath Path of the file
     * @return Content of the file
     */
    public String getFileContent(Path filePath) {
        if (!isFileOpen(filePath)) {
            return null;
        }
        return documents.get(filePath);
    }

    /**
     * Get the path entry for the given file path.
     *
     * @param filePath File Path to se
     * @return {@link Path}     Path Entry
     */
    private Path getPathEntry(Path filePath) {
        return this.documents.entrySet().stream().filter(entry -> {
            try {
                return Files.isSameFile(entry.getKey(), filePath);
            } catch (IOException e) {
                return false;
            }
        }).map(Map.Entry::getKey).findFirst().orElse(null);
    }
}
