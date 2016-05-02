package org.pustefixframework.xml.tools;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.schlund.pfixxml.util.SimpleCacheLRU;

/**
 * Manages XSL transformation tracing, its settings and trace data.
 */
public class XSLTracing {
    
    private static XSLTracing instance = new XSLTracing();
    
    private boolean enabled = false;
    private long instructionGranularity = 100000;
    private int traceCacheSize = 10;
    private boolean recordAttributes = false;
    private Map<String, XSLTrace> systemIdToTrace = new SimpleCacheLRU<>(traceCacheSize);
    
    public synchronized void addTrace(XSLTrace trace) {
        systemIdToTrace.put(trace.getId(), trace);
    }
    
    public synchronized Set<String> getTraceIds() {
        return new TreeSet<>(systemIdToTrace.keySet());
    }
    
    public synchronized XSLTrace getTrace(String traceId) {
        return systemIdToTrace.get(traceId);
    }
    
    public synchronized long getInstructionGranularity() {
        return instructionGranularity;
    }
    
    public synchronized void setInstructionGranularity(long instructionGranularity) {
        this.instructionGranularity = instructionGranularity;
    }
    
    public synchronized int getTraceCacheSize() {
        return traceCacheSize;
    }
    
    public synchronized void setTraceCacheSize(int traceCacheSize) {
        this.traceCacheSize = traceCacheSize;
        Map<String, XSLTrace> tmp = new SimpleCacheLRU<>(traceCacheSize);
        tmp.putAll(systemIdToTrace);
        systemIdToTrace = tmp;
    }
    
    public synchronized boolean isEnabled() {
        return enabled;
    }
    
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public synchronized boolean recordAttributes() {
        return recordAttributes;
    }
    
    public synchronized void setRecordAttributes(boolean recordAttributes) {
        this.recordAttributes = recordAttributes;
    }
    
    public static XSLTracing getInstance() {
        return instance;
    }
    
}
