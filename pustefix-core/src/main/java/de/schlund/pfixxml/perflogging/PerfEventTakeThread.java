/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
    @Override
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
