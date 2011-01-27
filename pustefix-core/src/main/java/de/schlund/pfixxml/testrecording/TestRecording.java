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
package de.schlund.pfixxml.testrecording;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionData;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import de.schlund.pfixxml.util.Xml;

public class TestRecording implements TestRecordingMBean, InitializingBean, DisposableBean {

   private final static Logger LOG=Logger.getLogger(TestRecording.class);
   
   private final List<String> knownClients=new ArrayList<String>();
   
   private SessionAdmin sessionAdmin;
   private String projectName;
   
   
   public void afterPropertiesSet() throws Exception {
      LOG.info("TestRecording init");
      try {
         MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
         ObjectName objectName = new ObjectName("Pustefix:type=TestRecording,project="+projectName);
         if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
         mbeanServer.registerMBean(this, objectName);
     } catch(Exception x) {
         throw new RuntimeException("Can't register TestRecording MBean.",x);
     } 
   }

    public void destroy() throws Exception {
       try {
           MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
           ObjectName objectName = new ObjectName("Pustefix:type=TestRecording,project="+projectName);
           if(mbeanServer.isRegistered(objectName)) mbeanServer.unregisterMBean(objectName);
       } catch(Exception x) {
           throw new RuntimeException("Can't unregister TestRecording MBean.",x);
       } 
    }
   
   public void setProjectName(String projectName) {
       this.projectName = projectName;
   }
   
   public void setSessionAdmin(SessionAdmin sessionAdmin) {
       this.sessionAdmin = sessionAdmin;
   }
   
   // TODO: who's responsible for unregister?
   public ObjectName startRecording(String sessionId) throws IOException {
     ObjectName name;
     TrailLogger logger;
     
     logger = new TrailLogger(TrailLogger.getVisit(getSession(sessionId)));
     
     try {
      name = new ObjectName("Pustefix:type=TestRecording,name=TrailLogger,project="+projectName+",session="+sessionId);
      MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
      if(mbeanServer.isRegistered(name)) mbeanServer.unregisterMBean(name);
      mbeanServer.registerMBean(logger, name);
      return name;
     } catch(Exception x) {
        LOG.error("Can't register TrailLogger MBean!",x);
        throw new RuntimeException("Can't register TrailLogger MBean!",x);
     } 
   
   }
   
   public ApplicationList getApplicationList(boolean tomcat, String sessionSuffix) {
      FileResource file;
      
      file = ResourceUtil.getFileResourceFromDocroot("WEB-INF" + File.separator + "projects.xml");
      try {
          return ApplicationList.load(Xml.parseMutable(file), tomcat, sessionSuffix);
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
  }
   
   public List<SessionData> getSessions(String serverName, String remoteAddr) {
      Iterator<String> iter;
      String id;
      List<SessionData> lst;
      SessionInfoStruct info;
      
      lst = new ArrayList<SessionData>();
      iter = sessionAdmin.getAllSessionIds().iterator();
      while (iter.hasNext()) {
          id = (String) iter.next();
          info = sessionAdmin.getInfo(id);
          if (serverName.equals(info.getData().getServerName()) && remoteAddr.equals(info.getData().getRemoteAddr())) {
              lst.add(info.getData());
          }
      }
      return lst;
  }
   
   public void invalidateSession(String id) throws IOException {
      getSession(id).invalidate();
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
   
   public HttpSession getSession(String id) throws IOException {
      SessionInfoStruct info;
      
      info = sessionAdmin.getInfo(id);
      if (info == null) {
          throw new IOException("session not found: " + id);
      }
      return info.getSession();
  }
   
}
