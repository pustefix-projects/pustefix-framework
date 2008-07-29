package org.pustefixframework.webservices.test;

import java.io.IOException;

import org.pustefixframework.webservices.ProcessingInfo;
import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceProcessor;
import org.pustefixframework.webservices.ServiceRegistry;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;
import org.pustefixframework.webservices.ServiceRuntime;

public class MockServiceProcessor implements ServiceProcessor {

    private String contentType = "text/plain";
    private String encoding = "UTF-8";
    private String content = "";
    private String serviceMethod;
   
    public void process(ServiceRequest req, ServiceResponse res, ServiceRuntime runtime, ServiceRegistry registry, ProcessingInfo procInfo)
            throws ServiceException {
        
        String serviceName = req.getServiceName();
        runtime.getConfiguration().getServiceConfig(serviceName);
        
        Object serviceObject = registry.getServiceObject(serviceName);
        
        
        
        res.setContentType(contentType);
        res.setCharacterEncoding(encoding);
        try {
            res.setMessage(content);
            
        } catch(IOException x) {
            throw new ServiceException("IO error",x);
        }
    }
    
    public void processException(ServiceRequest req, ServiceResponse res, Exception exception) throws ServiceException {
        // TODO Auto-generated method stub
        
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
