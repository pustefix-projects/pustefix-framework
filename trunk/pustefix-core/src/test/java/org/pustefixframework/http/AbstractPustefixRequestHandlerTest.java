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

package org.pustefixframework.http;

import java.lang.management.ManagementFactory;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

public class AbstractPustefixRequestHandlerTest extends TestCase {
    
    /**
     * Test mapping of HTTP to HTTPS port and vice versa
     */
    public void testSSLPortMapping() throws Exception {
        
        assertEquals(443, AbstractPustefixRequestHandler.getSSLRedirectPort(80, new Properties()));
        assertEquals(80, AbstractPustefixRequestHandler.getNonSSLRedirectPort(443, new Properties()));
        
        Properties props = new Properties();
        assertEquals(443, AbstractPustefixRequestHandler.getSSLRedirectPort(80, props));
        assertEquals(80, AbstractPustefixRequestHandler.getNonSSLRedirectPort(443, props));
        
        props = new Properties();
        assertEquals(443, AbstractPustefixRequestHandler.getSSLRedirectPort(8080, props));
        assertEquals(80, AbstractPustefixRequestHandler.getNonSSLRedirectPort(8443, props));
        
        props = new Properties();
        props.put(AbstractPustefixRequestHandler.PROP_SSL_REDIRECT_PORT + 8080, "8443");
        assertEquals(8443, AbstractPustefixRequestHandler.getSSLRedirectPort(8080, props));
        assertEquals(8080, AbstractPustefixRequestHandler.getNonSSLRedirectPort(8443, props));
        assertEquals(443, AbstractPustefixRequestHandler.getSSLRedirectPort(80, props));
        assertEquals(80, AbstractPustefixRequestHandler.getNonSSLRedirectPort(443, props));
        assertEquals(443, AbstractPustefixRequestHandler.getSSLRedirectPort(9080, props));
        assertEquals(80, AbstractPustefixRequestHandler.getNonSSLRedirectPort(9443, props));

        //test automatically detecting redirectPort by MBean lookup
        ObjectName objName = new ObjectName("Test:type=Connector,port=8080");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        TestConnector mbean = new TestConnector();
        mbean.setRedirectPort(8443);
        props = new Properties();
        try {
            server.registerMBean(mbean, objName);
            assertEquals(8443, AbstractPustefixRequestHandler.getSSLRedirectPort(8080, props));
            assertEquals(8080, AbstractPustefixRequestHandler.getNonSSLRedirectPort(8443, props));
        } finally {
            server.unregisterMBean(objName);
        }

    }

    public interface TestConnectorMBean {

        public int getredirectPort();
        
    }
    
    class TestConnector implements TestConnectorMBean {

        int redirectPort;
        
        public int getredirectPort() {
            return redirectPort;
        }
        
        public void setRedirectPort(int redirectPort) {
            this.redirectPort = redirectPort;
        }
        
    }

}
