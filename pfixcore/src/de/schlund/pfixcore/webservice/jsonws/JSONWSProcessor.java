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

package de.schlund.pfixcore.webservice.jsonws;

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

import de.schlund.pfixcore.webservice.ProcessingInfo;
import de.schlund.pfixcore.webservice.ServiceCallContext;
import de.schlund.pfixcore.webservice.ServiceDescriptor;
import de.schlund.pfixcore.webservice.ServiceException;
import de.schlund.pfixcore.webservice.ServiceProcessor;
import de.schlund.pfixcore.webservice.ServiceRegistry;
import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.ServiceRuntime;
import de.schlund.pfixcore.webservice.beans.BeanDescriptorFactory;
import de.schlund.pfixcore.webservice.beans.InitException;
import de.schlund.pfixcore.webservice.beans.metadata.DefaultLocator;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;
import de.schlund.pfixcore.webservice.json.JSONArray;
import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.json.parser.JSONParser;

/**
 * @author mleidig@schlund.de
 */
public class JSONWSProcessor implements ServiceProcessor {

    private Logger LOG=Logger.getLogger(JSONWSProcessor.class);
    
    private BeanDescriptorFactory beanDescFactory;
    private SerializerRegistry serializerRegistry;
    private DeserializerRegistry deserializerRegistry;
    
    public JSONWSProcessor(URL defaultBeanMetaDataURL) throws ServiceException {
        try {
            beanDescFactory=new BeanDescriptorFactory(new DefaultLocator(defaultBeanMetaDataURL));
        } catch(InitException x) {
            throw new ServiceException("BeanDescriptorFactory initialization failed.",x);
        }
        serializerRegistry=new SerializerRegistry(beanDescFactory);
        deserializerRegistry=new DeserializerRegistry(beanDescFactory);
    }
    
    public void process(ServiceRequest req,ServiceResponse res,ServiceRuntime runtime,ServiceRegistry registry,ProcessingInfo procInfo) throws ServiceException {
        Writer writer=null;
        try {
            
            String serviceName=req.getServiceName();
            ServiceConfig service=registry.getService(serviceName);
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
                                Object serviceObject=registry.getServiceObject(serviceName);
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
                        JSONSerializer jsonSer=new JSONSerializer(serializerRegistry,service.getJSONClassHinting());
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
                    FaultHandler faultHandler=service.getFaultHandler();
                    if(faultHandler==null) faultHandler=runtime.getConfiguration().getGlobalServiceConfig().getFaultHandler();
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

    private JSONObject listMethods(ServiceConfig service,ServiceRuntime runtime,ServiceRegistry srvReg) throws ServiceException {
        JSONArray meths=new JSONArray();
        ServiceDescriptor desc=runtime.getServiceDescriptorCache().getServiceDescriptor(service);
        if(desc!=null) {
            for(String methName:desc.getMethods()) meths.add(methName);
            JSONObject resObj=new JSONObject();
            resObj.putMember("result",meths);
            resObj.putMember("id",0);
            return resObj;
        } else throw new ServiceException("Unknown service: "+service.getName());
    }
    
}
