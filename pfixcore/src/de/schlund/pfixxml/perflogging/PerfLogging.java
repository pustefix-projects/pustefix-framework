/*
 * Created on 08.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import java.util.Map;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;


/**
 * @author jh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PerfLogging {
    private static PerfLogging instance = new PerfLogging();
    private static Logger LOG = Logger.getLogger(PerfLogging.class);
    private String PROP_BUFFER_SIZE = "perflogging.buffersize";
    private String PROP_LOG_WRITE = "perflogging.logwrite";
    private String PROP_ENABLED = "perflogging.enabled";
    private String PROP_AUTOSTART = "perflogging.autostart";
    private String PROP_OFFER_MAX_WAIT = "perflogging.offermaxwait";
    
    private String OFF = "0";
    private String ON = "1";
    private int DEFAULT_BUFFER_SIZE = 1000;
    private int DEFAULT_OFFER_MAX_WAIT = 5;
    private int DEFAULT_LOG_WRITE = 1000 * 60 * 10; //10 min
    private boolean perfLoggingEnabled = false;
    private boolean perfActive = false;
    private BoundedBufferWrapper boundedBuffer;
    private PerfEventTakeThread perfEventTakeThread;
  
    
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
    
    public synchronized Map inactivatePerfloggingMap() {
        if(!perfLoggingEnabled) {
            LOG.warn("Perflogging is disabled");
            return null;
        }
        if(perfActive) {
            LOG.info("Inactivating perflogging");
            perfActive = false;
            Map map = PerfStatistic.getInstance().toMap();
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
    
  
    
 
}
