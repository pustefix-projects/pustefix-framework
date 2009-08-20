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

package org.pustefixframework.webservices.jsonws;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.pustefixframework.webservices.ProcessingInfo;
import org.pustefixframework.webservices.ServiceCallContext;
import org.pustefixframework.webservices.ServiceDescriptor;
import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceProcessor;
import org.pustefixframework.webservices.ServiceRegistry;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;
import org.pustefixframework.webservices.ServiceRuntime;
import org.pustefixframework.webservices.fault.Fault;
import org.pustefixframework.webservices.fault.FaultHandler;
import org.pustefixframework.webservices.json.JSONArray;
import org.pustefixframework.webservices.json.JSONObject;
import org.pustefixframework.webservices.json.parser.JSONParser;
import org.pustefixframework.webservices.spring.WebserviceRegistration;

import de.schlund.pfixcore.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.beans.InitException;
import de.schlund.pfixcore.beans.metadata.DefaultLocator;

/**
 * @author mleidig@schlund.de
 */
public class JSONWSProcessor implements ServiceProcessor {

    private Logger LOG=Logger.getLogger(JSONWSProcessor.class);
    
    private BeanDescriptorFactory beanDescFactory;
    private SerializerRegistry serializerRegistry;
    private DeserializerRegistry deserializerRegistry;
    
    public JSONWSProcessor() throws ServiceException {
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
        deserializerRegistry=new DeserializerRegistry(beanDescFactory);
    }
    
    public void setBeanMetaDataURL(URL defaultBeanMetaDataURL) throws ServiceException {
        init(defaultBeanMetaDataURL);
    }
    
