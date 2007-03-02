/*
 * Created on 19.02.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.util;

import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class URIParameters {
    
    private final static Logger LOG = Logger.getLogger(URIParameters.class);
    
    HttpServletRequest request;
    String encoding;
    HashMap params;
    
    public URIParameters(HttpServletRequest request,String encoding) throws Exception {
   	this.request=request;
   	this.encoding=encoding;
        params=parse(request.getQueryString(),encoding);
        if(LOG.isDebugEnabled()) LOG.debug(toString());
    }
    
    public HttpServletRequest getRequest() {
   	return request;
    }
    
    public String getEncoding() {
   	return encoding;
    }
    
    private HashMap parse(String queryStr,String encoding) throws Exception {
   	HashMap map=new HashMap();
        if(queryStr!=null) {
            StringTokenizer st=new StringTokenizer(queryStr,"&");
            while(st.hasMoreTokens()) {
      		String pair=st.nextToken();
      		int ind=pair.indexOf('=');
      		if(ind>0) {
                    String key=pair.substring(0,ind);
                    String val=pair.substring(ind+1);
                    String decKey=URLDecoder.decode(key,encoding);
                    String decVal=URLDecoder.decode(val,encoding);
                    String[] vals=(String[])map.get(decKey);
                    if(vals==null) vals=new String[] {decVal};
                    else {
                        String[] tmp=new String[vals.length+1];
                        for(int i=0;i<vals.length;i++) tmp[i]=vals[i];
                        tmp[vals.length]=decVal;
                        vals=tmp;
                    }
                    map.put(decKey,vals); 
      		}
            }
        }
        return map;
    }
    
    public String getParameter(String name) {
   	String[] vals=(String[])params.get(name);
   	if(vals!=null && vals.length>0) return vals[0];
   	return null;
    }
    
    public Enumeration getParameterNames() {
   	Vector v=new Vector();
   	if(!params.isEmpty()) {
            Iterator it=params.keySet().iterator();
            while(it.hasNext()) v.add(it.next());
   	}
   	return v.elements();
    }
    
    public String[] getParameterValues(String name) {
   	return (String[])params.get(name);
    }
    
    public Map getParameterMap() {
   	return params;
    }
    
    public String toString() {
   	StringBuffer sb=new StringBuffer();
   	sb.append("URIParameters");
   	sb.append(" (");
   	sb.append(encoding);
   	sb.append("):\n");
   	Iterator it=params.keySet().iterator();
   	while(it.hasNext()) {
            String name=(String)it.next();
            sb.append("   ");
            sb.append(name);
            sb.append(" = ");
            String[] values=(String[])params.get(name);
            for(int i=0;i<values.length;i++) {
                sb.append(values[i]);
                if(i<values.length-1) sb.append(" | ");
            }
            sb.append("\n");
   	}
        return sb.toString();
    }
    
}
