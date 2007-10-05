/*
 * Created on 08.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * @author jh
 *
 */
public class LogFileWriterThread extends TimerTask {
    private static Logger LOG = Logger.getLogger(LogFileWriterThread.class);
   
    public void run() {
        
        if(! (PerfLogging.getInstance().isPerfLogggingEnabled() &&
                PerfLogging.getInstance().isPerfLoggingActive())) {
            LOG.info("PerfLogging not active");
        }
        
        final String str = PerfStatistic.getInstance().toLogfilePresentation();
        if(str != null && str.length() > 0) {
            LOG.info(str);
        }
    }

}
