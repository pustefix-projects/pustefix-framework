/*
 * de.schlund.pfixcore.webservice.WebServiceServlet
 */
package de.schlund.pfixcore.webservice;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;


import org.apache.axis.MessageContext;

import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.utils.Messages;



import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Category;

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
    
    
    private boolean MONITOR=true;
    private WeakHashMap monitorMap=new WeakHashMap();
    
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
            WebServiceContext wsc=new WebServiceContext(cfgProps);
            getServletContext().setAttribute(Constants.WEBSERVICE_CONTEXT,wsc);
           
        } catch(Exception x) {
            throw new ServletException("Can't get web service configuration",x);
        }
    }
    
    protected void sendForbidden(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        res.setContentType("text/html");
        writer.println("<h2>Forbidden!</h2>");
    }
    
    protected void sendBadRequest(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        res.setContentType("text/html");
        writer.println("<h2>Bad request!</h2>");
    }
    
    protected void sendMonitor(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        HttpSession session=req.getSession(false);
        if(session!=null) {
            MonitorMessage msg=(MonitorMessage)monitorMap.get(session);
            if(msg!=null) {
                res.setContentType("text/html");
                writer.println("<html><head><title>Web service monitor</title></head><body>");
                writer.println("<h2>Web service monitor</h2>");
                writer.println("<h3>"+msg.getURI()+"</h3>");
                writer.println("<table><tr>");
                writer.println("<td><b>Request:</b><br/><textarea name=\"request\" cols=\"70\" rows=\"20\">");
                writer.println(new String(msg.getRequestBody()));
                writer.println("</textarea></td>");
                writer.println("<td><b>Response:</b><br/><textarea name=\"request\" cols=\"70\" rows=\"20\">");
                writer.println(new String(msg.getResponseBody()));
                writer.println("</textarea></td>");
                writer.println("</tr></table>");
                writer.println("</body></html>");
            }
        } else sendForbidden(req,res,writer);
    }
    
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        if(MONITOR) {
            MonitorRequestWrapper reqWrapper=new MonitorRequestWrapper(req);
            MonitorMessage monMsg=new MonitorMessage(req.getRequestURI());
            monMsg.setRequestBody(reqWrapper.getBytes());
            MonitorResponseWrapper resWrapper=new MonitorResponseWrapper(res);
            super.doPost(reqWrapper,resWrapper);
            monMsg.setResponseBody(resWrapper.getBytes());
            HttpSession session=req.getSession(false);
            if(session!=null) {
                monitorMap.put(session,monMsg);
            }
            OutputStream out=res.getOutputStream();
            out.write(resWrapper.getBytes());
            out.close();
        } else {
            super.doPost(req,res);
        }
    }
    
    
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        PrintWriter writer = res.getWriter();
        HttpSession session=req.getSession(false);
        if(session!=null) {
            String qs=req.getQueryString();
            if(qs!=null) {
                if(qs.startsWith("WSDL")) {
                    String pathInfo=req.getPathInfo();
                    String realpath = getServletContext().getRealPath(req.getServletPath());
                    if (realpath == null) {
                        realpath = req.getServletPath();
                    }
                    String serviceName;
                    if (pathInfo.startsWith("/")) {
                        serviceName = pathInfo.substring(1);
                    } else {
                        serviceName = pathInfo;
                    }
                    res.setContentType("text/xml");
                    StringBuffer sb=new StringBuffer();
                    InputStream in=getServletContext().getResourceAsStream(serviceName+".wsdl");
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
                } else if(MONITOR && qs.equalsIgnoreCase("monitor")) {
                    sendMonitor(req,res,writer);
                } else {
                    sendBadRequest(req,res,writer);
                }
            } else {
                sendBadRequest(req,res,writer);
            }
        } else {
            sendForbidden(req,res,writer);
        }
        writer.close();
    }
    
}
