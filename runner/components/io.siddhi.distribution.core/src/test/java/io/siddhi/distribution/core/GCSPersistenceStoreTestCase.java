package io.siddhi.distribution.core;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import io.siddhi.core.util.persistence.PersistenceStore;
import io.siddhi.distribution.core.persistence.GCSPersistenceStore;
import io.siddhi.distribution.core.persistence.util.PersistenceConstants;
import io.siddhi.distribution.core.util.UnitTestAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class GCSPersistenceStoreTestCase {

    @Test
    public void testWithoutSettingConfigs() throws InterruptedException {
        PersistenceStore gcsPersistenceStore = new GCSPersistenceStore();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        UnitTestAppender appender = new UnitTestAppender();
        Logger.getRootLogger().addAppender(appender);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(gcsPersistenceStore);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime("@source(type='inMemory', " +
                "topic='Stocks', @map(type='passThrough'))define " +
                "stream StocksStream (symbol string, price float, volume long);");
        siddhiAppRuntime.start();
        siddhiAppRuntime.persist();
        Thread.sleep(1000);
        Assert.assertTrue(appender.getMessages().contains("'bucketName' cannot be null, Please set the bucket name " +
                "and initialize the GCS client before save the persistence"));
    }

    @Test
    public void testToSavePersistence() throws CannotRestoreSiddhiAppStateException, InterruptedException {
        PersistenceStore gcsPersistenceStore = new GCSPersistenceStore();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        UnitTestAppender appender = new UnitTestAppender();
        Logger.getRootLogger().addAppender(appender);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> config = new HashMap<>();
        config.put("bucketName", "siddhi-persistence");
        config.put(PersistenceConstants.CREDENTIAL_FILE_PATH, "<path to the secret key file>");
        properties.put("intervalInMin", "1");
        properties.put("revisionsToKeep", "2");
        properties.put(PersistenceConstants.STATE_PERSISTENCE_CONFIGS, config);
        gcsPersistenceStore.setProperties(properties);
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.setPersistenceStore(gcsPersistenceStore);
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime("@App:name('TestGCSPersistence')" +
                "@source(type='inMemory', " +
                "topic='Stocks', @map(type='passThrough'))define " +
                "stream StocksStream (symbol string, price float, volume long);");
        siddhiAppRuntime.start();
        siddhiAppRuntime.restoreLastRevision();
        siddhiAppRuntime.persist();
        Thread.sleep(3000);
        Assert.assertTrue(String.join(", ", appender.getMessages()).contains("object has been uploaded to " +
                "the bucket successfully."));
    }
}

