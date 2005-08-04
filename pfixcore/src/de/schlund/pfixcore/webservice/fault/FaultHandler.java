/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.fault;

import java.util.HashMap;
import java.util.Iterator;

public abstract class FaultHandler {
	
    HashMap params;
    
	protected FaultHandler() {
	}
    
    public void setParams(HashMap params) {
        this.params=params;
    }
    
    public HashMap getParams() {
        return params;
    }
    
    public String getParam(String name) {
        if(params==null) return null;
        return (String)params.get(name);
    }
	
    public String paramsToString() {
        if(params==null) return "[]";
        else {
            StringBuffer sb=new StringBuffer();
            sb.append("[");
            Iterator it=params.keySet().iterator();
            while(it.hasNext()) {
                String key=(String)it.next();
                String val=(String)params.get(key);
                sb.append("("+key+"="+val+")");
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    public abstract void init();
    
	public abstract void handleFault(Fault fault);

}
