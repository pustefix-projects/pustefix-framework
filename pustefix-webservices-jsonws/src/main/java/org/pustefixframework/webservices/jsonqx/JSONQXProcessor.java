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

package org.pustefixframework.webservices.jsonqx;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.json.JSONArray;
import org.pustefixframework.webservices.json.JSONObject;
import org.pustefixframework.webservices.json.parser.JSONParser;
import org.pustefixframework.webservices.jsonws.DeserializerRegistry;
import org.pustefixframework.webservices.jsonws.JSONDeserializer;
import org.pustefixframework.webservices.jsonws.JSONSerializer;
import org.pustefixframework.webservices.jsonws.SerializationException;
import org.pustefixframework.webservices.jsonws.Serializer;
import org.pustefixframework.webservices.jsonws.SerializerRegistry;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.beans.InitException;
import de.schlund.pfixcore.beans.metadata.DefaultLocator;
import de.schlund.pfixcore.webservice.ProcessingInfo;
import de.schlund.pfixcore.webservice.ServiceCallContext;
import de.schlund.pfixcore.webservice.ServiceDescriptor;
import de.schlund.pfixcore.webservice.ServiceException;
import de.schlund.pfixcore.webservice.ServiceProcessor;
import de.schlund.pfixcore.webservice.ServiceRegistry;
import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.ServiceRuntime;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;

/**
 * @author mleidig@schlund.de
 */
public class JSONQXProcessor implements ServiceProcessor {

    private Logger LOG=Logger.getLogger(JSONQXProcessor.class);
    
    private BeanDescriptorFactory beanDescFactory;
    private SerializerRegistry serializerRegistry;
    private DeserializerRegistry deserializerRegistry;
    
    public JSONQXProcessor() throws ServiceException {
        init(null);
    }
    
    private void init(URL defaultBeanMetaDataURL) throws ServiceException {
        try {
            if(defaultBeanMetaDataURL==null) beanDescFactory=new BeanDescriptorFactory();
            else beanDescFactory=new BeanDescriptorFactory(new DefaultLocator(defaultBeanMetaDataURL));
        } catch(InitException x) {
            throw new ServiceException("BeanDescriptorFactory initialization failed.",x);
        }
        serializerRegistry=new SerializerRegistry(beanDescFactory);
        Serializer dateSerializer=new CalendarSerializer();
        serializerRegistry.register(Date.class,dateSerializer);
        serializerRegistry.register(Calendar.class,dateSerializer);
        serializerRegistry.register(GregorianCalendar.class,dateSerializer);
        deserializerRegistry=new DeserializerRegistry(beanDescFactory);
    }
    
    public void setBeanMetaDataURL(URL defaultBeanMetaDataURL) throws ServiceException {
        init(defaultBeanMetaDataURL);
    }
    
