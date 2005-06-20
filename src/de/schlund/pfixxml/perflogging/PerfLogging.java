/*
 * Created on 08.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

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
    private boolean perfActive;
    private BoundedBufferWrapper boundedBuffer;
    private PerfEventTakeThread perfEventTakeThread;
    private LogFileWriterThread logFileWriterThread;
    private Timer logFileWriterThreadTimer;
    private int logFileWriterThreadSchedule = 0;
    
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
        
        
        String prop_autostart = props.getProperty(PROP_AUTOSTART);
        if(prop_autostart.equals(ON)) {
            perfActive = true;
            activatePerflogging();
        } else {
            perfActive = false;
        }
        LOG.info("Perflogging active: "+perfActive);
        
        
        String prop_logwrite = props.getProperty(PROP_LOG_WRITE);
        
        if(prop_logwrite == null || prop_logwrite.length() < 1 ) {
            LOG.warn("Property "+PROP_LOG_WRITE+" not found. Using default: "+DEFAULT_LOG_WRITE);
            logFileWriterThreadSchedule = DEFAULT_LOG_WRITE;
        } else {
            logFileWriterThreadSchedule = Integer.parseInt(prop_logwrite);
        }
    }
    
    synchronized boolean isPerfLogggingEnabled() {
        return perfLoggingEnabled;
    }
    
    synchronized boolean isPerfLoggingActive() {
        return perfActive;
    }
    
    synchronized void activatePerflogging() {
        if(!perfLoggingEnabled) {
            LOG.warn("Perflogging is disabled");
        }
        if(!perfActive) {
            LOG.info("Activating perflogging");
            boundedBuffer.init();
            PerfEventPut.getInstance().setBuffer(boundedBuffer);
            startPerfEventTakeThread();
            startLogFileWriterThread();
            perfActive = true;
        } else {
            LOG.info("perflogging already active");
        }
    }
    
    synchronized void inactivatePerflogging() {
        if(!perfLoggingEnabled) {
            LOG.warn("Perflogging is disabled");
        }
        if(perfActive) {
            LOG.info("Inactivating perflogging");
            PerfStatistic.getInstance().reset();
            stopPerfEventTakeThread();
            stopLogFileWriterThread();
            perfActive = false;
            boundedBuffer.reset();
            
        } else {
            LOG.info("perflogging already inactive");
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
    
    private void startLogFileWriterThread() {
        LOG.info("Scheduling logFileWriterThread. New timer and new thread");
        logFileWriterThreadTimer = new Timer();
        logFileWriterThread = new LogFileWriterThread();
        logFileWriterThreadTimer.schedule(logFileWriterThread, 
                logFileWriterThreadSchedule, logFileWriterThreadSchedule);
    }
    
    private void stopLogFileWriterThread() {
        LOG.info("Canceling existing timer for logFileWriterThread");
        logFileWriterThreadTimer.cancel();
    }
    
 
}
