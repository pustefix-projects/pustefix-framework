package org.pustefixframework.webservices;

import de.schlund.pfixxml.perflogging.PerfEvent;
import de.schlund.pfixxml.perflogging.PerfEventType;

public class ProcessingInfo {

    String service;
    String method;
    long startTime;
    
    long invocTime=-1;
    PerfEvent invocEvent;
    
    long procTime=-1;
    PerfEvent procEvent;
    
    public ProcessingInfo(String service,String method) {
        this.service=service;
        this.method=method;
    }
    
    public void setService(String service) {
        this.service=service;
    }
    
    public String getService() {
        return service;
    }
    
    public void setMethod(String method) {
        this.method=method;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setStartTime(long startTime) {
        this.startTime=startTime;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void startInvocation() {
        invocEvent=new PerfEvent(PerfEventType.WEBSERVICE_INVOCATION);
        invocEvent.setIdentfier(service+"."+method);
        invocEvent.start();
        invocTime=System.currentTimeMillis();
    }
    public void endInvocation() {
        invocEvent.save();
        invocTime=System.currentTimeMillis()-invocTime;
    }
    
    public long getInvocationTime() {
        return invocTime;
    }
    
    public void startProcessing() {
        procEvent=new PerfEvent(PerfEventType.WEBSERVICE_PROCESSING);
        procEvent.setIdentfier(service+"."+method);
        procEvent.start();
        procTime=System.currentTimeMillis();
    }
    public void endProcessing() {
        procEvent.save();
        procTime=System.currentTimeMillis()-procTime;
    }

    public long getProcessingTime() {
        return procTime;
    }
    
}
