package org.pustefixframework.admin.mbeans;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import de.schlund.pfixcore.exception.PustefixRuntimeException;

/**
 * MBean providing webapp administration methods.
 * 
 * @author mleidig@schlund.de
 *
 */
public class Admin implements AdminMBean {
    
    public final static String JMX_NAME = "Pustefix:type=Admin";
    
    private Logger LOG = Logger.getLogger(Admin.class.getName());
   
    private int port;
    
    public Admin(int port) {
        this.port = port;
        Thread listener = new ListenerThread(port);
        listener.start();
        try {
            Field field = Thread.class.getDeclaredField("inheritedAccessControlContext");
            field.setAccessible(true);
            field.set(listener, new AccessControlContext(new ProtectionDomain[0]));
            field = URLClassLoader.class.getDeclaredField("acc");
            field.setAccessible(true);
            field.set(getClass().getClassLoader(), new AccessControlContext(new ProtectionDomain[0]));
        } catch(Exception x) {
            LOG.log(Level.WARNING, "Can't reset AccessControlContext", x);
        }
    }
    
    public int getPort() {
        return port;
    }
    
    
    public synchronized void reload(String workDir) {
        try {Thread.sleep(500);} catch(InterruptedException x) {}
        ObjectName objectName = getWebModuleObjectName(workDir);
        if(objectName==null) throw new PustefixRuntimeException("Can't reload webapp because "
                +"no WebModule MBean could be found.");
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mbeanServer.invoke(objectName,"reload",new Object[0],new String[0]);
        } catch(Exception x) {
            throw new PustefixRuntimeException("Can't reload webapp.",x);
        }
    }
    
    private ObjectName getWebModuleObjectName(String workDir) {
        try {
            Hashtable<String, String> props = new Hashtable<String,String>();
            props.put("J2EEApplication","none");
            props.put("J2EEServer","none");
            props.put("j2eeType","WebModule");
            props.put("name","//"+"*"+"/*");
            ObjectName objectNamePattern = new ObjectName("*",props);
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            if(mbeanServer!=null) {
                Set<ObjectName> objectNames = mbeanServer.queryNames(objectNamePattern, null);
                for(ObjectName objectName:objectNames) {
                    String workDirAttr =(String)mbeanServer.getAttribute(objectName, "workDir");
                    if(workDir.endsWith(workDirAttr)) return objectName;
                }
            } 
            return null;
        } catch(Exception x) {
            throw new PustefixRuntimeException("Error while trying to find WebModule mbean name.",x);
        } 
    }
    
    class ListenerThread extends Thread {
        
        int port;
        
        ListenerThread(int port) {
            super("Pustefix-Admin-Listener");
            this.port = port;
        }
        
        @Override
        public void run() {
            
            ServerSocket serverSock = null;
            try {
                serverSock = new ServerSocket(port);
            } catch(IOException x) {
                LOG.log(Level.SEVERE, "Can't listen on port " + port, x);
                throw new RuntimeException("Can't listen on port " + port, x);
            }
            while(!isInterrupted()) {
                try {
                    Socket sock = serverSock.accept();
                    ClientThread client = new ClientThread(sock);
                    client.start(); 
                } catch(IOException x) {
                    LOG.log(Level.SEVERE, "I/O error while handling connection", x);
                    throw new RuntimeException("I/O error while handling connection", x);
                }
            }
        }
        
    }
    
    class ClientThread extends Thread {
        
        Socket sock;
        
        ClientThread(Socket sock) {
            super("Pustefix-Admin-Client");
            this.sock = sock;
        }
        
        @Override
        public void run() {
            try {
                InputStream in = sock.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                String cmd = null;
                List<String> params = new ArrayList<String>();
                String line = null;
                while((line = reader.readLine()) != null) {
                    if(cmd == null) {
                        cmd = line;
                    } else {
                        params.add(line);
                    }
                }
                if(cmd == null) {
                    LOG.log(Level.SEVERE, "Missing command.");
                } else if(cmd.equals("reload")) {
                    if(params.size() == 1) {
                        reload(params.get(0));
                    } else {
                        LOG.log(Level.SEVERE, "Command 'reload' called with " + params.size() + " params.");
                    }
                } else {
                    LOG.log(Level.SEVERE, "Command '" + cmd + "' not supported.");
                }
                sock.close();
            } catch(IOException x) {
                LOG.log(Level.SEVERE, "I/O error during admin request", x);
            }
        }
        
    }
    
}
