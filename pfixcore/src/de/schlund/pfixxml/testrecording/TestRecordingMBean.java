package de.schlund.pfixxml.testrecording;

import java.io.IOException;
import java.util.List;

import javax.management.ObjectName;

import de.schlund.pfixxml.serverutil.SessionData;

public interface TestRecordingMBean {

   public ObjectName startRecording(String sessionId) throws IOException;
   public ApplicationList getApplicationList(boolean tomcat, String sessionSuffix);
   public List<SessionData> getSessions(String serverName, String remoteAddr);
   public void invalidateSession(String id) throws IOException;
   public boolean isKnownClient(String remoteAddr);
   public void addKnownClient(String remoteAddr);
   public void removeKnownClient(String remoteAddr);
   
}
