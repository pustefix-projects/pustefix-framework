/*
 * Created on 29.07.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.pustefixframework.webservices.fault;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public abstract class FaultHandler implements Serializable {
	
    HashMap<String,String> params=new HashMap<String,String>();
    
	protected FaultHandler() {
	}
    
	public void addParam(String name,String value) {
		params.put(name,value);
	}
	
    public void setParams(HashMap<String,String> params) {
        this.params=params;
    }
    
    public HashMap<String,String> getParams() {
        return params;
    }
    
    public String getParam(String name) {
        if(params==null) return null;
        return params.get(name);
    }
	
    public String paramsToString() {
        if(params==null) return "[]";
        else {
            StringBuffer sb=new StringBuffer();
            sb.append("[");
            Iterator<String> it=params.keySet().iterator();
            while(it.hasNext()) {
                String key=it.next();
                String val=params.get(key);
                sb.append("("+key+"="+val+")");
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    public abstract void init();
    
	public abstract void handleFault(Fault fault);

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FaultHandler) {
			FaultHandler ref=(FaultHandler)obj;
			if(!getClass().equals(ref.getClass())) return false;
			if(params==null ^ ref.getParams()==null) return false;
			Iterator<String> it=params.keySet().iterator();
			while(it.hasNext()) {
				String name=it.next();
				String val=params.get(name);
				String refVal=ref.getParam(name);
				if(refVal==null||!(val.equals(refVal))) return false;
			}
			it=ref.getParams().keySet().iterator();
			while(it.hasNext()) {
				String name=it.next();
				String val=params.get(name);
				if(val==null) return false;
			}
			return true;
		}
		return false;
	}
    
}