    public void process(ServiceRequest req,ServiceResponse res,ServiceRuntime runtime,ServiceRegistry registry,ProcessingInfo procInfo) throws ServiceException {
        
        int errorOrigin=0;
        int errorCode=0;
        Throwable error=null;            
        String serviceName=null;
        ServiceConfig service=null;
        JSONObject jsonReq=null;
        Object resultObject=null;
        String jsonData=null;
        
        try {
            
            jsonData=req.getMessage();
            
            //Parsing
            try {
                long t1=System.currentTimeMillis();
                JSONParser parser=new JSONParser(new StringReader(jsonData));
                jsonReq=(JSONObject)parser.getJSONValue();
                long t2 = System.currentTimeMillis();
                if(LOG.isDebugEnabled()) LOG.debug("Parsing: "+(t2-t1)+"ms");
            } catch(Throwable t) {
                errorOrigin=1;
                throw new ServiceException("Error during parsing",t);
            }
            
            //Service lookup
            serviceName=jsonReq.getStringMember("service");
            if(serviceName==null||serviceName.trim().equals("")) {
                errorOrigin=1;
                errorCode=1;
                throw new ServiceException("Illegal service name: "+serviceName);
            } else service=registry.getService(serviceName);
            if(service==null) {
                errorOrigin=1;
                errorCode=2;
                throw new ServiceException("Service not found: "+serviceName);
            }
        
            //Service method lookup
            Method method=null;
            JSONArray params=jsonReq.getArrayMember("params");
            JSONDeserializer jsonDeser = new JSONDeserializer(deserializerRegistry);
            String methodName=jsonReq.getStringMember("method");
            ServiceDescriptor serviceDesc=runtime.getServiceDescriptorCache().getServiceDescriptor(service);
            List<Method> methods=serviceDesc.getMethods(methodName);
            if(methods.size()==0) {
                errorOrigin=1;
                errorCode=4;
                throw new ServiceException("Method not found: "+methodName);
            } else if(methods.size()==1) method=methods.get(0);
            else {
                //ambiguous methods, guess the right one
                Iterator<Method> methIt=methods.iterator();
                while(methIt.hasNext() && method==null) {
                    Method testMeth=methIt.next();
                    Type[] types=testMeth.getGenericParameterTypes();
                    if(types.length==params.size()) {
                        boolean canDeserialize=true;
                        for(int i=0;i<params.size() && canDeserialize;i++) {
                            if(!jsonDeser.canDeserialize(params.get(i),types[i])) canDeserialize=false;
                        }
                        if(canDeserialize) method=testMeth;
                    }
                }
            }
            if(method==null) {
                errorOrigin=1;
                errorCode=5;
                throw new ServiceException("No matching method found: "+methodName);
            }
                   
            //Deserialization
            Object[] paramObjects=null;
            try {
                long t1=System.currentTimeMillis();
                paramObjects=new Object[params.size()];
                Type[] types=method.getGenericParameterTypes();
                for(int i=0;i<params.size();i++) {
                    Object obj=params.get(i);
                    Object deserObj=jsonDeser.deserialize(obj, types[i]);
                    paramObjects[i]=deserObj;
                }
                long t2 = System.currentTimeMillis();
                if(LOG.isDebugEnabled()) LOG.debug("Deserialization: "+(t2-t1)+"ms");
            } catch(Throwable t) {
                errorOrigin=1;
                throw new ServiceException("Error during deserialization",t);
            }
                        
            procInfo.setService(serviceName);
            procInfo.setMethod(method.getName());
            procInfo.startInvocation();
                            
            //Invocation
            try { 
                Object serviceObject=registry.getServiceObject(serviceName);
                resultObject=method.invoke(serviceObject,paramObjects);
            } catch(Throwable t) {
                if(t instanceof InvocationTargetException && t.getCause()!=null) {
                    errorOrigin=2;
                    throw t.getCause();
                } else {
                    errorOrigin=1;
                    throw new ServiceException("Error during invocation",t);
                }
            } 
                            
            procInfo.endInvocation();
            if(LOG.isDebugEnabled()) LOG.debug("Invocation: "+procInfo.getInvocationTime()+"ms");
       
        } catch(Throwable t) {
            if(errorOrigin==0) errorOrigin=1;
            error=t;
            LOG.error(error,error);
        }
            
        try {
        
            res.setContentType("text/plain");
            res.setCharacterEncoding("utf-8");
            Writer writer=res.getMessageWriter();
            writer.write("{");
            if(jsonReq.hasMember("id")) {
                writer.write("\"id\":");
                writer.write(jsonReq.getMember("id").toString());
                writer.write(",");
            }
            if(error==null) {
                writer.write("\"result\":");
                //Serialization
                long t1=System.currentTimeMillis();
                if(resultObject instanceof Void || resultObject==null) {
                    writer.write("null");
                } else {
                    JSONSerializer jsonSer=new JSONSerializer(serializerRegistry,service.getJSONClassHinting());
                    jsonSer.serialize(resultObject,writer);
                }
                long t2=System.currentTimeMillis();
                if(LOG.isDebugEnabled()) LOG.debug("Serialization: "+(t2-t1)+"ms");
            } else {
                //Handle error
                LOG.error(error,error);
                ServiceCallContext callContext=ServiceCallContext.getCurrentContext();
                Fault fault=new Fault(serviceName,callContext.getServiceRequest(),
                        callContext.getServiceResponse(),jsonData,callContext.getContext());
                fault.setThrowable(error);
                FaultHandler faultHandler=service.getFaultHandler();
                if(faultHandler==null) faultHandler=runtime.getConfiguration().getGlobalServiceConfig().getFaultHandler();
                if(faultHandler!=null) faultHandler.handleFault(fault);
                error=fault.getThrowable();
                JSONObject errobj=new JSONObject();
                errobj.putMember("origin",errorOrigin);
                errobj.putMember("code",errorCode);
                writer.write("\"error\":");
                writer.write(errobj.toJSONString());   
            }
            writer.write("}");
            writer.flush();
            writer.close();    
       
        } catch(SerializationException x) {
            new ServiceException("Error while processing service request.",x);
        } catch(IOException x) {
            new ServiceException("Error while processing service request.",x);
        }
            
    }
  
    public void processException(ServiceRequest req, ServiceResponse res, Exception exception) throws ServiceException {
        try {
            res.setContentType("text/plain"); 
            res.setCharacterEncoding("utf-8");
            Writer writer=res.getMessageWriter();
            writer.write("{");
            JSONObject errobj=new JSONObject();
            errobj.putMember("origin",1);
            errobj.putMember("code",6);
            writer.write("\"error\":");
            writer.write(errobj.toJSONString());   
            writer.write("}");
            writer.flush();
            writer.close();      
        } catch(IOException x) {
            throw new ServiceException("IOException during service exception processing.",x);
        }
    }
    
}
