package org.pustefixframework.admin.mbeans;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;


import de.schlund.pfixcore.exception.PustefixRuntimeException;

/**
 * MBean providing webapp administration methods.
 * 
 * @author mleidig@schlund.de
 *
 */
public class Admin implements AdminMBean, NotificationListener {
    
    public final static String JMX_NAME = "Pustefix:type=Admin";
    
    private Logger LOG = Logger.getLogger(Admin.class.getName());
    
    public Admin() {
        try {
            AdminBroadcaster ab = new AdminBroadcaster();
            ObjectName on = new ObjectName(AdminBroadcaster.JMX_NAME);
            ManagementFactory.getPlatformMBeanServer().registerMBean(ab, on);
            ManagementFactory.getPlatformMBeanServer().addNotificationListener(on, this, null, null);
        } catch(Exception x) {
            LOG.log(Level.SEVERE, "Can't register AdminBroadcaster MBean", x);
        }    
    }
    
    public synchronized void handleNotification(Notification notification, Object handback) {
        if(notification.getType().equals(AdminBroadcaster.NOTIFICATION_TYPE_RELOAD)) {
            String realPath = (String)notification.getUserData();
            reload(realPath);
        }
    }
    
   
    
    public synchronized void reload(String workDir)  {
        
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
    
}
