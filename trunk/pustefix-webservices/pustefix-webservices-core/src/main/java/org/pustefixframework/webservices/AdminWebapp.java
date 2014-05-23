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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.pustefixframework.webservices.config.ServiceConfig;
import org.pustefixframework.webservices.monitor.MonitorHistory;
import org.pustefixframework.webservices.monitor.MonitorRecord;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.ClassUtils;


public class AdminWebapp {

    ServiceRuntime runtime;
    
    public AdminWebapp(ServiceRuntime runtime) {
        this.runtime=runtime;
    }
    
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession session=req.getSession(false);
        String qs=req.getQueryString();
        if(qs==null) {
            sendBadRequest(req,res);
        } else if(qs.equalsIgnoreCase("WSDL")) {
            if(runtime.getConfiguration().getGlobalServiceConfig().getWSDLSupportEnabled()) {
                String pathInfo=req.getPathInfo();
                String serviceName;
                int ind = pathInfo.lastIndexOf('/');
                serviceName=pathInfo.substring(ind+1);
                ServiceConfig conf=runtime.getConfiguration().getServiceConfig(serviceName);
                String type=conf.getSessionType();
                if(type.equals(Constants.SESSION_TYPE_SERVLET) && session==null) {
                    sendForbidden(req,res);
                    return;
                }
                res.setContentType("text/xml");
                String repoPath=runtime.getConfiguration().getGlobalServiceConfig().getWSDLRepository();
                InputStream in=session.getServletContext().getResourceAsStream(repoPath+"/"+serviceName+".wsdl");
                if(in!=null) {
                    PrintWriter writer=res.getWriter();
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
                } else sendError(req,res);
            } else sendForbidden(req,res);
        } else if(qs.equalsIgnoreCase("monitor")) {
            if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
                sendMonitor(req,res);
            } else sendForbidden(req,res);
        } else if(qs.equalsIgnoreCase("admin")) {
            if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
                sendAdmin(req,res);
            } else sendForbidden(req,res);
        } else if(req.getParameter("wsscript")!=null) {
            try {
                runtime.getStub(req,res);
            } catch(ServiceException x) {
                throw new ServletException("Can't get webservice stub.",x);
            }
        } else sendBadRequest(req,res);
    }
    
    
    protected void sendForbidden(HttpServletRequest req,HttpServletResponse res) throws IOException {
        PrintWriter writer=res.getWriter();
        res.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        res.setContentType("text/html");
        writer.println("<h2>Forbidden!</h2>");
        writer.close();
    }
    
    protected void sendBadRequest(HttpServletRequest req,HttpServletResponse res) throws IOException {
        PrintWriter writer=res.getWriter();
        res.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        res.setContentType("text/html");
        writer.println("<h2>Bad request!</h2>");
        writer.close();
    }
    
    protected void sendError(HttpServletRequest req,HttpServletResponse res) throws IOException {
        PrintWriter writer=res.getWriter();
        res.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        res.setContentType("text/html");
        writer.println("<h2>Internal server error!</h2>");
        writer.close();
    }
    
    public void sendAdmin(HttpServletRequest req,HttpServletResponse res) throws IOException {  
        PrintWriter writer=res.getWriter();
        
        //TODO: source out html
        HttpSession session=req.getSession(false);
        if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            addHeader(req, writer, "Pustefix webservice admin");
            writer.println("<div class=\"content\">");
            for(ServiceConfig srvConf:runtime.getConfiguration().getServiceConfig()) {
                String name=srvConf.getName();
                writer.println("<div>");
                writer.println("<b>"+name+"</b>");
                if(srvConf.getProtocolType().equals(Constants.PROTOCOL_TYPE_ANY)||
                        srvConf.getProtocolType().equals(Constants.PROTOCOL_TYPE_SOAP)) {
                    String wsdlUri=req.getRequestURI()+"/"+name+";jsessionid="+session.getId()+"?WSDL";
                    writer.println("&nbsp;&nbsp;<a class=\"srvlink\" target=\"_blank\" href=\""+wsdlUri+"\" title=\"Show generated WSDL\">WSDL</a>");
                    String soapUri=req.getRequestURI()+"?wsscript&amp;name="+srvConf.getName()+"&amp;type=soap";
                    writer.println("&nbsp;&nbsp;<a class=\"srvlink\" target=\"_blank\" href=\""+soapUri+"\" title=\"Show generated SOAP Javascript stub\">SOAP JS</a>");
                }
                if(srvConf.getProtocolType().equals(Constants.PROTOCOL_TYPE_ANY)||
                        srvConf.getProtocolType().equals(Constants.PROTOCOL_TYPE_JSONWS)) {
                    String jsonwsUri=req.getRequestURI()+"?wsscript&amp;name="+srvConf.getName()+"&amp;type=jsonws";
                    writer.println("&nbsp;&nbsp;<a class=\"srvlink\" target=\"_blank\" href=\""+jsonwsUri+"\" title=\"Show generated JSONWS Javascript stub\">JSONWS JS</a>");
                }
                writer.println("<br/>");
                try {
                    ServiceDescriptor desc=runtime.getServiceDescriptorCache().getServiceDescriptor(srvConf);
                    writer.println("<ul class=\"srvmeth\">");
                    for(String methName:desc.getMethods()) {
                        for(Method meth:desc.getMethods(methName)) {
                        	Class<?> returnType = meth.getReturnType();
                            writer.println("<li>");
                            writer.println("<span class=\"returntype\">");
                        	writer.println(ClassUtils.getQualifiedName(returnType));
                        	writer.println("</span>");
                        	writer.println("<span class=\"method\">");
                            writer.println(" " + meth.getName() + " (");
                            writer.println("</span>");
                            Class<?>[] paramTypes = meth.getParameterTypes();
                            if(paramTypes.length > 0) {
	                            writer.println("<ul>");
	                            LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
	                            String[] paramNames = discoverer.getParameterNames(meth);
	                            boolean hasParamNames = ( paramNames != null && paramNames.length == paramTypes.length );
	                            for(int i=0; i<paramTypes.length; i++) {
	                            	writer.println("<li>");
	                            	writer.println("<span class=\"paramtype\">");
	                            	writer.println(ClassUtils.getQualifiedName(paramTypes[i]));
	                            	writer.println("</span>");
	                            	if(hasParamNames) {
	                            		writer.print(" ");
	                            		writer.print(paramNames[i]);
	                            	}
	                            	if(i < paramTypes.length -1) writer.print(" , ");
	                            	writer.println("</li>");
	                            } 
	                            writer.println("</ul>");
                            }
                            writer.println(")</li>");
                        }
                    }
                    writer.println("</ul>");
                } catch(ServiceException x) {
                    writer.println("<p style=\"color:red\">ERROR: "+x.getMessage()+"</p>");
                }
                writer.println("</div>");                   
            }
            writer.println("</div></body></html>");
            writer.close();
        } else sendForbidden(req,res);
    }
    
    public void sendMonitor(HttpServletRequest req,HttpServletResponse res) throws IOException {
        PrintWriter writer=res.getWriter();
        //TODO: source out html
        if(runtime.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            MonitorHistory history=runtime.getMonitor().getMonitorHistory(req);
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            addHeader(req, writer, "Pustefix webservice monitor");
            writer.println("<div class=\"content\">");
            writer.println("<table class=\"overview\">");
            writer.println("<tr>");
            writer.println("<th align=\"left\" title=\"Date/time of receipt\">Start</th>");
            writer.println("<th align=\"left\" title=\"Service protocol type\">Protocol</th>");
            writer.println("<th align=\"left\" title=\"Service name\">Service</th>");
            writer.println("<th align=\"left\" title=\"Service method name\">Method</th>");
            writer.println("<th align=\"left\" title=\"Service processing time in ms (including delivery)\">Processing</th>");
            writer.println("<th align=\"left\" title=\"Service method invocation time in ms\">Invocation</th>");
            writer.println("</tr>");
            MonitorRecord[] records=history.getRecords();
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String styleClass="nosel";
                if(i==records.length-1) styleClass="sel";
                writer.println("<tr class=\""+styleClass+"\" onclick=\"toggleDetails(this,'"+id+"')\">");
                writer.println("<td align=\"left\">"+format.format(new Date(record.getStartTime()))+"</td>");
                writer.println("<td align=\"left\">"+record.getProtocol()+"</td>");
                writer.println("<td align=\"left\">"+record.getService()+"</td>");
                writer.println("<td align=\"left\">"+(record.getMethod()==null?"n/a":record.getMethod())+"</td>");
                writer.println("<td align=\"right\">"+(record.getProcessingTime()==-1?"n/a":record.getProcessingTime())+"</td>");
                writer.println("<td align=\"right\">"+(record.getInvocationTime()==-1?"n/a":record.getInvocationTime())+"</td>");
                writer.println("</tr>");
            }
            writer.println("</table>");
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String display="";
                if(i==records.length-1) display=" style=\"display:block;\"";
                writer.println("<div class=\"detailentry\" id=\""+id+"\"" + display + ">");
                writer.println("<table class=\"reqres\">");
                writer.println("<tr><th>Request:</th><th>Response:</th></tr>");
                writer.println("<tr>");
                writer.println("<td><div class=\"body\"><pre>");
                String reqMsg=record.getRequestMessage();
                if(reqMsg==null) reqMsg="Not available";
                writer.println(htmlEscape(reqMsg));
                writer.println("</pre></div></td>");
                writer.println("<td><div class=\"body\"><pre>");
                String resMsg=record.getResponseMessage();
                if(resMsg==null) resMsg="Not available";
                writer.println(htmlEscape(resMsg));
                writer.println("</pre></div></td>");
                writer.println("</tr>");
                
                writer.println("</table>");
                writer.println("</div>");
            }
            writer.println("</div></body></html>");
            writer.close();
        } else sendForbidden(req,res);
    }
    
    private void addHeader(HttpServletRequest req, PrintWriter writer, String title) {
    	writer.println("<!DOCTYPE html>");
    	writer.println("<html><head><title>" + title + "</title>"+getJS()+getCSS(req.getContextPath())+"</head><body>");
        writer.println("<div class=\"header\">");
        writer.println("<div class=\"logo\"><img class=\"logo\" src=\"" + req.getContextPath() + "/modules/pustefix-core/img/logo.png\"/></div>");
        writer.println("<div class=\"pagetitle\">" + title + "</div>");
        writer.println("</div>");
    }
    
    private String getJS() {
        //TODO: source out js
        String js=
            "<script type=\"text/javascript\">" +
            "  function toggleDetails(src,id) {" +
            "    var elems=document.getElementsByTagName('tr');" +
            "    for(var i=0;i<elems.length;i++) {" +
            "      if(elems[i].className=='sel') {" +
            "        elems[i].className='nosel';" +
            "      }" +
            "    }" +
            "    src.className='sel';" +
            "    elems=document.getElementsByTagName('div');" +
            "    for(var i=0;i<elems.length;i++) {" +
            "      if(elems[i].className=='detailentry') {" +
            "        if(elems[i].id==id) {" +
            "          elems[i].style.display='block';" +
            "        } else {" +
            "          elems[i].style.display='none';" +
            "        }" +
            "      }" +
            "    }" +
            "  }" +
            "</script>";
        return js;
    }
    
    private String htmlEscape(String text) {
        text=text.replaceAll("&","&amp;");
        text=text.replaceAll("<","&lt;");
        text=text.replaceAll(">","&gt;");
        return text;
    }
    
    private String getCSS(String contextPath) {
        //TODO: source out css
        String css=
            "<style type=\"text/css\">" +
            "   body {margin:0pt;border:0pt;padding:0pt;background: #fff url(" + contextPath + "/modules/pustefix-core/img/background.png) 0 0 repeat-x;}" +
            "   div.header {clear: both;font-family: sans-serif;min-width: 1024px;}" +
            "   div.header div.pagetitle {float: left;padding-left: 60px;padding-top: 30px;font-size: 36px;color: #fff;font-weight: 100;}" +
            "   div.header div.logo {float: left;padding-left: 10px;margin-bottom:20px;}" +
            "   div.content {clear: both;margin-left: 20px;margin-right: 20px;min-width: 1024px; padding: 10px; background: #fff; border-radius:12px;}" +
            "   div.title {padding:5pt;font-size:18pt;width:100%;background-color:black;color:white}" +
            "   table.overview td,th {padding-bottom:5pt;padding-right:15pt}" +
            "   table.overview tr.nosel {cursor:pointer;color:#666666;}" +
            "   table.overview tr.sel {cursor:pointer;color:#000000;}" +
            "   div.headers {width:500px;height:100px;overflow:auto;background-color:#FFFFFF;border:1px solid #000000;}" +
            "   span.returntype {font-stretch:condensed; color:#333333;}" +
            "   span.paramtype {font-stretch:condensed; color:#333333;}" +
            "   span.method {font-weight:bold;}" +
            "   table.reqres {width:100%; table-layout:fixed;padding-top:10px;}" +
            "   table.reqres td {width:50%; vertical-align:top; border:1px solid #000000;}" +
            "   table.reqres th {text-align:left;}" +
            "   div.body {overflow:auto;background-color:#FFFFFF;padding:5px;}" +
            "   div.detailentry {display:none;}"+
            "   ul.srvmeth {font-size:100%;}" +
            "   ul.srvmeth li {padding-bottom: 10px;}" +
            "   ul.srvmeth ul {list-style-type:none;padding-top: 10px;}" +
            "   a.srvlink {color:#666666;}" +
            "</style>";
        return css;
    }
    
}
