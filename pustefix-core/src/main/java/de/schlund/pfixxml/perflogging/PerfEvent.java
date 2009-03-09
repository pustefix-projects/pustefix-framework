/*
 * Created on 27.05.2005
 *
 */
package de.schlund.pfixxml.perflogging;



/**
 * @author jh
 *
 */
public class PerfEvent {
    
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
     
   
     @Override
     public String toString() {
         return category+"|"+identifier+":"+duration;
     }
     
     String getKey() {
         return category+"_"+identifier;
     }
     
     private boolean isActive() {
         PerfLogging perf = PerfLogging.getInstanceForThread();
         if(perf != null) {
             return perf.isPerfLoggingActive();
         }
         return false;
     }
     
}
