/*
 * Created on 08.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;


/**
 * @author jh
 *
 */
public class PerfLogging extends NotificationBroadcasterSupport implements PerfLoggingMBean {
    
    private static PerfLogging instance = new PerfLogging();
    private static Logger LOG = Logger.getLogger(PerfLogging.class);
    private String PROP_BUFFER_SIZE = "perflogging.buffersize";
    private String PROP_ENABLED = "perflogging.enabled";
    private String PROP_AUTOSTART = "perflogging.autostart";
    private String PROP_OFFER_MAX_WAIT = "perflogging.offermaxwait";
    
    private String OFF = "0";
    private String ON = "1";
    private int DEFAULT_BUFFER_SIZE = 1000;
    private int DEFAULT_OFFER_MAX_WAIT = 5;
    private boolean perfLoggingEnabled = false;
    private boolean perfActive = false;
    private BoundedBufferWrapper boundedBuffer;
    private PerfEventTakeThread perfEventTakeThread;
    private long seqNo;
    
    private PerfLogging() {}
    
    public static PerfLogging getInstance() {
        return instance;
    }
    
    public void init(Properties props) {
        
        LOG.info("Perflogging init");
        
        String prop_enabled = props.getProperty(PROP_ENABLED, OFF);
        if(prop_enabled.equals(ON)) {
            perfLoggingEnabled = true;
        } else {
            perfLoggingEnabled = false;
        }
        LOG.info("Perflogging enabled: "+perfLoggingEnabled);
        if(!perfLoggingEnabled) return;
        
        
        String prop_buffs = props.getProperty(PROP_BUFFER_SIZE);
        int size = 0;
        if(prop_buffs == null || prop_buffs.length() < 1 ) {
            LOG.warn("Property "+PROP_BUFFER_SIZE+" not found. Using default: "+DEFAULT_BUFFER_SIZE);
            size = DEFAULT_BUFFER_SIZE;
        } else {
            size = Integer.parseInt(prop_buffs);
        }
        
        String prop_offerwait = props.getProperty(PROP_OFFER_MAX_WAIT);
        int wait = 0;
        if(prop_offerwait == null || prop_offerwait.length() < 1 ) {
            LOG.warn("Property "+PROP_OFFER_MAX_WAIT+" not found. Using default: "+DEFAULT_OFFER_MAX_WAIT);
            wait = DEFAULT_OFFER_MAX_WAIT;
        } else {
            wait = Integer.parseInt(prop_offerwait);
        }
        boundedBuffer = new BoundedBufferWrapper(size, wait);
        
        String prop_autostart = props.getProperty(PROP_AUTOSTART, OFF);
        
        if(LOG.isInfoEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("After init: \n").
                append("Enabled: "+perfLoggingEnabled+"\n").
                append("Active: "+perfActive+"\n").
                append("Buffersize: "+size+"\n").
                append("Bufferwait: "+wait+"\n");
            LOG.info(sb.toString());
               
        }
        
        if(prop_autostart.equals(ON)) {
            activatePerflogging();
        } else {
            perfActive = false;
        }
        
        try {
            MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer(); 
            ObjectName objectName = new ObjectName("Pustefix:type=PerfLogging"); 
            mbeanServer.registerMBean(this, objectName);
        } catch(Exception x) {
            LOG.error("Can't register PerfLogging MBean!",x);
        } 
        
    }
    
    public boolean isPerfLogggingEnabled() {
        return perfLoggingEnabled;
    }
    
    public boolean isPerfLoggingActive() {
        return perfActive;
    }
    
    public synchronized void activatePerflogging() {
        if(!perfLoggingEnabled) {
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
        if(!perfLoggingEnabled) {
            LOG.warn("Perflogging is disabled");
            return null;
        }
        if(perfActive) {
            LOG.info("Inactivating perflogging");
            perfActive = false;
            String xml = PerfStatistic.getInstance().toXML();
            PerfStatistic.getInstance().reset();
            stopPerfEventTakeThread();
            boundedBuffer.reset();
            return xml;
        } else {
            LOG.info("perflogging already inactive");
            return null;
        }
    }
    
    public synchronized Map<String, Map<String, int[]>> inactivatePerfloggingMap() {
        if(!perfLoggingEnabled) {
            LOG.warn("Perflogging is disabled");
            return null;
        }
        if(perfActive) {
            LOG.info("Inactivating perflogging");
            perfActive = false;
            Map<String, Map<String, int[]>> map = PerfStatistic.getInstance().toMap();
            PerfStatistic.getInstance().reset();
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
        perfEventTakeThread = new PerfEventTakeThread(boundedBuffer);
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
