/*
 * Created on 31.07.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HttpMessage {
    
    private HttpHeader[] headers;
    private byte[] body;
   
    public HttpMessage(HttpHeader[] headers,byte[] body) {
    	this.headers=headers;
        this.body=body;
    }
    
    public HttpHeader[] getHeaders() {
    	return headers;
    }
    
    public byte[] getBody() {
    	return body;
    }
    
    public String toString() {
    	StringBuffer sb=new StringBuffer();
        for(int i=0;i<headers.length;i++) {
        	sb.append(headers[i].toString());
            sb.append("\n");
        }
        sb.append("\n");
        sb.append(new String(body));
        return sb.toString();
    }
    
}
