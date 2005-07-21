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

import org.apache.log4j.Category;

import de.schlund.pfixxml.PathFactory;

/** 
 * Configures and starts JmxServer
 */
public class JmxServerFactory {
    /** don't store this in a config file, because client apps can access values where */
    public static final int PORT_A = 9334;
    public static final int PORT_B = 9335;

    private static Category LOG = Category.getInstance(JmxServerFactory.class.getName());

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
        InetAddress host;
        
        host = InetAddress.getLocalHost(); 
        // TODO: i'd like to use
        //    getenv("MACHINE")
        // (instead of the hack below) but it throws an exception in Java 1.4
        if (host.getHostName().equals("pem.schlund.de")) {
            host = InetAddress.getByName(System.getProperty("user.name") +
                    "." + host.getHostName());
            LOG.debug("hacked host: " + host.getHostName());
        }
        return host;
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
