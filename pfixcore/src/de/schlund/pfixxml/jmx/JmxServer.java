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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.jmxmp.JMXMPConnectorServer;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import de.schlund.pfixxml.perflogging.PerfLogging;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import de.schlund.pfixxml.util.Xml;

/** 
 * Jmx Server, started via factory.init
 */
public class JmxServer implements JmxServerMBean {
    private static Logger LOG = Logger.getLogger(JmxServer.class);

    private final MBeanServer server;
    private final List knownClients;
    
    public JmxServer(MBeanServer srv) {
        this.server = srv;
        this.knownClients = new ArrayList();
    }
    
	//--
    
    public void start(String host, int port, String pathtokeystore) throws Exception {
        JMXConnectorServer connector;
        FileResource keystore;
        
        
        final ObjectName createServerName = createServerName();
        LOG.info("Registering MBean:"+createServerName.getCanonicalName());
        server.registerMBean(this, createServerName);
        // otherwise, clients cannot instaniate TrailLogger objects:
        final ObjectName createName = createName("loader");
        LOG.info("Registering MBean:"+createName.getCanonicalName());
        server.registerMBean(this.getClass().getClassLoader(), createName);
        
        LOG.info(server.getMBeanCount()+" MBean registered.");
        
        Map env = null;
        if(pathtokeystore != null) {
            keystore = ResourceUtil.getFileResourceFromDocroot(pathtokeystore);
            Environment.assertCipher();
            env = Environment.create(keystore, true);
        }  else {
            env = Environment.create(null, false);
        }
        connector = JMXConnectorServerFactory.newJMXConnectorServer(createServerURL(host, port), 
                env, server);
       
        
       
        connector.start();
        connector.addNotificationListener(new JMXConnectionListener(), null, null);
        LOG.info("started secure=["+new Boolean(pathtokeystore!=null)+"] server on: " + connector.getAddress()+" with attr ["+connector.getAttributes()+"] and env ="+env); 
        notifications(connector);
        
    }

    private static void notifications(JMXConnectorServer connector) {
    	connector.addNotificationListener(new NotificationListener() {
                public void handleNotification(Notification n, Object arg1) {
                    JMXConnectionNotification cn;
                    JMXMPConnectorServer cs;
                    
                    if (n instanceof JMXConnectionNotification) {
                        cn = (JMXConnectionNotification) n;
                        cs = (JMXMPConnectorServer) n.getSource();
                        StringBuffer sb = new StringBuffer();
                        sb.append("connection: " + n.getType() + " " + cn.getConnectionId() + " " + n.getMessage()+"\n");
                        sb.append("address: " + cs.getAddress()+"\n");
                        sb.append("attributes" + cs.getAttributes()+"\n");
                        LOG.info(sb.toString());
                    } else {
                        StringBuffer sb = new StringBuffer();
                        sb.append("notification " + n.getClass() + ": " + n.getMessage() + " " + arg1+"\n");
                        sb.append("type " + n.getType()+"\n");
                        sb.append("source: " + n.getSource()+"\n");
                        LOG.info(sb.toString());
                    }
                }}, null, null);
    }

  /*  private void javaLogging() throws IOException {
        Logger logger;
        Handler handler;
        
        logger = Logger.getLogger("javax.management.remote");
        logger.setLevel(Level.FINER);
        handler = new FileHandler("jmx.log");
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
    }*/

    //-- client authentication
    
    public boolean isKnownClient(String remoteAddr) {
        return knownClients.contains(remoteAddr);
    }

    public void addKnownClient(String remoteAddr) {
        knownClients.add(remoteAddr);
    }
    
    public void removeKnownClient(String remoteAddr) {
        knownClients.remove(remoteAddr);
    }
    
    public ApplicationList getApplicationList(boolean tomcat, String sessionSuffix) {
        FileResource file;
        
        file = ResourceUtil.getFileResourceFromDocroot("servletconf/projects.xml");
        try {
            return ApplicationList.load(Xml.parseMutable(file), tomcat, sessionSuffix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //--

    public static JMXServiceURL createServerURL(String host, int port) {
        try { 
            return new JMXServiceURL("jmxmp", host, port);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static ObjectName createServerName() {
        return createName(JmxServer.class.getName());
    }
    
    // TODO: who's responsible for unregister?
    public ObjectName startRecording(String sessionId) throws IOException {
    	ObjectName name;
    	TrailLogger logger;
    	
    	logger = new TrailLogger(TrailLogger.getVisit(getSession(sessionId)));
    	name = createName(TrailLogger.class.getName(), sessionId);
        try {
            server.registerMBean(logger, name);
        } catch (InstanceAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (MBeanRegistrationException e) {
            throw new RuntimeException(e);
        } catch (NotCompliantMBeanException e) {
            throw new RuntimeException(e);
        }
    	
    	return name;
    }

    private static ObjectName createName(String type) {
        try {
            return new ObjectName("testalizer:type=" + type);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectName createName(String type, String session) {
        try {
            return new ObjectName("testalizer:type=" + type + ",session=" + session);
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
        iter = admin.getAllSessionIds().iterator();
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
    
    public boolean isPerfLoggingEnabled() {
        return PerfLogging.getInstance().isPerfLogggingEnabled();
    }
    
    public boolean isPerfLoggingRunning() {
        return PerfLogging.getInstance().isPerfLoggingActive();
    }
    
    public void startPerfLogging() {
        PerfLogging.getInstance().activatePerflogging();
    }
    
    public String stopPerfLogging() {
        return PerfLogging.getInstance().inactivatePerflogging();
    }
    
    public Map<String, Map<String, int[]>> stopPerfLoggingMap() {
        return PerfLogging.getInstance().inactivatePerfloggingMap();
    }
    
   
}

