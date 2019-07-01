package io.siddhi.distribution.test.framework.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

/**
 * Contains utility methods utilized during the process of converting a JAR file
 * to an OSGi bundle.
 */
public class BundleUtil {
    private static final Logger logger = LoggerFactory.getLogger(BundleUtil.class);

    /**
     * Converts a specified non-OSGi JAR file to an OSGi bundle at the specified destination.
     *
     * @param sourcePath      the directory of the JAR file to be bundled
     * @param outputPath the directory into which the created OSGi bundle needs to be placed
     */
    public static void convertFromJarToBundle(String sourcePath, String outputPath) {
        File inputDir = new File(sourcePath);
        File outputDir = new File(outputPath);
        if ((inputDir.exists()) && (outputDir.exists())) {
            if ((Files.isReadable(inputDir.toPath())) && (Files.isWritable(outputDir.toPath()))) {
                try {
                    if (!Files.isDirectory(inputDir.toPath())) {
                        if (!BundleGeneratorUtils.isOSGiBundle(inputDir.toPath())) {
                            BundleGeneratorUtils.
                                    convertFromJarToBundle(inputDir.toPath(), outputDir.toPath(),
                                            new Manifest(), "");
                        } else {
                            FileUtils.copyDirectory(inputDir, outputDir);
                            logger.debug("Path " + inputDir.toString() + " refers to an OSGi bundle");
                        }
                    } else {
                        List<Path> directoryContent = listFiles(inputDir.toPath());
                        for (Path aDirectoryItem : directoryContent) {
                            if (aDirectoryItem.toString().endsWith(".jar")) {
                                if (!BundleGeneratorUtils.isOSGiBundle(aDirectoryItem)) {
                                    BundleGeneratorUtils.convertFromJarToBundle(
                                            aDirectoryItem, outputDir.toPath(), new Manifest(), "");
                                } else {
                                    FileUtils.copyFileToDirectory(aDirectoryItem.toFile(), outputDir);
                                    logger.debug("Path " + inputDir.toString() + " refers to an OSGi bundle");
                                }
                            }
                        }
                    }
                } catch (IOException | CarbonToolException e) {
                    logger.error(
                            "An error occurred when making the JAR (Java Archive) to OSGi bundle conversion", e);
                }
            } else {
                logger.warn("The source location and/or output location does not have appropriate " +
                        "read/write permissions.");
            }
        } else {
            logger.warn("Invalid file path(s)");
        }
    }

    /**
     * Returns a {@code List} of file paths of the child elements of the specified directory.
     *
     * @param directory the directory whose child elements are to be returned
     * @return a {@link List} of {@link Path} instances of the child elements of the specified directory
     * @throws IOException if an I/O error occurs
     */
    public static List<Path> listFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            directoryStream.forEach(files::add);
        }
        return files;
    }
}
