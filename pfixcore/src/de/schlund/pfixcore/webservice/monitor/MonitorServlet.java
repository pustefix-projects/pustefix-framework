/*
 * Created on 01.08.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author ml
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MonitorServlet extends HttpServlet {
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException {
        doGet(req,res);
    }

    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        ServletOutputStream out=res.getOutputStream();
        Monitor monitor=(Monitor)getServletContext().getAttribute(Monitor.class.getName());
        if(monitor!=null) {
            String ip=req.getRemoteAddr();
            MonitorHistory hist=monitor.getMonitorHistory(ip);
            HttpRequest[] requests=hist.getEntries();
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            out.println("<html><head><title>HTTP Monitor</title>"+getJS()+"</head><body>");
            out.println("<h2>HTTP Monitor</h2>");
            out.println("<table width=\"100%\">");
            out.println("<tr>");
            out.println("<th align=\"left\">Start</th>");
            out.println("<th align=\"left\">Time (in ms)</th>");
            out.println("<th align=\"left\">Method</th>");
            out.println("<th align=\"left\">URI</th>");
            out.println("<th></th");
            out.println("</tr>");
            for(int i=0;i<requests.length;i++) {
            	HttpRequest request=requests[i];
                HttpResponse response=request.getResponse();
                out.println("<tr>");
                out.println("<td align=\"left\">"+format.format(request.getDate())+"</td>");
                out.println("<td align=\"right\">"+request.getTime()+"</td>");
                out.println("<td align=\"left\">"+request.getMethod()+"</td>");
                out.println("<td align=\"left\">"+request.getURI()+"</td>");
                String id="entry"+i;
                out.println("<td align=\"left\"><a href=\"javascript:toggleDetails('"+id+"')\">Details</a></td>");
                out.println("</tr>");
                out.println("<tr>");
                out.println("<td id=\""+id+"\" colspan=\"5\" style=\"display:none\"><nobr><textarea cols=\"70\" rows=\"10\">");
                out.println(request.toString());
                out.println("</textarea>");
                out.println("<textarea cols=\"70\" rows=\"10\">");
                out.println(charToEntity(response.toString()));
                out.println("</textarea></nobr></td>");
                out.println("</tr>");
            }
            out.println("</table>");
            out.println("</body></html>");
        } else sendError(out,"Monitor is disabled");
        out.close();
    }
    
    private String getJS() {
        String js=
            "<script type=\"text/javascript\">" +
            "function toggleDetails(id) {" +
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
    
    private void sendError(ServletOutputStream out,String msg) throws IOException {
    	out.println(msg);
    }
    
    public static String charToEntity(String str) {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<str.length();i++) {
                char ch=str.charAt(i);
                if(ch=='&') {
                        sb.append("&amp;");
                } else if(ch=='<') {
                        sb.append("&lt;");
                } else if(ch=='>') {
                        sb.append("&gt;");
                } else if(ch=='"') {
                        sb.append("&quot;");
                } else {
                        sb.append(ch);
                }
        }
        return sb.toString();
}

    
}
