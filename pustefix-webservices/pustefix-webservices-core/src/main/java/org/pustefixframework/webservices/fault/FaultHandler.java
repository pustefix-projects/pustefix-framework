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
package org.pustefixframework.webservices.fault;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public abstract class FaultHandler implements Serializable {
	
    private static final long serialVersionUID = -7813108540195779294L;
    
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
