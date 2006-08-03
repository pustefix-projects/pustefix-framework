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

package de.schlund.pfixcore.webservice;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.ConfigurationException;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.ConfigurationReader;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;

import de.schlund.pfixcore.webservice.jsonws.JSONWSProcessor;
import de.schlund.pfixcore.webservice.monitor.MonitorHistory;
import de.schlund.pfixcore.webservice.monitor.MonitorRecord;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.loader.AppLoader;

/**
 * WebServiceServlet.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceServlet extends AxisServlet implements ServiceProcessor {

    private Category LOG=Logger.getLogger(getClass().getName());
    private boolean DEBUG=LOG.isDebugEnabled();
    
    private static Object initLock=new Object();
    private ServiceRuntime runtime;
   
   
    private ClassLoader currentLoader;
    
    private static ThreadLocal<Fault> currentFault=new ThreadLocal<Fault>();
    private static ThreadLocal<ServiceRequest> currentRequest=new ThreadLocal<ServiceRequest>();
    private static ThreadLocal<ServiceResponse> currentResponse=new ThreadLocal<ServiceResponse>();
    
    public static void setCurrentFault(Fault fault) {
        currentFault.set(fault);
    }
    
    public static Fault getCurrentFault() {
        return (Fault)currentFault.get();
    }
    
    public static void setCurrentRequest(ServiceRequest request) {
        currentRequest.set(request);
    }
    
    public static ServiceRequest getCurrentRequest() {
        return (ServiceRequest)currentRequest.get();
    }
    
    public static void setCurrentResponse(ServiceResponse response) {
        currentResponse.set(response);
    }
    
    public static ServiceResponse getCurrentResponse() {
        return (ServiceResponse)currentResponse.get();
    }
    
    public void init(ServletConfig config) throws ServletException {
    	AppLoader loader=AppLoader.getInstance();
    	if(loader.isEnabled()) {
    		ClassLoader newLoader=loader.getAppClassLoader();
    		//ClassUtils.setDefaultClassLoader(newLoader);
    		Thread.currentThread().setContextClassLoader(newLoader);
            currentLoader=newLoader;
    	}
        super.init(config);
        synchronized(initLock) {
        	runtime=(ServiceRuntime)getServletContext().getAttribute(ServiceRuntime.class.getName());
        	if(runtime==null) {
        		String servletProp=config.getInitParameter(Constants.PROP_SERVLET_FILE);
        		if(servletProp!=null) {
        			File wsConfFile=PathFactory.getInstance().createPath(servletProp).resolve();
        			try {
        				Configuration srvConf=ConfigurationReader.read(wsConfFile);
        				runtime=new ServiceRuntime();
        				runtime.setConfiguration(srvConf);
        				runtime.setApplicationServiceRegistry(new ServiceRegistry(srvConf,ServiceRegistry.RegistryType.APPLICATION));
        				runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_SOAP,this);
        				runtime.addServiceProcessor(Constants.PROTOCOL_TYPE_JSONWS,new JSONWSProcessor());
        				getServletContext().setAttribute(ServiceRuntime.class.getName(),runtime);
        			} catch(Exception x) {
        				LOG.error("Error while initializing ServiceRuntime",x);
        				throw new ServletException("Error while initializing ServiceRuntime",x);
        			}
        		} else LOG.error("No webservice configuration found!!!");
        	}
        }
    }
    
    protected void sendForbidden(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        res.setContentType("text/html");
        writer.println("<h2>Forbidden!</h2>");
        writer.close();
    }
    
    protected void sendBadRequest(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        res.setContentType("text/html");
        writer.println("<h2>Bad request!</h2>");
        writer.close();
    }
    
    protected void sendError(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        res.setContentType("text/html");
        writer.println("<h2>Internal server error!</h2>");
        writer.close();
    }
 
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
            ClassLoader newLoader=loader.getAppClassLoader();
            //ClassLoader currentLoader=org.apache.axis.utils.ClassUtils.getDefaultClassLoader();
            Thread.currentThread().setContextClassLoader(newLoader);
        
            synchronized(this) {
                if(newLoader!=currentLoader) {
                    if(DEBUG) LOG.debug("Reload Axis Engine.");
                    //ClassUtils.setDefaultClassLoader(newLoader);
                    Thread.currentThread().setContextClassLoader(newLoader);
                    currentLoader=newLoader;
                    axisServer=null;
                    getServletContext().removeAttribute(ATTR_AXIS_ENGINE);
                    getServletContext().removeAttribute(getServletName() + ATTR_AXIS_ENGINE);
                    try {
                        init();
                    } catch(ServletException x) {
                        throw new RuntimeException("Error while reloading Axis",x);
                    }
                }
            }
        }
        
    	try {
    		runtime.process(req,res);
    	} catch(ServiceException x) {
    		throw new ServletException("Error while processing webservice request.",x);
    	}
    }
    
    public void init(ServiceRuntime runtime) {}
    
    public void process(ServiceRequest serviceReq,ServiceResponse serviceRes,ServiceRegistry registry) throws ServiceException {
 
        HttpServletRequest req=(HttpServletRequest)serviceReq.getUnderlyingRequest();
        HttpServletResponse res=(HttpServletResponse)serviceRes.getUnderlyingResponse();
        
        try {
        	setCurrentFault(null);
        	setCurrentRequest(serviceReq);
        	setCurrentResponse(serviceRes);
        	
          	if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null && req.getParameter(Constants.PARAM_SOAP_MESSAGE)!=null) {
        		if(LOG.isDebugEnabled()) LOG.debug("no SOAPAction header, but soapmessage parameter -> iframe method");
        		HttpServletResponse response=res;
        		String reqID=req.getParameter(Constants.PARAM_REQUEST_ID);
        		if(LOG.isDebugEnabled()) if(reqID!=null) LOG.debug("contains requestID parameter: "+reqID);
        		String insPI=req.getParameter("insertpi");
        		if(insPI!=null) response=new InsertPIResponseWrapper(res);
        		if(LOG.isDebugEnabled()) if(insPI!=null) LOG.debug("contains insertpi parameter");
        		super.doPost(new SOAPActionRequestWrapper(req),response);
        	} else if(req.getHeader(Constants.HEADER_SOAP_ACTION)!=null) {
        		if(LOG.isDebugEnabled()) LOG.debug("found SOAPAction header, but no soapmessage parameter -> xmlhttprequest version");
        		String reqID=req.getHeader(Constants.HEADER_REQUEST_ID);
        		if(LOG.isDebugEnabled()) if(reqID!=null) LOG.debug("contains requestID header: "+reqID);
        		if(reqID!=null) res.setHeader(Constants.HEADER_REQUEST_ID,reqID);
        		super.doPost(req,res);
        	} 
        } catch(IOException x) {
        	throw new ServiceException("IOException during service processing.",x);
        } catch(ServletException x) {
        	throw new ServiceException("ServletException during service processing.",x);
        } finally {
        	setCurrentFault(null);
        	setCurrentRequest(null);
        }
    	
    }
    
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession session=req.getSession(false);
        PrintWriter writer = res.getWriter();
        String qs=req.getQueryString();
        if(qs==null) {
            sendBadRequest(req,res,writer);
        } else if(qs.equalsIgnoreCase("WSDL")) {
            if(runtime.getConfiguration().getGlobalServiceConfig().getWSDLSupportEnabled()) {
                String pathInfo=req.getPathInfo();
                String serviceName;
                if (pathInfo.startsWith("/")) {
                    serviceName=pathInfo.substring(1);
                } else {
                    serviceName=pathInfo;
                }
                ServiceConfig conf=runtime.getConfiguration().getServiceConfig(serviceName);
                String type=conf.getSessionType();
                if(type.equals(Constants.SESSION_TYPE_SERVLET) && session==null) {
                    sendForbidden(req,res,writer);
                    return;
                }
                res.setContentType("text/xml");
                String repoPath=runtime.getConfiguration().getGlobalServiceConfig().getWSDLRepository();
                InputStream in=getServletContext().getResourceAsStream(repoPath+"/"+serviceName+".wsdl");
                if(in!=null) {
                    if(type.equals(Constants.SESSION_TYPE_SERVLET)) {
                        StringBuffer sb=new StringBuffer();
                        BufferedInputStream bis=new BufferedInputStream(in);
                        int ch=0;
                        while((ch=bis.read())!=-1) {
                            sb.append((char)ch);
                        }
                        String str=sb.toString();
                        String sid=Constants.SESSION_PREFIX+session.getId();
                        Pattern pat=Pattern.compile("(wsdlsoap:address location=\"[^\"]*)");
                        Matcher mat=pat.matcher(str);
                        sb=new StringBuffer();
                        while(mat.find()) {
                            mat.appendReplacement(sb,mat.group(1)+sid);
                        }
                        mat.appendTail(sb);
                        str=sb.toString();
                        writer.println(str);
                    } else {
                        BufferedInputStream bis=new BufferedInputStream(in);
                        int ch=0;
                        while((ch=bis.read())!=-1) {
                            writer.write(ch);
                        }
                    }
                    writer.close();
                } else sendError(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else if(qs.equalsIgnoreCase("monitor")) {
            if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            	sendMonitor(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else if(qs.equalsIgnoreCase("admin")) {
            if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
                sendAdmin(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else sendBadRequest(req,res,writer);
    }
    
    protected void processAxisFault(AxisFault axisFault) {
        Fault fault=getCurrentFault();
        if(fault==null) {
        	ServiceRequest serviceReq=getCurrentRequest();
        	HttpServletRequest req=(HttpServletRequest)serviceReq.getUnderlyingRequest();
        	LOG.warn(dumpRequest(req,true));
        	Throwable t=axisFault.getCause();
            if(t!=null) LOG.warn(t,t);
        } else {
        	Throwable t=axisFault.getCause();
        	if(t!=null) LOG.error("Exception while processing request",t);
        	String serviceName=fault.getServiceName();
        	Configuration config=runtime.getConfiguration();
        	ServiceConfig serviceConfig=config.getServiceConfig(serviceName);
        	FaultHandler faultHandler=serviceConfig.getFaultHandler();
        	if(faultHandler==null) {
        		GlobalServiceConfig globalConfig=config.getGlobalServiceConfig();
        		faultHandler=globalConfig.getFaultHandler();
        	}
        	if(faultHandler!=null) {
        		fault.setThrowable(t);
        		faultHandler.handleFault(fault);
        		axisFault.setFaultString(fault.getFaultString());
        	}
        }
        axisFault.removeFaultDetail(org.apache.axis.Constants.QNAME_FAULTDETAIL_STACKTRACE);
    }

    private String dumpRequest(HttpServletRequest srvReq,boolean showHeaders) {
		StringBuffer sb=new StringBuffer();
		sb.append(srvReq.getScheme());
		sb.append("://");
		sb.append(srvReq.getServerName());
		sb.append(":");
		sb.append(srvReq.getServerPort());
		sb.append(srvReq.getRequestURI());
		HttpSession session=srvReq.getSession(false);
		if(session!=null) {
			sb.append(Constants.SESSION_PREFIX);
			sb.append(session.getId());
		}
		String s=srvReq.getQueryString();
		if(s!=null&&!s.equals("")) {
			sb.append("?");
			sb.append(s);
		}
		sb.append("\n");
		if(showHeaders) {
			Enumeration headers=srvReq.getHeaderNames();
			while(headers.hasMoreElements()) {
				String header=(String)headers.nextElement();
				String value=srvReq.getHeader(header);
				sb.append(header+": "+value+"\n");
			}
		}
		return sb.toString();
	}
    
    public void sendAdmin(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
      
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
          
            ClassLoader newLoader=loader.getAppClassLoader();
            if(newLoader!=null) {
                //ClassLoader currentLoader=Thread.currentThread().getContextClassLoader();
               
                Thread.currentThread().setContextClassLoader(newLoader);
             
            }
           
        }
        
        //TODO: source out html
        HttpSession session=req.getSession(false);
        if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            writer.println("<html><head><title>Web service admin</title>"+getJS()+getCSS()+"</head><body>");
            writer.println("<div class=\"title\">Web service admin</div><div class=\"content\">");
            try {
                AxisEngine engine=getEngine();
                writer.println("<table>");
                writer.println("<tr><td>");
                Iterator it=engine.getConfig().getDeployedServices();
                while(it.hasNext()) {
                    ServiceDesc desc=(ServiceDesc)it.next();
                    String name=desc.getName();
                    writer.println("<p>");
                    writer.println("<b>"+name+"</b>");
                    String wsdlUri=req.getRequestURI()+"/"+name+";jsessionid="+session.getId()+"?WSDL";
                    writer.println("&nbsp;&nbsp;<a style=\"color:#666666\" target=\"_blank\" href=\""+wsdlUri+"\">WSDL</a>");
                    writer.println("<br/>");
                    ArrayList operations=desc.getOperations();
                    if(!operations.isEmpty()) {
                        writer.println("<ul>");
                        for (Iterator oit = operations.iterator(); oit.hasNext();) {
                            OperationDesc opDesc = (OperationDesc) oit.next();
                            writer.println("<li>" + opDesc.getName());
                        }
                        writer.println("</ul>");
                    }
                     writer.println("</p>");
                    
                }
                writer.println("</td></tr>");
                writer.println("</table");
            } catch(AxisFault fault) {
            
            } catch(ConfigurationException x) {
            
            }
            writer.println("</div></body></html>");
            writer.close();
        } else sendForbidden(req,res,writer);
    }
    
    public void sendMonitor(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        //TODO: source out html
        if(runtime.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            MonitorHistory history=runtime.getMonitor().getMonitorHistory(req);
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            writer.println("<html><head><title>Web service monitor</title>"+getJS()+getCSS()+"</head><body>");
            writer.println("<div class=\"title\">Web service monitor</div><div class=\"content\">");
            writer.println("<table class=\"overview\">");
            writer.println("<tr>");
            writer.println("<th align=\"left\">Start</th>");
            writer.println("<th align=\"left\">Time (in ms)</th>");
            writer.println("<th align=\"left\">Service</th>");
            writer.println("<th align=\"left\">Protocol</th>");
            writer.println("</tr>");
            MonitorRecord[] records=history.getRecords();
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String styleClass="nosel";
                if(i==records.length-1) styleClass="sel";
                writer.println("<tr name=\"row_entry\" class=\""+styleClass+"\" onclick=\"toggleDetails(this,'"+id+"')\">");
                writer.println("<td align=\"left\">"+format.format(new Date(record.getStartTime()))+"</td>");
                writer.println("<td align=\"right\">"+record.getTime()+"</td>");
                writer.println("<td align=\"left\">"+record.getService()+"</td>");
                writer.println("<td align=\"left\">"+record.getProtocol()+"</td>");
                writer.println("</tr>");
            }
            writer.println("</table>");
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String display="none";
                if(i==records.length-1) display="block";
                writer.println("<div name=\"detail_entry\" id=\""+id+"\" style=\"display:"+display+"\">");
                writer.println("<table width=\"100%\">");
                writer.println("<tr>");
                writer.println("<td width=\"50%\"><b>Request:</b><br/><div class=\"body\"><pre>");
                String reqMsg=record.getRequestMessage();
                if(reqMsg==null) reqMsg="Not available";
                writer.println(htmlEscape(reqMsg));
                writer.println("</pre></div></td>");
                writer.println("<td width=\"50%\"><b>Response:</b><br/><div class=\"body\"><pre>");
                String resMsg=record.getResponseMessage();
                if(resMsg==null) resMsg="Not available";
                writer.println(htmlEscape(resMsg));
                writer.println("</pre></div></td>");
                writer.println("</tr>");
                
                writer.println("</table>");
                writer.println("</div");
            }
            writer.println("</div></body></html>");
            writer.close();
        } else sendForbidden(req,res,writer);
    }
    
    private String getJS() {
        //TODO: source out js
        String js=
            "<script type=\"text/javascript\">" +
            "function toggleDetails(src,id) {" +
            "   var elems=document.getElementsByName('row_entry');"+
            "   for(var i=0;i<elems.length;i++) {" +
            "       elems[i].className='nosel';" +
            "   }" +
            "   src.className='sel';" +
            "   elems=document.getElementsByName('detail_entry');"+
            "   for(var i=0;i<elems.length;i++) {" +
            "       elems[i].style.display='none';" +
            "   }" +
            "   var elem=document.getElementById(id);" +
            "   if(elem.style.display=='none') {" +
            "       elem.style.display='block';" +
            "   } else {" +
            "       elem.style.display='none';" +
            "   }" +
            "}" +
            "</script>";
        return js;
    }
    
    private String htmlEscape(String text) {
    	text=text.replaceAll("&","&amp;");
    	text=text.replaceAll("<","&lt;");
    	text=text.replaceAll(">","&gt;");
    	return text;
    }
    
    private String getCSS() {
        //TODO: source out css
        String css=
            "<style type=\"text/css\">" +
            "   body {margin:0pt;border:0pt;background-color:#b6cfe4}" +
            "   div.content {padding:5pt;}" +
            "   div.title {padding:5pt;font-size:18pt;width:100%;background-color:black;color:white}" +
            "   table.overview td,th {padding-bottom:5pt;padding-right:15pt}" +
            "   table.overview tr.nosel {cursor:pointer;color:#000000;}" +
            "   table.overview tr.sel {cursor:pointer;color:#666666;}" +
            "   div.body {width:100%;height:300px;overflow:auto;background-color:#FFFFFF;border:1px solid #000000;}" +
            "   div.headers {width:100%;height:100px;overflow:auto;background-color:#FFFFFF;border:1px solid #000000;}" +
            "</style>";
        return css;
    }

}
