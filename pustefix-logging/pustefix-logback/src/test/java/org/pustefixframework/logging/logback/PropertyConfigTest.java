package org.pustefixframework.logging.logback;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlund.pfixxml.config.EnvironmentProperties;

public class PropertyConfigTest {

    @Test
    public void checkLoggerConfig() {

        EnvironmentProperties.getProperties().setProperty("mode", "test");
        EnvironmentProperties.getProperties().setProperty("mylevel", "INFO");

        Logger logger = LoggerFactory.getLogger(PropertyConfigTest.class);
        assertTrue(logger.isDebugEnabled());
        assertFalse(logger.isTraceEnabled());

        logger = LoggerFactory.getLogger("undefined");
        assertTrue(logger.isWarnEnabled());
        assertFalse(logger.isInfoEnabled());

        logger = LoggerFactory.getLogger("prod");
        assertTrue(logger.isWarnEnabled());
        assertFalse(logger.isInfoEnabled());

        logger = LoggerFactory.getLogger("test");
        assertTrue(logger.isInfoEnabled());
        assertFalse(logger.isDebugEnabled());

        logger = LoggerFactory.getLogger("mylogger");
        assertTrue(logger.isInfoEnabled());
        assertFalse(logger.isDebugEnabled());
    }

}
