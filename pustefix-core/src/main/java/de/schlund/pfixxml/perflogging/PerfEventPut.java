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
class PerfEventPut {
    private static PerfEventPut instance = new PerfEventPut();
    private final static Logger LOG = Logger.getLogger(PerfEventPut.class);
    
    private BoundedBufferWrapper bBuffer;
    
    private PerfEventPut() {

    }
    
    static PerfEventPut getInstance() {
        return instance;
    }

    void setBuffer(BoundedBufferWrapper b) {
        bBuffer = b;
    }
    
    
    void logPerf(PerfEvent pe) {
        
        try {
            LOG.info("Putting ("+pe+") into buffer. Buffersize: "+bBuffer.size());
            boolean ok = bBuffer.offer(pe);
            LOG.info("Putting succeeded: "+ok);
          
        } catch (InterruptedException e) {
            LOG.warn(e);
        }
    }

   

  
}
