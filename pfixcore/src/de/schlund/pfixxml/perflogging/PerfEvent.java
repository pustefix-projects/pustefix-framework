/*
 * Created on 27.05.2005
 *
 */
package de.schlund.pfixxml.perflogging;

import org.apache.log4j.Logger;


/**
 * @author jh
 *
 */
public class PerfEvent {
    private static Logger LOG = Logger.getLogger(PerfEvent.class);
    private String category;
    private String identifier;
    private long duration;
    private long starttime;
 
       
    public PerfEvent(String category, String identifier) {
         this.category = category;
         this.identifier = identifier;
     }
    
    public PerfEvent(String category) {
        this.category = category;
    }
    
     public String getCategory() {
         return category;
     }
     
     public String getIdentifier() {
         return identifier;
     }
    
     public void setIdentfier(String identifier) {
         this.identifier = identifier;
     }
     
    
     
     public void start() {
         if(!isActive()) return;
         starttime = System.currentTimeMillis(); 
     }
     
     public void save() {
         if(!isActive()) return;
         stop();
         doSave();
     }
     
     public void stop() {
         if(!isActive()) return;
         duration += System.currentTimeMillis() - starttime;
     }
     
     
     private void doSave() {
         if(!isActive()) return;
         PerfEventPut.getInstance().logPerf(this);
     }
         
  
     long getDuration() {
         return duration;
     }
     
   
     public String toString() {
         return category+"|"+identifier+":"+duration;
     }
     
     String getKey() {
         return category+"_"+identifier;
     }
     
     private boolean isActive() {
         PerfLogging perf = PerfLogging.getInstance();
         boolean ret1 = perf.isPerfLogggingEnabled();
         boolean ret2 = perf.isPerfLoggingActive();
         if(LOG.isDebugEnabled())
             LOG.debug("Enabled: "+ret1+"  Active: "+ret2);
         return ret1 && ret2;
     }
     
}
