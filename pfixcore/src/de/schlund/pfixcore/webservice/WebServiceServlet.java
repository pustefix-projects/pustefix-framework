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
import javax.xml.transform.*;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.utils.Messages;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;

import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Category;

import de.schlund.pfixcore.webservice.config.*;
import de.schlund.pfixcore.webservice.monitor.*;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.targets.TraxXSLTProcessor;

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
    
    private static final String MONITOR_XSL="core/xsl/wsmonitor.xsl";
    
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
            GlobalServiceConfig globConf=wsc.getConfiguration().getGlobalServiceConfig();
            if(globConf.getMonitoringEnabled()) {
                Monitor monitor=new Monitor(globConf.getMonitoringHistorySize(),globConf.getMonitoringScope());
                wsc.setAttribute(Monitor.class.getName(),monitor);
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
    
    

    
    
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
            ClassLoader newLoader=loader.getAppClassLoader();
            if(newLoader!=null) {
                ClassLoader currentLoader=Thread.currentThread().getContextClassLoader();
               
                if(!newLoader.equals(currentLoader)) {
                   
                    Thread.currentThread().setContextClassLoader(newLoader);
                    org.apache.axis.utils.ClassUtils.removeClassLoader(de.schlund.pfixcore.example.webservices.CounterImpl.class.getName());
                    axisServer=null;
                    getServletContext().removeAttribute(ATTR_AXIS_ENGINE);
                    
                }
            }
        }
      
        if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null && req.getParameter(Constants.PARAM_SOAP_MESSAGE)!=null) {
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
            if(session!=null && wsc.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            	sendMonitor(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else if(qs.equalsIgnoreCase("admin")) {
            sendForbidden(req,res,writer);
        } else sendBadRequest(req,res,writer);
    }
    
    public void sendAdmin(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        //TODO: source out html
        
    }
    
    public void sendMonitor(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        //TODO: source out html
        Monitor monitor=(Monitor)wsc.getAttribute(Monitor.class.getName());
        if(monitor!=null && wsc.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            String ip=req.getRemoteAddr();
            MonitorHistory history=monitor.getMonitorHistory(ip);
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            writer.println("<html><head><title>Web service monitor</title>"+getJS()+getCSS()+"</head><body>");
            writer.println("<div class=\"title\">Web service monitor</div><div class=\"content\">");
            writer.println("<table class=\"overview\">");
            writer.println("<tr>");
            writer.println("<th align=\"left\">Start</th>");
            writer.println("<th align=\"left\">Time (in ms)</th>");
            writer.println("<th align=\"left\">Service</th>");
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
                writer.println("<td align=\"left\">"+record.getTarget()+"</td>");
                writer.println("</tr>");
            }
            writer.println("</table");
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String display="none";
                if(i==records.length-1) display="block";
                writer.println("<div name=\"detail_entry\" id=\""+id+"\" style=\"display:"+display+"\">");
                writer.println("<table width=\"100%\">");
                writer.println("<tr>");
                writer.println("<td><b>Request:</b><br/><textarea class=\"xml\">");
                writer.println(record.getRequest());
                writer.println("</textarea></td>");
                writer.println("<td><b>Response:</b><br/><textarea class=\"xml\">");
                writer.println(record.getResponse());
                writer.println("</textarea></td>");
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
            "   textarea.xml {width:100%;height:400px}" +
            "</style>";
        return css;
    }
    
}
