/*
 * de.schlund.pfixcore.webservice.WebServiceServlet
 */
package de.schlund.pfixcore.webservice;

import java.io.*;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.WeakHashMap;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.utils.Messages;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;

import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Category;

import de.schlund.pfixcore.webservice.config.*;

/**
 * WebServiceServlet.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceServlet extends AxisServlet {

    private Category CAT=Category.getInstance(getClass().getName());
    private boolean DEBUG=CAT.isDebugEnabled();
    
    
    private WebServiceContext wsc;
    
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ArrayList al=new ArrayList();
        String common=config.getInitParameter(Constants.PROP_COMMON_FILE);
        if(common!=null) al.add(new File(common));
        String servlet= config.getInitParameter(Constants.PROP_SERVLET_FILE);
        if(servlet!=null) al.add(new File(servlet));
        try {
            File[] propFiles=new File[al.size()];
            for(int i=0;i<al.size();i++) propFiles[i]=(File)al.get(i);
            ConfigProperties cfgProps=new ConfigProperties(propFiles);
            Configuration srvConf=new Configuration(cfgProps);
            wsc=new WebServiceContext(srvConf);
            getServletContext().setAttribute(wsc.getClass().getName(),wsc);
            if(wsc.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
                MonitoringCache cache=new MonitoringCache();
                wsc.setAttribute(cache.getClass().getName(),cache);
            }
        } catch(Exception x) {
            throw new ServletException("Can't get web service configuration",x);
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
    
    protected void sendMonitor(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_OK);
        res.setContentType("text/html");
        writer.println("<html><head><title>Web service monitor</title>"+getCSS()+"</head><body>");
        writer.println("<div class=\"title\">Web service monitor</div><div class=\"content\">");
        HttpSession session=req.getSession(false);
        if(session!=null) {
            MonitoringCache cache=(MonitoringCache)wsc.getAttribute(MonitoringCache.class.getName());
            MonitoringCacheEntry entry=cache.getLastEntry(session);
            if(entry==null) writer.println("<h2>No data available</h2");
            else {
                SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                writer.println("<table border=\"2\">");
                writer.println("<tr><td align=\"left\">Start:</td><td>"+format.format(new Date(entry.getStart()))+"</td></tr>");
                writer.println("<tr><td align=\"left\">Time:</td><td>"+entry.getTime()+" ms</td></tr>");
                writer.println("<tr><td align=\"left\">Service:</td><td>"+entry.getTarget()+"</td></tr>");
                writer.println("</table>");
                writer.println("<div name=\"detail_entry\">");
                writer.println("<table width=\"100%\">");
                writer.println("<tr>");
                writer.println("<td width=\"50%\"><b>Request:</b><br/><textarea style=\"width:100%\" rows=\"25\">");
                writer.println(entry.getRequest());
                writer.println("</textarea></td>");
                writer.println("<td width=\"50%\"><b>Response:</b><br/><textarea style=\"width:100%\" rows=\"25\">");
                writer.println(entry.getResponse());
                writer.println("</textarea></td>");
                writer.println("</tr>");
                writer.println("</table>");
                writer.println("</div");
            }
        }
        writer.println("</div></body></html>");
        writer.close();
    }
    
    private String getCSS() {
        String css=
            "<style type=\"text/css\">" +
            "   body {margin:0pt;border:0pt;background-color:#b6cfe4}" +
            "   div.content {padding:5pt;}" +
            "   div.title {padding:5pt;font-size:18pt;width:100%;background-color:black;color:white}" +
            "   table.overview td,th {padding-bottom:5pt;padding-right:15pt}" +
            "   table.overview tr {cursor:pointer;}" +
            "</style>";
        return css;
    }
    
    
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        System.out.println("POSTSOAP");
        System.out.println(req.getClass().getName());
        System.out.println(req.getHeader(Constants.HEADER_SOAP_ACTION));
        System.out.println(req.getParameter(Constants.PARAM_SOAP_MESSAGE));
        java.util.Enumeration enum=req.getParameterNames();
        while(enum.hasMoreElements()) System.out.println(enum.nextElement());
        if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null && req.getParameter(Constants.PARAM_SOAP_MESSAGE)!=null) {
            System.out.println("FORMSOAP");
            super.doPost(new SOAPActionRequestWrapper(req),res);
        } else {
            super.doPost(req,res);
        }
    }
    
    
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession session=req.getSession(false);
        PrintWriter writer = res.getWriter();
        String qs=req.getQueryString();
        if(qs==null) {
            sendBadRequest(req,res,writer);
        } else if(qs.equalsIgnoreCase("WSDL")) {
            if(wsc.getConfiguration().getGlobalServiceConfig().getWSDLSupportEnabled()) {
                String pathInfo=req.getPathInfo();
                String serviceName;
                if (pathInfo.startsWith("/")) {
                    serviceName=pathInfo.substring(1);
                } else {
                    serviceName=pathInfo;
                }
                ServiceConfig conf=wsc.getConfiguration().getServiceConfig(serviceName);
                String type=conf.getSessionType();
                if(type.equals(Constants.SESSION_TYPE_SERVLET) && session==null) {
                    sendForbidden(req,res,writer);
                    return;
                }
                res.setContentType("text/xml");
                String repoPath=wsc.getConfiguration().getGlobalServiceConfig().getWSDLRepository();
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
            if(wsc.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
                if(session!=null) sendMonitor(req,res,writer);
                else sendForbidden(req,res,writer);
            }
        } else if(qs.equalsIgnoreCase("admin")) {
            
        } else sendBadRequest(req,res,writer);
    }
    
  
    
}
