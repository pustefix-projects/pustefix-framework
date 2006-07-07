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

package de.schlund.pfixcore.webservice.jsonrpc;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.metaparadigm.jsonrpc.JSONRPCBridge;
import com.metaparadigm.jsonrpc.JSONRPCResult;
import com.metaparadigm.jsonrpc.org.json.JSONArray;
import com.metaparadigm.jsonrpc.org.json.JSONObject;

import de.schlund.pfixcore.webservice.Constants;
import de.schlund.pfixcore.webservice.ServiceDescriptor;
import de.schlund.pfixcore.webservice.ServiceException;
import de.schlund.pfixcore.webservice.ServiceProcessor;
import de.schlund.pfixcore.webservice.ServiceRegistry;
import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixcore.webservice.config.ServiceConfig;

/**
 * @author mleidig@schlund.de
 */
public class JSONRPCProcessor implements ServiceProcessor {
	
	final static Logger LOG=Logger.getLogger(JSONRPCProcessor.class.getName());
	
	HashMap<String,Object> appServiceObjects;
	JSONRPCBridge applicationBridge;
    
    static CalendarSerializer calendarSerializer=new CalendarSerializer();
	
	public JSONRPCProcessor() throws ServiceException {
		 java.util.logging.Logger l=java.util.logging.Logger.getLogger("com.metaparadigm.jsonrpc");
		 l.setLevel(Level.SEVERE);
		 applicationBridge=createBridge();
	}
	
	public void process(ServiceRequest req,ServiceResponse res,ServiceRegistry registry) throws ServiceException {
		 String jsonData=null;
		 JSONRPCResult jsonRes=null;
		 String serviceName=req.getServiceName();
		 ServiceConfig srvConf=registry.getServiceConfig(serviceName);
		 if(req.getParameter("json")!=null) {
			 jsonRes=listMethods(srvConf,registry);
		 } else {
			 try {
				 jsonData=req.getMessage();
			 } catch(IOException x) {
				 throw new ServiceException("IOException while reading message.",x);
			 }
			 
			 Object serviceObject=registry.getServiceObject(serviceName);
			 String scope=srvConf.getScopeType();
			 if(scope==null) scope=registry.getGlobalServiceConfig().getScopeType();
			 
			 JSONRPCBridge bridge=null;
			 if(scope.equals(Constants.SERVICE_SCOPE_APPLICATION)) {
				 synchronized(applicationBridge) {
					 bridge=applicationBridge;
					 Object obj=null;
					 try {
						 obj=bridge.lookupObject(serviceName);
					 } catch(Exception x) {
						 throw new ServiceException("Error while doing object lookup.",x);
					 }	 
					 if(obj==null) {
						 bridge.registerObject(serviceName,serviceObject);
						 if(LOG.isDebugEnabled()) LOG.debug("Registered service object "+serviceName);
					 } else if(obj!=serviceObject) {
						 bridge.unregisterObject(serviceName);
						 bridge.registerObject(serviceName,serviceObject);
						 if(LOG.isDebugEnabled()) LOG.debug("Re-registered service object "+serviceName);
					 }
				 }
			 } else if(scope.equals(Constants.SERVICE_SCOPE_SESSION)) {
				 if(req.getUnderlyingRequest() instanceof HttpServletRequest) {
					 HttpServletRequest httpReq=(HttpServletRequest)req.getUnderlyingRequest();
					 HttpSession session=httpReq.getSession(false);
					 if(session!=null) {
						 bridge=(JSONRPCBridge)session.getAttribute(JSONRPCBridge.class.getName());
						 if(bridge==null) bridge=createBridge();
						 Object obj=null;
						 try {
							 obj=bridge.lookupObject(serviceName);
						 } catch(Exception x) {
							 throw new ServiceException("Error while doing object lookup.",x);
						 }
						 if(obj==null) bridge.registerObject(serviceName,serviceObject);
						 else if(obj!=serviceObject) {
							 bridge.unregisterObject(serviceName);
							 bridge.registerObject(serviceName,serviceObject);
						 }
						 session.setAttribute(JSONRPCBridge.class.getName(),bridge); 
					 } 
				 }
			 } else if(scope.equals(Constants.SERVICE_SCOPE_REQUEST)) {
				 bridge=createBridge();
				 bridge.registerObject(serviceName,serviceObject);
			 }
			 
	        JSONObject json_req = null;
	        try {
	            json_req = new JSONObject(jsonData);
	            jsonRes = bridge.call(new Object[] {req}, json_req);
	            
	        } catch (ParseException e) {
	            System.err.println("can't parse call: " + jsonData);
	            jsonRes = new JSONRPCResult
	                (JSONRPCResult.CODE_ERR_PARSE, null,
	                 JSONRPCResult.MSG_ERR_PARSE);
	        }
	                                   
		 }
	   
			 res.setContentType("text/plain");
			 res.setCharacterEncoding("utf8");
			 
			 try {
				 res.setMessage(jsonRes.toString());
			 } catch(IOException x) {
				 throw new ServiceException("IOException while writing message.",x);
			 }

	}
	
    private JSONRPCBridge createBridge() throws ServiceException {
        JSONRPCBridge bridge=new JSONRPCBridge();
        try {
            bridge.registerSerializer(calendarSerializer);
        } catch(Exception x) {
            throw new ServiceException("Error while registering serializer for Calendar type.",x);
        }
        return bridge;
    }
    
	private JSONRPCResult listMethods(ServiceConfig srvConf,ServiceRegistry srvReg) throws ServiceException {
		JSONArray meths=new JSONArray();
		ServiceDescriptor desc=srvReg.getServiceDescriptor(srvConf.getName());
		Iterator<String> methIt=desc.getMethods();
		while(methIt.hasNext()) meths.put(methIt.next());
		return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS,"0",meths);
	}
	
}
