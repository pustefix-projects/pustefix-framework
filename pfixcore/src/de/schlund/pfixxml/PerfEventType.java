/*
 * Created on 23.06.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml;

import de.schlund.pfixcore.workflow.PageRequest;

/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PerfEventType {
    
    public static final PerfEventType IHANDLER_ISACTIVE = new PerfEventType("IHANDLER_IS_ACTIVE", 3);
    public static final PerfEventType IHANDLER_NEEDSDATA = new PerfEventType("IHANDLER_NEEDS_DATA", 3);
    public static final PerfEventType IHANDLER_PREREQUISITESMET = new PerfEventType("IHANDLER_PREREQUISITES_MET", 3);
    
    public static final PerfEventType CONTEXTRESOURCE_INSERTSTATUS = new PerfEventType("CONTEXTRESOURCE_INSERT_STATUS", 5);
    public static final PerfEventType CONTEXTRESOURCE_OBSERVERUPDATE = new PerfEventType("CONTEXTRESOURCE_OBSERVER_UPDATE", 0);
    
    public static final PerfEventType PAGE_ISACCESSIBLE = new PerfEventType("PAGE_IS_ACCESSIBLE", 10);
    public static final PerfEventType PAGE_NEEDSDATA = new PerfEventType("PAGE_NEEDS_DATA", 10);
    public static final PerfEventType PAGE_HANDLESUBMITTEDDATA = new PerfEventType("PAGE_HANDLE_SUBMITTED_DATA", 300);
    public static final PerfEventType PAGE_INITIWRAPPERS = new PerfEventType("PAGE_INIT_IWRAPPERS", 5);
    public static final PerfEventType PAGE_RETRIEVECURRENTSTATUS = new PerfEventType("PAGE_RETRIEVE_CURRENT_STATUS", 5);
    
    public static final PerfEventType CONTEXT_CREATENAVICOMPLETE = new PerfEventType("CONTEXT_CREATE_NAVI_COMPLETE", 25);
    public static final PerfEventType CONTEXT_CREATENAVIREUSE = new PerfEventType("CONTEXT_CREATE_NAVI_REUSE", 2);
    
    public static final PerfEventType XMLSERVER_HANDLEDOCUMENT = new PerfEventType("XMLSERVER_HANDLE_DOCUMENT", 100);
    public static final PerfEventType XMLSERVER_GETDOM = new PerfEventType("XMLSERVER_GET_DOM", 400);
    public static final PerfEventType XMLSERVER_CALLPROCESS= new PerfEventType("XMLSERVER_CALL_PROCESS", 0);
    public static final PerfEventType XMLSERVER_PREPROCESS = new PerfEventType("XMLSERVER_PRE_PROCESS", 0);
    
    
    private String tag;
    private long delay;
    private String msg;
    private String extra;
    private long duration;
    
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
    
    public void setMessage(String message) {
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
    }
    
    public void setAdditionalInfo(String info) {
        this.extra = info;
    }
    
    public String getAdditionalInfo() {
        return extra;
    }
        
    public String getMessage() {
        return msg;
    }
    
    public void setDuration(long d) {
        duration = d;
    }
     
    public long getDuration() {
        return duration;
    }
    
    public String toString() {
        return "Tag: "+tag;
    }

}
