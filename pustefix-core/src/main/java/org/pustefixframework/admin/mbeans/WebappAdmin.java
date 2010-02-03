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
package org.pustefixframework.admin.mbeans;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.pustefixframework.container.spring.beans.PustefixWebApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.schlund.pfixcore.exception.PustefixRuntimeException;

/**
 * 
 * @author mleidig
 *
 */
public class WebappAdmin implements WebappAdminMBean, InitializingBean, DisposableBean, ApplicationContextAware {
    
    private Logger LOG = Logger.getLogger(WebappAdmin.class);
    
    private String projectName;
    private ApplicationContext applicationContext;
   
    private boolean refreshTriggered;
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    public void afterPropertiesSet() throws Exception {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=WebappAdmin,project="+projectName);
            if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register WebappAdmin MBean!",x);
        } 
    }
    
    public void destroy() throws Exception {
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=WebappAdmin,project="+projectName);
            if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
        } catch(Exception x) {
            LOG.error("Can't unregister WebappAdmin MBean!",x);
        } 
    }
    
    public synchronized void reload()  {
        long t1 = System.currentTimeMillis();
        //we have to reset log4j here, as it often breaks Tomcat reloading
        Logger.getRootLogger().removeAllAppenders();
        ObjectName objectName = getWebModuleObjectName(projectName);
        if(objectName==null) throw new PustefixRuntimeException("Can't reload webapp because "
                +"no WebModule MBean could be found for project '"+projectName+"'.");
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            mbeanServer.invoke(objectName,"reload",new Object[0],new String[0]);
        } catch(Exception x) {
            throw new PustefixRuntimeException("Can't reload webapp.",x);
        }
        long t2 = System.currentTimeMillis();
        LOG.info("Reloaded webapp of project '"+projectName+"' ["+(t2-t1)+"ms]");
    }
    
    private ObjectName getWebModuleObjectName(String projectName) {
        try {
            Hashtable<String, String> props = new Hashtable<String,String>();
            props.put("J2EEApplication","none");
            props.put("J2EEServer","none");
            props.put("j2eeType","WebModule");
            props.put("name","//"+"*"+"/");
            ObjectName objectNamePattern = new ObjectName("Catalina",props);
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
            if(mbeanServer!=null) {
                Set<ObjectName> objectNames = mbeanServer.queryNames(objectNamePattern, null);
                for(ObjectName objectName:objectNames) {
                    String docBase =(String)mbeanServer.getAttribute(objectName,"docBase");
                    if(docBase!=null && docBase.endsWith(projectName)) return objectName;
                }
            } 
            return null;
        } catch(Exception x) {
            throw new PustefixRuntimeException("Error while trying to find WebModule mbean name.",x);
        } 
    }
    
    public synchronized void refresh() {
        refreshTriggered = true;
    }
    
    public synchronized void refreshIfTriggered() {
        if(refreshTriggered) doRefresh();
    }
    
    private void doRefresh() {
        long t1 = System.currentTimeMillis();
        ((PustefixWebApplicationContext)applicationContext).refresh();
        refreshTriggered = false;
        long t2 = System.currentTimeMillis();
        LOG.info("Refreshed ApplicationContext of project '"+projectName+"' ["+(t2-t1)+"ms]");
    }

}
