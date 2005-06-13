/*
 * Created on 08.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import org.apache.log4j.Logger;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;

/**
 * @author jh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PerfEventTakeThread extends Thread {
    private static Logger LOG = Logger.getLogger(PerfEventTakeThread.class);
    private BoundedBufferWrapper bBuffer;
    
    public PerfEventTakeThread(BoundedBufferWrapper b) {
        bBuffer = b;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        int bufsize = 0;
        while(true) {
            bufsize = bBuffer.size();
            try {
                PerfEvent pe = (PerfEvent) bBuffer.take();
                LOG.info("Took ("+pe+") from channel. Buffersize: "+bufsize);
                Thread.currentThread().sleep(20);
                PerfStatistic.getInstance().process(pe);
            } catch (InterruptedException e) {
                LOG.warn("Perflogging may be disabled. Buffersize: "+bufsize, e);
                return;
            }
        }
    
    }

}
