/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml;


/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PerfEventType {
    
    //TODO: this is clumsy. What happens if this class is loaded before
    // factory init (PerfTypeConfig) is finished??? Can this happen?
    private static final String IHANDLER_ISACTIVE_TAG = "IHANDLER_IS_ACTIVE";
    public static final PerfEventType IHANDLER_ISACTIVE = 
        new PerfEventType(IHANDLER_ISACTIVE_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(IHANDLER_ISACTIVE_TAG));
    
    private static final String IHANDLER_NEEDSDATA_TAG = "IHANDLER_NEEDS_DATA";
    public static final PerfEventType IHANDLER_NEEDSDATA = 
        new PerfEventType(IHANDLER_NEEDSDATA_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(IHANDLER_NEEDSDATA_TAG));
    
    private static final String IHANDLER_PREREQUISITESMET_TAG = "IHANDLER_PREREQUISITES_MET";
    public static final PerfEventType IHANDLER_PREREQUISITESMET = 
        new PerfEventType(IHANDLER_PREREQUISITESMET_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(IHANDLER_PREREQUISITESMET_TAG));
    
    
    private static final String CONTEXTRESOURCE_INSERTSTATUS_TAG = "CONTEXTRESOURCE_INSERT_STATUS";
    public static final PerfEventType CONTEXTRESOURCE_INSERTSTATUS = 
        new PerfEventType(CONTEXTRESOURCE_INSERTSTATUS_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(CONTEXTRESOURCE_INSERTSTATUS_TAG));
    
    private static final String CONTEXTRESOURCE_OBSERVERUPDATE_TAG = "CONTEXTRESOURCE_OBSERVER_UPDATE";
    public static final PerfEventType CONTEXTRESOURCE_OBSERVERUPDATE = 
        new PerfEventType(CONTEXTRESOURCE_OBSERVERUPDATE_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(CONTEXTRESOURCE_OBSERVERUPDATE_TAG));
    
    
    private static final String PAGE_ISACCESSIBLE_TAG = "PAGE_IS_ACCESSIBLE";
    public static final PerfEventType PAGE_ISACCESSIBLE = 
        new PerfEventType(PAGE_ISACCESSIBLE_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(PAGE_ISACCESSIBLE_TAG));
    
    private static final String PAGE_NEEDSDATA_TAG = "PAGE_NEEDS_DATA";
    public static final PerfEventType PAGE_NEEDSDATA = 
        new PerfEventType(PAGE_NEEDSDATA_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(PAGE_NEEDSDATA_TAG));
    
    private static final String PAGE_HANDLESUBMITTEDDATA_TAG = "PAGE_HANDLE_SUBMITTED_DATA";
    public static final PerfEventType PAGE_HANDLESUBMITTEDDATA = 
        new PerfEventType(PAGE_HANDLESUBMITTEDDATA_TAG,
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(PAGE_HANDLESUBMITTEDDATA_TAG));
    
    private static final String PAGE_INITIWRAPPERS_TAG = "PAGE_INIT_IWRAPPERS";
    public static final PerfEventType PAGE_INITIWRAPPERS = 
        new PerfEventType(PAGE_INITIWRAPPERS_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(PAGE_INITIWRAPPERS_TAG));
    
    private static final String PAGE_RETRIEVECURRENTSTATUS_TAG = "PAGE_RETRIEVE_CURRENT_STATUS"; 
    public static final PerfEventType PAGE_RETRIEVECURRENTSTATUS = 
        new PerfEventType(PAGE_RETRIEVECURRENTSTATUS_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(PAGE_RETRIEVECURRENTSTATUS_TAG));
    
    
    private static final String CONTEXT_CREATENAVICOMPLETE_TAG = "CONTEXT_CREATE_NAVI_COMPLETE";
    public static final PerfEventType CONTEXT_CREATENAVICOMPLETE = 
        new PerfEventType(CONTEXT_CREATENAVICOMPLETE_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(CONTEXT_CREATENAVICOMPLETE_TAG));
    
    private static final String CONTEXT_CREATENAVIREUSE_TAG = "CONTEXT_CREATE_NAVI_REUSE";
    public static final PerfEventType CONTEXT_CREATENAVIREUSE = 
        new PerfEventType(CONTEXT_CREATENAVIREUSE_TAG,
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(CONTEXT_CREATENAVIREUSE_TAG));
    
    
    private static final String XMLSERVER_HANDLEDOCUMENT_TAG = "XMLSERVER_HANDLE_DOCUMENT";
    public static final PerfEventType XMLSERVER_HANDLEDOCUMENT = 
        new PerfEventType(XMLSERVER_HANDLEDOCUMENT_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(XMLSERVER_HANDLEDOCUMENT_TAG));
    
    private static final String XMLSERVER_GETDOM_TAG = "XMLSERVER_GET_DOM";
    public static final PerfEventType XMLSERVER_GETDOM = 
        new PerfEventType(XMLSERVER_GETDOM_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(XMLSERVER_GETDOM_TAG));
    
    private static final String XMLSERVER_CALLPROCESS_TAG = "XMLSERVER_CALL_PROCESS";
    public static final PerfEventType XMLSERVER_CALLPROCESS = 
        new PerfEventType(XMLSERVER_CALLPROCESS_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(XMLSERVER_CALLPROCESS_TAG));
    
    private static final String XMLSERVER_PREPROCESS_TAG = "XMLSERVER_PRE_PROCESS";
    public static final PerfEventType XMLSERVER_PREPROCESS = 
        new PerfEventType(XMLSERVER_PREPROCESS_TAG, 
                PerfEventTypeConfig.getInstance().getPerfDelayProperty(XMLSERVER_PREPROCESS_TAG));
    
    private String tag;
    private long delay;
    //private String msg;
    private String extra;
    private long duration;
    private String page;
    private String clazz;
    
    private PerfEventType(String t, long d) {
        this.tag = t;
        this.delay = d;
    }
    public String getTag() {
        return tag;
    }
    
    public long getDelay() {
        return delay;
    }
    
    
    public void setPage(String page) {
        this.page = page;
    }
    
    public void setClass(String clazz) {
        this.clazz = clazz;
    }
    
    /*public void setMessage(String message) {
        this.msg = message;
    }
    
    public void setMessage(PfixServletRequest psreq) {
        String pathinfo = psreq.getPathInfo();
        RequestParam name = psreq.getRequestParam(PageRequest.PAGEPARAM);
        if(name != null && !name.getValue().equals("")) {
            msg = name.getValue();
        } else if(pathinfo != null && !pathinfo.equals("")  &&
                    pathinfo.startsWith("/") && pathinfo.length() > 1) {
            msg = pathinfo.substring(1);
        } else {
            msg = "Null";
        }
    }*/
    
    public void setAdditionalInfo(String info) {
        this.extra = info;
    }
    
    public String getAdditionalInfo() {
        return extra;
    }
        
   /* public String getMessage() {
        return msg;
    }*/
    
    
    
    public void setDuration(long d) {
        duration = d;
    }
     
    public long getDuration() {
        return duration;
    }
    
  
    public String toString() {
        return "Tag: "+tag;
    }
    /**
     * @return
     */
    public String getPage() {    
        return this.page;
    }
    /**
     * @return
     */
    public String getHandlingClass() {
        return this.clazz;
    }

}
