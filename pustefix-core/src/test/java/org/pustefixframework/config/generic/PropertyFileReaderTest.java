/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.config.generic;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;
import org.pustefixframework.config.customization.RuntimeProperties;


/**
 * 
 * @author mleidig
 *
 */
public class PropertyFileReaderTest {

    @Before
    public void setUp() {
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
    }
    
    @Test
    public void test() throws Exception {
        InputStream in = getClass().getResourceAsStream("/properties.xml");
        Properties props = new Properties();
        PropertyFileReader.read(in, props);
        Properties refProps = new Properties();
        refProps.setProperty("foo", "bar");
        refProps.setProperty("hey", "ho");
        refProps.setProperty("fqdn", RuntimeProperties.getProperties().getProperty("fqdn"));
        assertEquals(props, refProps);
    }
}
