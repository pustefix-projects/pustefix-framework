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
package org.pustefixframework.webservices;

import java.io.IOException;
import java.lang.reflect.Method;

public class TestServiceProcessor implements ServiceProcessor {

    private String contentType = "text/plain";
    private String encoding = "UTF-8";
    private String content = "";
    private String serviceMethod = "test";
   
    public void process(ServiceRequest req, ServiceResponse res, ServiceRuntime runtime, ServiceRegistry registry, ProcessingInfo procInfo)
            throws ServiceException {
        
        String serviceName = req.getServiceName();
        runtime.getConfiguration().getServiceConfig(serviceName);
        
        Object serviceObject = registry.getServiceObject(serviceName);
        
        String reqMsg = null;
        try {
            reqMsg = req.getMessage();
        } catch(IOException x) {
            throw new ServiceException("Can't get request message",x);
        }
        
        Object result = null;
        try {
            Method meth = serviceObject.getClass().getMethod(serviceMethod,String.class);
            result = meth.invoke(serviceObject, reqMsg);
        } catch (Exception x) {
            throw new ServiceException("Error invoking service method",x);
        }
        
        res.setContentType(contentType);
        res.setCharacterEncoding(encoding);
   
        try {
            if(result==Void.class) res.setMessage(content);
            else res.setMessage(result.toString());
        } catch(IOException x) {
            throw new ServiceException("Can't set response message",x);
        }
    }
    
    public void processException(ServiceRequest req, ServiceResponse res, Exception exception) throws ServiceException {
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setServiceMethod(String serviceMethod) {
        this.serviceMethod = serviceMethod;
    }
    
}
