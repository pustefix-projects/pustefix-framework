/*
 * Created on 31.07.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

import java.util.Date;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class HttpRequest extends HttpMessage {

    private HttpResponse response;
    private String method;
    private String uri;
    private Date date;
    private long time;
    
    public HttpRequest(String method,String uri,Date date,long time,HttpHeader[] headers,byte[] body) {
    	super(headers,body);
      this.method=method;
      this.uri=uri;
      this.date=date;
      this.time=time;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getURI() {
        return uri;
    }
    
    public Date getDate() {
        return date;
    }
    
    public long getTime() {
        return time;
    }
    
    public void setResponse(HttpResponse response) {
    	this.response=response;
    }
    
    public HttpResponse getResponse() {
    	return response;
    }
    
}
