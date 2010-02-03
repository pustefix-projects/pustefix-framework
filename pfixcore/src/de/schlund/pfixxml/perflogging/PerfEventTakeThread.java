/*
 * Created on 08.06.2005
 *
 */
package de.schlund.pfixxml.perflogging;

import org.apache.log4j.Logger;

/**
 * @author jh
 *
 */
public class PerfEventTakeThread extends Thread {

    private final static Logger LOG = Logger.getLogger(PerfEventTakeThread.class);
    
    private BoundedBufferWrapper bBuffer;
    private PerfStatistic perfStatistic;
 
    public PerfEventTakeThread(BoundedBufferWrapper b, PerfStatistic perfStatistic) {
        super("PerfEventTakeThread");
        bBuffer = b;
        this.perfStatistic = perfStatistic;
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
                //testing: Thread.currentThread().sleep(20);
                perfStatistic.process(pe);
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException. Perflogging may be disabled. Buffersize: "+bufsize);
                return;
            }
        }
    
    }

}
