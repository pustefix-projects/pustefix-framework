/*
 * Created on 10.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.schlund.pfixxml.perflogging;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;

/**
 * @author jh
 *
 */
class BoundedBufferWrapper {
    private BoundedBuffer boundedBuffer;
    private int size;
    private int ms;
    /**
     * 
     */
    /**
     * 
     */
    BoundedBufferWrapper(int size, int ms) {
        this.size = size;
        this.ms = ms;
    }
    
    boolean offer(Object arg) throws InterruptedException {
        return boundedBuffer.offer(arg, ms);
    }
    
    Object take() throws InterruptedException {
        return boundedBuffer.take();
    }
    
    void init() {
        boundedBuffer = new BoundedBuffer(size);
    }
    
    void reset() {
        boundedBuffer = null;
    }
    
    int size() {
        return boundedBuffer.size();
    }
}
