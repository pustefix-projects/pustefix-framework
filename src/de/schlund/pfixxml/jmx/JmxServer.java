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

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

/** 
 * Jmx Server, started via factory.init
 */
public class JmxServer implements JmxServerMBean {
    private static Category CAT = Category.getInstance(JmxServer.class.getName());

    private static JmxServer instance = new JmxServer();

    public static JmxServer getInstance() {
        return instance;
    }
    
    private int port;
    private final List knownClients;
    
    public JmxServer() {
        this.port = -1; // not started yet
        this.knownClients = new ArrayList();
    }
    
    public void init(Properties props) throws Exception {
        this.port = PropertiesUtils.getInteger(props, "jmx.server.port"); 
        start();
    }

    public void start() throws Exception {
        MBeanServer server;
        JMXConnectorServer connection;

        try {
            server = MBeanServerFactory.createMBeanServer();
            server.registerMBean(this, createServerName());
            // otherwise, clients cannot instaniate TrailLogger objects:
            server.registerMBean(this.getClass().getClassLoader(), createName("loader"));
            connection = JMXConnectorServerFactory.newJMXConnectorServer(createServerURL(null, port), null, server);
        	connection.start();
        	CAT.info("server started on port " + port);
        } finally {
            port = -1;
        }
    }
	
    public boolean isKnownClient(String remoteAddr) {
        return knownClients.contains(remoteAddr);
    }

    public void addKnownClient(String remoteAddr) {
        knownClients.add(remoteAddr);
    }
    
    public void removeKnownClient(String remoteAddr) {
        knownClients.remove(remoteAddr);
    }

    //--

    public static JMXServiceURL createServerURL(String hostname, int port) {
        try { 
            return new JMXServiceURL("jmxmp", hostname, port);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ObjectName createServerName() {
        return createName(JmxServer.class.getName());
    }

    public static ObjectName createName() {
        return createName(TrailLogger.class.getName());
    }

    private static ObjectName createName(String type) {
        try {
            return new ObjectName("testalizer:type=" + type);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    //--
    
    public List getSessions(String serverName, String remoteAddr) {
        SessionAdmin admin;
        Iterator iter;
        String id;
        List lst;
        SessionInfoStruct info;

        lst = new ArrayList();
        admin = SessionAdmin.getInstance();
        iter = SessionAdmin.getInstance().getAllSessionIds().iterator();
        while (iter.hasNext()) {
            id = (String) iter.next();
            info = admin.getInfo(id);
            if (serverName.equals(info.getData().getServerName()) && remoteAddr.equals(info.getData().getRemoteAddr())) {
                lst.add(info.getData());
            }
        }
        return lst;
    }
    
    public void invalidateSession(String id) throws IOException {
        getSession(id).invalidate();
    }
    
    public static HttpSession getSession(String id) throws IOException {
        SessionInfoStruct info;
        
        info = SessionAdmin.getInstance().getInfo(id);
        if (info == null) {
            throw new IOException("session not found: " + id);
        }
        return info.getSession();
    }
}
