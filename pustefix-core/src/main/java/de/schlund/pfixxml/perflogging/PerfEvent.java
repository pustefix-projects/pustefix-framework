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
