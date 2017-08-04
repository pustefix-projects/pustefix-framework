package org.pustefixframework.logging.log4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.schlund.pfixxml.config.EnvironmentProperties;


public class PropertyLookupTest {

    @Before
    public void setUp() {
        EnvironmentProperties.getProperties().setProperty("mode", "test");
    }

    @Test
    public void checkLoggerConfig() {

        Logger logger = LoggerFactory.getLogger(PropertyLookupTest.class);
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
    }

    @Test
    public void checkLoggerConfigImpl() {

        org.apache.logging.log4j.Logger logger = LogManager.getLogger(PropertyLookupTest.class);
        assertEquals("DEBUG", logger.getLevel().toString());

        logger = LogManager.getLogger("undefined");
        assertEquals("WARN", logger.getLevel().toString());

        logger = LogManager.getLogger("prod");
        assertEquals("WARN", logger.getLevel().toString());

        logger = LogManager.getLogger("test");
        assertEquals("INFO", logger.getLevel().toString());
    }

}
