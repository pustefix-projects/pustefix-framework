/*
 * Created on 08.06.2005
 *
 */
package de.schlund.pfixxml.perflogging;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author jh
 *
 */
public class PerfLogging extends NotificationBroadcasterSupport implements PerfLoggingMBean, InitializingBean {
    
    private final static Logger LOG = Logger.getLogger(PerfLogging.class);
    
    private static ThreadLocal<PerfLogging> instance = new ThreadLocal<PerfLogging>();
    
    private int DEFAULT_BUFFER_SIZE = 1000;
    private int DEFAULT_OFFER_MAX_WAIT = 5;
    private boolean enabled = false;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private int offerMaxWait = DEFAULT_OFFER_MAX_WAIT;
    private boolean autoStart;
    private boolean perfActive = false;
    private String projectName;
    private BoundedBufferWrapper boundedBuffer;
    private PerfEventTakeThread perfEventTakeThread;
    private long seqNo;
    
    private PerfStatistic perfStatistic;
    
    public static PerfLogging getInstanceForThread() {
        return instance.get();
    }
    
    public static void setInstanceForThread(PerfLogging perfLogging) {
        instance.set(perfLogging);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        System.out.println("setEnabled "+enabled);
    }
    
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    public void setOfferMaxWait(int offerMaxWait) {
        this.offerMaxWait = offerMaxWait;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    
    public void afterPropertiesSet() throws Exception {
        LOG.info("Perflogging init");
        
        boundedBuffer = new BoundedBufferWrapper(bufferSize, offerMaxWait);
        
        perfStatistic = new PerfStatistic(this);
        
        if(autoStart) {
            activatePerflogging();
        } else {
            perfActive = false;
        }
        
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=PerfLogging,project="+projectName); 
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register PerfLogging MBean!",x);
        } 
        
        if(LOG.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("After init: \n").
                append("Enabled: "+enabled+"\n").
                append("Active: "+perfActive+"\n").
                append("Buffersize: "+bufferSize+"\n").
                append("Bufferwait: "+offerMaxWait+"\n");
            LOG.info(sb.toString());
               
        }
        
    }
    
    public boolean isPerfLogggingEnabled() {
        return enabled;
    }
    
    public boolean isPerfLoggingActive() {
        return perfActive;
    }
    
    public synchronized void activatePerflogging() {
        if(!enabled) {
            LOG.warn("Perflogging is disabled");
            return;
        }
        if(!perfActive) {
            LOG.info("Activating perflogging");
            boundedBuffer.init();
            PerfEventPut.getInstance().setBuffer(boundedBuffer);
            startPerfEventTakeThread();
            perfActive = true;
            LOG.info("Perflogging now active");
        } else {
            LOG.info("perflogging already active");
        }
    }
    
    public synchronized String inactivatePerflogging() {
        if(!enabled) {
            LOG.warn("Perflogging is disabled");
            return null;
        }
        if(perfActive) {
            LOG.info("Inactivating perflogging");
            perfActive = false;
            String xml = perfStatistic.toXML();
            perfStatistic.reset();
            stopPerfEventTakeThread();
            boundedBuffer.reset();
            return xml;
        } else {
            LOG.info("perflogging already inactive");
            return null;
        }
    }
    
    public synchronized Map<String, Map<String, int[]>> inactivatePerfloggingMap() {
        if(!enabled) {
            LOG.warn("Perflogging is disabled");
            return null;
        }
        if(perfActive) {
            LOG.info("Inactivating perflogging");
            perfActive = false;
            Map<String, Map<String, int[]>> map = perfStatistic.toMap();
            perfStatistic.reset();
            stopPerfEventTakeThread();
            boundedBuffer.reset();
            return map;
        } else {
            LOG.info("perflogging already inactive");
            return null;
        }
    }
    
    private void startPerfEventTakeThread() {
        LOG.info("Starting new Take-Thread for Buffer");
        perfEventTakeThread = new PerfEventTakeThread(boundedBuffer, perfStatistic);
        perfEventTakeThread.start();
    }
    
    private void stopPerfEventTakeThread() {
        LOG.info("Interrupting existing Take-Thread for Buffer");
        perfEventTakeThread.interrupt();
    }
    
  
    //accessible via JMX:
    
    public synchronized boolean isPerfLoggingEnabled() {
        return isPerfLogggingEnabled();
    }
    
    public synchronized boolean isPerfLoggingRunning() {
        return isPerfLoggingActive();
    }
    
    public synchronized void startPerfLogging() {
        if(!isPerfLoggingActive()) {
            try {
                activatePerflogging();
            } finally {
                if(isPerfLoggingActive()) {
                    Notification n= new Notification("PerfLogging.started",this,seqNo++, 
                            System.currentTimeMillis(),"Started performance logging");
                    sendNotification(n);
                }
            }
        } else throw new IllegalStateException("Performance logging is already running.");
    }
    
    public synchronized String stopPerfLogging() {
        if(isPerfLoggingActive()) {
            try {
                return inactivatePerflogging();
            } finally {
                if(!isPerfLoggingActive()) {
                    Notification n= new Notification("PerfLogging.stopped",this,seqNo++, 
                            System.currentTimeMillis(),"Stopped performance logging");
                    sendNotification(n);
                }
            }
        } else throw new IllegalStateException("Performance logging isn't running.");
    }
    
    public synchronized Map<String, Map<String, int[]>> stopPerfLoggingMap() {
        if(isPerfLoggingActive()) {
            try {
                return inactivatePerfloggingMap();
            } finally {
                if(!isPerfLoggingActive()) {
                    Notification n= new Notification("PerfLogging.stopped",this,seqNo++, 
                            System.currentTimeMillis(),"Stopped performance logging");
                    sendNotification(n);
                }
            }
        } else throw new IllegalStateException("Performance logging isn't running.");
    }
 
}
