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

import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.ServiceDescriptor;
import de.schlund.pfixcore.webservice.ServiceException;
import de.schlund.pfixcore.webservice.ServiceProcessor;
import de.schlund.pfixcore.webservice.ServiceRegistry;
import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.json.JSONArray;
import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.json.parser.JSONParser;

/**
 * @author mleidig@schlund.de
 */
public class JSONWSProcessor implements ServiceProcessor {

    private Logger LOG=Logger.getLogger(JSONWSProcessor.class);
    
    public JSONWSProcessor() {
    }
    
    public void process(ServiceRequest req, ServiceResponse res, ServiceRegistry registry) throws ServiceException {
        try {
            String jsonData = null;
            JSONObject jsonRes = null;
            String serviceName = req.getServiceName();
            ServiceConfig srvConf = registry.getServiceConfig(serviceName);
            if (srvConf == null)
                throw new ServiceException("Unknown service: " + serviceName);

            if (req.getParameter("json") != null) {
                jsonRes = listMethods(srvConf, registry);
                res.setContentType("text/plain");
                res.setCharacterEncoding("utf8");
                res.setMessage(jsonRes.toJSONString());
            } else {
                jsonData = req.getMessage();

                Object serviceObject = registry.getServiceObject(serviceName);
                ServiceDescriptor serviceDesc = registry
                        .getServiceDescriptor(serviceName);
                String scope = srvConf.getScopeType();
                if (scope == null)
                    scope = registry.getGlobalServiceConfig().getScopeType();

                JSONObject json_req = null;

                // Parsing

                long t1 = System.currentTimeMillis();

                JSONParser parser = new JSONParser(new StringReader(jsonData));
                json_req = (JSONObject) parser.getJSONValue();

                long t2 = System.currentTimeMillis();
                if (LOG.isDebugEnabled())
                    LOG.debug("Parsing: " + (t2 - t1) + "ms");

                String methodName = json_req.getStringMember("method");
                JSONArray params = json_req.getArrayMember("params");

                JSONDeserializer jsonDeser = new JSONDeserializer();

                List<Method> methods = serviceDesc.getMethods(methodName);
                Method method = null;
                if (methods.size() == 0)
                    throw new ServiceException("Method '" + methodName
                            + "' not found!");
                else if (methods.size() == 1)
                    method = methods.get(0);
                else {
                    // ambiguous methods, guess the right one
                    Iterator<Method> methIt = methods.iterator();
                    while (methIt.hasNext() && method == null) {
                        Method testMeth = methIt.next();
                        Class[] types = testMeth.getParameterTypes();
                        if (types.length == params.size()) {
                            boolean canDeserialize = true;
                            for (int i = 0; i < params.size() && canDeserialize; i++) {
                                if (!jsonDeser.canDeserialize(params.get(i),
                                        types[i]))
                                    canDeserialize = false;
                            }
                            if (canDeserialize)
                                method = testMeth;
                        }
                    }
                }

                Class[] types = method.getParameterTypes();

                // Deserialization

                t1 = System.currentTimeMillis();

                Object[] paramObjects = new Object[params.size()];
                for (int i = 0; i < params.size(); i++) {
                    Object obj = params.get(i);
                    Object deserObj = jsonDeser.deserialize(obj, types[i]);
                    paramObjects[i] = deserObj;

                }

                t2 = System.currentTimeMillis();
                if (LOG.isDebugEnabled())
                    LOG.debug("Deserialization: " + (t2 - t1) + "ms");

                res.setContentType("text/plain");
                res.setCharacterEncoding("utf8");

                Writer writer = null;

                // res.setMessage(jsonRes.toJSONString());
                writer = res.getMessageWriter();

                // Invocation

                t1 = System.currentTimeMillis();

                Object resultObject = method
                        .invoke(serviceObject, paramObjects);

                t2 = System.currentTimeMillis();
                if (LOG.isDebugEnabled())
                    LOG.debug("Invocation: " + (t2 - t1) + "ms");

                writer.write("{");
                writer.write("\"id\":");
                writer.write(String.valueOf(json_req.getNumberMember("id")));
                writer.write(",");
                writer.write("\"result\":");

                // Serialization

                t1 = System.currentTimeMillis();

                if (resultObject instanceof Void || resultObject == null) {
                    writer.write("null");
                } else {
                    JSONSerializer jsonSer = new JSONSerializer();
                    jsonSer.serialize(resultObject, writer);
                }

                t2 = System.currentTimeMillis();
                if (LOG.isDebugEnabled())
                    LOG.debug("Serialization: " + (t2 - t1) + "ms");

                writer.write("}");

                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            throw new ServiceException("Error while processing service request.",e);
        }

    }
    
    private JSONObject listMethods(ServiceConfig srvConf,ServiceRegistry srvReg) throws ServiceException {
        JSONArray meths=new JSONArray();
        ServiceDescriptor desc=srvReg.getServiceDescriptor(srvConf.getName());
        if(desc!=null) {
            Iterator<String> methIt=desc.getMethods();
            while(methIt.hasNext()) meths.add(methIt.next());
            JSONObject resObj=new JSONObject();
            resObj.putMember("result",meths);
            resObj.putMember("id",0);
            return resObj;
        } else throw new ServiceException("Unknown service: "+srvConf.getName());
    }
    
}