    public void process(ServiceRequest req,ServiceResponse res,ServiceRuntime runtime,ServiceRegistry registry,ProcessingInfo procInfo) throws ServiceException {
        Writer writer=null;
        try {
            
            String serviceName=req.getServiceName();
            WebserviceRegistration service=registry.getWebserviceRegistration(serviceName);
            if(service==null) throw new ServiceException("Service not found: "+serviceName);
            
            if(req.getParameter("json")!=null) {
                
                //Get service description
                JSONObject jsonRes=listMethods(service,runtime,registry);
                res.setContentType("text/plain");
                res.setCharacterEncoding("utf-8");
                res.setMessage(jsonRes.toJSONString());
                
            } else {
                
                Throwable error=null;
                String jsonData=req.getMessage();
                JSONObject jsonReq=null;
                Object resultObject=null;
                
                //Parsing
                try {
                    long t1=System.currentTimeMillis();
                    JSONParser parser=new JSONParser(new StringReader(jsonData));
                    jsonReq=(JSONObject)parser.getJSONValue();
                    long t2 = System.currentTimeMillis();
                    if (LOG.isDebugEnabled()) LOG.debug("Parsing: "+(t2-t1)+"ms");
                } catch(Throwable t) {
                    error=new ServiceException("Error during parsing",t);
                }

                if(error==null) {
                    
                    //Service method lookup
                    Method method=null;
                    JSONArray params=jsonReq.getArrayMember("params");
                    JSONDeserializer jsonDeser = new JSONDeserializer(deserializerRegistry);
                    try {
                        String methodName=jsonReq.getStringMember("method");
                        ServiceDescriptor serviceDesc=runtime.getServiceDescriptorCache().getServiceDescriptor(service);
                        List<Method> methods=serviceDesc.getMethods(methodName);
                        if(methods.size()==0) throw new ServiceException("Method not found: "+methodName);
                        else if(methods.size()==1) method=methods.get(0);
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
                        if(method==null) throw new ServiceException("No matching method found: "+methodName);
                    } catch(Throwable t) {
                        error=new ServiceException("Error during method lookup",t);
                    }
                   
                    if(error==null) {
                        
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
                            error=new ServiceException("Error during deserialization",t);
                        }
                        
                        if(error==null) {
                            
                            procInfo.setService(serviceName);
                            procInfo.setMethod(method.getName());
                            procInfo.startInvocation();
                            
                            //Invocation
                            try { 
                                Object serviceObject=service.getTarget();
                                resultObject=method.invoke(serviceObject,paramObjects);
                            } catch(Throwable t) {
                                if(t instanceof InvocationTargetException && t.getCause()!=null) error=t.getCause();
                                else error=new ServiceException("Error during invocation",t);
                            } 
                            
                            procInfo.endInvocation();
                            if(LOG.isDebugEnabled()) LOG.debug("Invocation: "+procInfo.getInvocationTime()+"ms");
                        }
                    }
                }
                
                res.setContentType("text/plain");
                res.setCharacterEncoding("utf-8");
                writer=res.getMessageWriter();
                writer.write("{");
                if(jsonReq!=null && jsonReq.hasMember("id")) {
                    writer.write("\"id\":");
                    writer.write("\""+jsonReq.getStringMember("id")+"\"");
                    writer.write(",");
                }
           
                if(error==null) {
                    writer.write("\"result\":");
                    //Serialization
                    long t1=System.currentTimeMillis();
                    if(resultObject instanceof Void || resultObject==null) {
                        writer.write("null");
                    } else {
                    	boolean classHinting = false;
                    	if(service.getClassHinting() != null) classHinting = service.getClassHinting();
                    	else if(runtime.getConfiguration().getClassHinting() != null) classHinting = runtime.getConfiguration().getClassHinting();
                        JSONSerializer jsonSer=new JSONSerializer(serializerRegistry, classHinting);
                        jsonSer.serialize(resultObject,writer);
                    }
                    long t2=System.currentTimeMillis();
                    if(LOG.isDebugEnabled()) LOG.debug("Serialization: "+(t2-t1)+"ms");
                } else {
                    //Handle error
                    LOG.error(error,error);
                    try {LOG.error(req.dump());} catch(Exception x) {LOG.error("No dump available",x);}
                    ServiceCallContext callContext=ServiceCallContext.getCurrentContext();
                    Fault fault=new Fault(serviceName,callContext.getServiceRequest(),
                            callContext.getServiceResponse(),jsonData,callContext.getContext());
                    fault.setThrowable(error);
                    FaultHandler faultHandler=runtime.getConfiguration().getFaultHandler();
                    if(faultHandler!=null) faultHandler.handleFault(fault);
                    error=fault.getThrowable();
                    JSONObject errobj=new JSONObject();
                    errobj.putMember("name",error.getClass().getName());
                    errobj.putMember("message",error.getMessage());
                    writer.write("\"error\":");
                    writer.write(errobj.toJSONString());   
                }
                writer.write("}");
                writer.flush();
            }
        } catch (Exception e) {
            ServiceException se=new ServiceException("Error while processing service request.",e);
            LOG.error(se);
            try {LOG.error(req.dump());} catch(Exception x) {LOG.error("No dump available",x);}
            throw se;
        } finally {
            if(writer!=null) {
                try {
                    writer.close();
                } catch(IOException x) {}
            }
        }
    }

    public void processException(ServiceRequest req, ServiceResponse res, Exception exception) throws ServiceException {
        try {
            res.setContentType("text/plain");
            res.setCharacterEncoding("utf-8");
            Writer writer=res.getMessageWriter();
            writer.write("{");
            JSONObject errobj=new JSONObject();
            errobj.putMember("name",exception.getClass().getName());
            errobj.putMember("message",exception.getMessage());
            writer.write("\"error\":");
            writer.write(errobj.toJSONString());   
            writer.write("}");
            writer.flush();
            writer.close();
        } catch(IOException x) {
            throw new ServiceException("IOException during service exception processing.",x);
        }
    }
    
    private JSONObject listMethods(WebserviceRegistration registration,ServiceRuntime runtime,ServiceRegistry srvReg) throws ServiceException {
        JSONArray meths=new JSONArray();
        ServiceDescriptor desc=runtime.getServiceDescriptorCache().getServiceDescriptor(registration);
        if(desc!=null) {
            for(String methName:desc.getMethods()) meths.add(methName);
            JSONObject resObj=new JSONObject();
            resObj.putMember("result",meths);
            resObj.putMember("id",0);
            return resObj;
        } else throw new ServiceException("Unknown service: "+registration.getServiceName());
    }
    
}
