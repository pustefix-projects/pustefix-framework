/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.jmx;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.PathFactory;

/** 
 * Configures and starts JmxServer
 */
public class JmxServerFactory {
    /** don't store this in a config file, because client apps can access values where */
    public static final int PORT_A = 9334;
    public static final int PORT_B = 9335;
    
    private static Logger LOG = Logger.getLogger(JmxServerFactory.class);
    
    private static JmxServerFactory instance = new JmxServerFactory();
    
    public static JmxServerFactory getInstance() {
        return instance;
    }
    
    public static JmxServer getServer() {
    	if (instance.server == null) {
            throw new RuntimeException();
    	}
    	return instance.server;
    }
    
    private JmxServer server;
    
    public JmxServerFactory() {
    	server = null;
    }
    
    public void init(Properties props) throws Exception {
        InetAddress host;
        
        LOG.debug("starting jmx server");
        host = getHost();
        try {
            server = new JmxServer();
            server.start(host, getPort(host));
        } catch (Exception e) {
            LOG.debug("not started", e);
        }
        LOG.info("started");
    }
    
    private static InetAddress getHost() throws UnknownHostException {
        String machine;
        
        machine = System.getenv("MACHINE");
        if (machine != null) {
            return InetAddress.getByName(machine);
        } else {
            return InetAddress.getLocalHost();
        }
    }
    
    private static int getPort(InetAddress host) {
        String name;
        
        LOG.debug("host: " + host.getHostName());
        LOG.debug("path: " + PathFactory.getInstance().createPath("foo").resolve().getAbsolutePath());
        if (host.getHostName().startsWith("pustefix")) {
            LOG.debug("pustefix life port hack");
            name = PathFactory.getInstance().createPath("foo").resolve().getAbsolutePath();
            if (name.endsWith("/pfixschlund_b/projects/foo")) {
                return PORT_B;
            }
        }
        return PORT_A;
    }
}
