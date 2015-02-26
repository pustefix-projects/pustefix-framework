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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
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

import de.schlund.pfixcore.beans.BeanDescriptor;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;


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
        } else if(qs.startsWith("test")) {
            if(session!=null && runtime.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
                sendTest(req,res);
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
            res.setCharacterEncoding("utf-8");
            res.setContentType("text/html");
            addHeader(req, res, writer, "admin");
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
                            writer.println(" " + meth.getName() + " ");
                            writer.println("</span>");
                            writer.println("(");
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
                            writer.println(") ");
                            String href = req.getContextPath() + "/webservice?test&amp;service=" + srvConf.getName() + "&amp;method=" + meth.getName();
                            String idSuffix = srvConf.getName() + "_" + meth.getName();
                            
                            writer.println("<div class=\"runtest\" onclick=\"javascript:openTest('" + href + "','" + idSuffix + "');\" title=\"Test webservice call\">");
                            writer.println("Test");
                            writer.println("</div>");
                            
                            writer.println("</li>");
                            writer.println("<div id=\"container_" + idSuffix + "\">");
                           
                            writer.println("</div>");
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
    
    public void sendTest(HttpServletRequest req,HttpServletResponse res) throws IOException {  
        
        String service = req.getParameter("service");
        String method = req.getParameter("method");
        if(service != null && method != null) {
            ServiceConfig config = runtime.getConfiguration().getServiceConfig(service);
            if(config != null) {
                res.setContentType("text/html");
                res.setCharacterEncoding("utf-8");
                PrintWriter writer = res.getWriter();
                writer.println("<html>");
                writer.println("<head>");
                String jsUri= req.getContextPath() + "/modules/pustefix-core/script/httpRequest.js";
                writer.println("<script type=\"text/javascript\" src=\"" + jsUri + "\"></script>");
                jsUri= req.getContextPath() + "/modules/pustefix-webservices-jsonws/script/webservice_json.js";
                writer.println("<script type=\"text/javascript\" src=\"" + jsUri + "\"></script>");
                String jsonwsUri=req.getRequestURI()+"?wsscript&amp;name="+config.getName()+"&amp;type=jsonws";
                writer.println("<script type=\"text/javascript\" src=\"" + jsonwsUri + "\"></script>");
                writer.println(getJS(req, "test", true));
                writer.println(getCSS(req, "test", true));
                writer.println("</head>");
                writer.println("<body>");
                writer.println("<textarea id=\"run\" rows=\"10\" cols=\"80\" spellcheck=\"false\">");
                
                ServiceDescriptor desc;
                try {
                    desc = new ServiceDescriptor(config);
                } catch (ServiceException e) {
                    throw new RuntimeException("Can't get service descriptor", e);
                }
                String jsClassName=null;
                if(config.getStubJSNamespace().equals(Constants.STUBGEN_JSNAMESPACE_COMPAT)) {
                    jsClassName=Constants.STUBGEN_DEFAULT_JSNAMESPACE+config.getName();
                } else if(config.getStubJSNamespace().equals(Constants.STUBGEN_JSNAMESPACE_COMPATUNIQUE)) {
                    jsClassName=Constants.STUBGEN_JSONWS_JSNAMESPACE+config.getName();
                } else if(config.getStubJSNamespace().equals(Constants.STUBGEN_JSNAMESPACE_JAVANAME)) {
                    jsClassName=desc.getServiceClass().getName();
                } else {
                    String prefix=config.getStubJSNamespace();
                    if(prefix.contains(".")&&!prefix.endsWith(".")) prefix+=".";
                    jsClassName=prefix+config.getName();
                }
                
                writer.println("function serviceCallback(result, requestId, exception) {");
                writer.println("  alert(result != null ? JSON.stringify(result) : exception);");
                writer.println("}");
                writer.println("var service = new " + jsClassName + "();");
                Method meth = desc.getMethods(method).get(0);
                Class<?>[] paramTypes = meth.getParameterTypes();
                StringBuilder paramList = new StringBuilder();
                for(int i=0; i<paramTypes.length; i++) {
                    if(i>0) {
                        paramList.append(", ");
                    }
                    paramList.append(getParameterValue(paramTypes[i], 0));
                }
                String params = paramList.toString();
                if(params.length() > 0) {
                    params += ", ";
                }
                writer.println("service." + method + "(" + params + "serviceCallback);");
                
                writer.println("</textarea>");
                writer.println("<input name=\"run\" type=\"button\" value=\"Run\" onclick=\"runJS();\">");
                writer.println("</body>");
                writer.println("</html>");
                writer.close();
            }
            
        }
        
    }
    
    private String getParameterValue(Class<?> paramType, int depth) {
        if(String.class.isAssignableFrom(paramType)) {
            return "'foo'";
        } else if(paramType.isPrimitive()) {
            if(paramType == boolean.class) {
                return "true";
            } else if(paramType == char.class) {
                return "a";
            } else {
                return "1";
            }
        } else if(paramType.isArray()) {
            if(depth < 3) {
                return "[" + getParameterValue(paramType.getComponentType(), depth + 1) + "]";
            } else {
                return "[]";
            }
        } else if(paramType == Boolean.class) {
            return "true";
        } else if(paramType == Character.class) {
            return "'a'";
        } else if(Number.class.isAssignableFrom(paramType)) {
            return "1";
        } else if(Collection.class.isAssignableFrom(paramType)) {
            return "[]";
        } else if(paramType == Date.class || Calendar.class.isAssignableFrom(paramType)) {
            return "new Date()";
        } else {
            StringBuilder sb = new StringBuilder();
            if(depth < 3) {
                sb.append("{");
                BeanDescriptor desc = new BeanDescriptor(paramType);
                Set<String> props = desc.getReadableProperties();
                for(String prop: props) {
                    Type targetType = desc.getPropertyType(prop);
                    Class<?> targetClass = null;
                    if (targetType instanceof Class)
                        targetClass = (Class<?>) targetType;
                    else if (targetType instanceof ParameterizedType) {
                        Type rawType = ((ParameterizedType) targetType).getRawType();
                        if (rawType instanceof Class)
                            targetClass = (Class<?>) rawType;
                    }
                    if(targetClass != null) {
                        if(sb.length() > 1) {
                            sb.append(", ");
                        }
                        sb.append("'").append(prop).append("':");
                        sb.append(getParameterValue(targetClass, depth + 1));
                    }
                }
                sb.append("}");
                return sb.toString();
            }
            return sb.toString();
        }
    }
    
    public void sendMonitor(HttpServletRequest req,HttpServletResponse res) throws IOException {
        PrintWriter writer=res.getWriter();
        //TODO: source out html
        if(runtime.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            MonitorHistory history=runtime.getMonitor().getMonitorHistory(req);
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            addHeader(req, res, writer, "monitor");
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
    
    private void addHeader(HttpServletRequest req, HttpServletResponse res, PrintWriter writer, String page) throws IOException {
    	String title = "Pustefix webservice " + page;
        writer.println("<!DOCTYPE html>");
    	writer.println("<html><head><title>" + title + "</title>");
    	writer.println(getJS(req, page, false));
    	writer.println(getCSS(req, page, false));
    	writer.println("</head><body>");
    	writer.println("");
        writer.println("<div class=\"header\">");
        writer.println("<div class=\"logo\"><img class=\"logo\" src=\"" + req.getContextPath() + "/modules/pustefix-core/img/logo.png\"/></div>");
        writer.println("<div class=\"pagetitle\">" + title + "</div>");
        String targetPage = "monitor";
        if(page.equals("monitor")) {
            targetPage = "admin";
        }
        String target = "pfixcore_web_service_" + targetPage;
        String href = res.encodeURL(req.getContextPath() + "/webservice?" + targetPage);
        writer.println("<div class=\"navi\">");
        writer.println("<a href=\"" + href + "\" class=\"topnavlink\" target=\"" + target + "\">Webservice " + targetPage + "</a>");
        writer.println("</div>");
        writer.println("</div>");   
    }

    private String htmlEscape(String text) {
        text=text.replaceAll("&","&amp;");
        text=text.replaceAll("<","&lt;");
        text=text.replaceAll(">","&gt;");
        return text;
    }
    
    private String getCSS(HttpServletRequest request, String page, boolean pageOnly) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<style type=\"text/css\">");
        if(!pageOnly) {
            Resource res = ResourceUtil.getResource("module://pustefix-webservices-core/admin/common.css");
            if(res.exists()) {
                sb.append(replaceVariables(loadResource(res.getInputStream()), request));
            }
        }
        Resource res = ResourceUtil.getResource("module://pustefix-webservices-core/admin/" + page + ".css");
        if(res.exists()) {
            sb.append(replaceVariables(loadResource(res.getInputStream()), request));
        }
        sb.append("</style>");
        return sb.toString();
    }
    
    private String getJS(HttpServletRequest request, String page, boolean pageOnly) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\">");
        if(!pageOnly) {
            Resource res = ResourceUtil.getResource("module://pustefix-webservices-core/admin/common.js");
            if(res.exists()) {
                sb.append(replaceVariables(loadResource(res.getInputStream()), request));
            }
        }
        Resource res = ResourceUtil.getResource("module://pustefix-webservices-core/admin/" + page + ".js");
        if(res.exists()) {
            sb.append(replaceVariables(loadResource(res.getInputStream()), request));
        }
        sb.append("</script>");
        return sb.toString();
    }

    private static String loadResource(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, "utf8");
        StringBuffer strBuf = new StringBuffer();
        char[] buffer = new char[4096];
        int i = 0;
        try {
            while ((i = reader.read(buffer)) != -1)
                strBuf.append(buffer, 0, i);
        } finally {
            in.close();
        }
        return strBuf.toString();
    }

    private static String replaceVariables(String content, HttpServletRequest request) {
        String contextPath = request.getContextPath();
        content = content.replaceAll("\\$\\{contextPath\\}", contextPath);
        return content;
    }

}
