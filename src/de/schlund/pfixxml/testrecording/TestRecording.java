package de.schlund.pfixxml.testrecording;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionData;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;
import de.schlund.pfixxml.util.Xml;

public class TestRecording implements TestRecordingMBean {

   private final static Logger LOG=Logger.getLogger(TestRecording.class);
   
   private static TestRecording instance=new TestRecording();
   
   private final List<String> knownClients=new ArrayList<String>();
   
   public static TestRecording getInstance() {
      return instance;
   }
   
   public void init(Properties props) { 
      LOG.info("TestRecording init");
      try {
         MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
         ObjectName objectName = new ObjectName("Pustefix:type=TestRecording"); 
         mbeanServer.registerMBean(this, objectName);
      // otherwise, clients cannot instaniate TrailLogger objects:
         ObjectName createName = new ObjectName("Pustefix:type=TestRecording,name=loader");
         mbeanServer.registerMBean(this.getClass().getClassLoader(),createName);
     } catch(Exception x) {
         throw new RuntimeException("Can't register TestRecording MBean.",x);
     } 
   }
   
   public TestRecording() {
    
   }
   
   // TODO: who's responsible for unregister?
   public ObjectName startRecording(String sessionId) throws IOException {
     ObjectName name;
     TrailLogger logger;
     
     logger = new TrailLogger(TrailLogger.getVisit(getSession(sessionId)));
     
     try {
      name = new ObjectName("Pustefix:type=TestRecording,name=TrailLogger,session="+sessionId);
      MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
      mbeanServer.registerMBean(logger, name);
      return name;
     } catch(Exception x) {
        LOG.error("Can't register TrailLogger MBean!",x);
        throw new RuntimeException("Can't register TrailLogger MBean!",x);
     } 
   
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
   
   public List<SessionData> getSessions(String serverName, String remoteAddr) {
      SessionAdmin admin;
      Iterator<String> iter;
      String id;
      List<SessionData> lst;
      SessionInfoStruct info;
      
      lst = new ArrayList<SessionData>();
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
   
   public boolean isKnownClient(String remoteAddr) {
      return knownClients.contains(remoteAddr);
  }

  public void addKnownClient(String remoteAddr) {
      knownClients.add(remoteAddr);
  }
  
  public void removeKnownClient(String remoteAddr) {
      knownClients.remove(remoteAddr);
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
