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
/**
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        ServletOutputStream out=res.getOutputStream();
        Monitor monitor=(Monitor)getServletContext().getAttribute(Monitor.class.getName());
        if(monitor!=null) {
            String ip=req.getRemoteAddr();
            MonitorHistory hist=monitor.getMonitorHistory(ip);
            
            HttpRequest[] requests=hist.getEntries();
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            out.println("<html><head><title>HTTP Monitor</title>"+getJS()+getCSS()+"</head><body>");
            out.println("<div class=\"title\">HTTP Monitor</div><div class=\"content\">");
            out.println("<table class=\"overview\">");
            out.println("<tr>");
            out.println("<th align=\"left\">Start</th>");
            out.println("<th align=\"left\">Time (in ms)</th>");
            out.println("<th align=\"left\">Method</th>");
            out.println("<th align=\"left\">URI</th>");
            out.println("</tr>");
            for(int i=0;i<requests.length;i++) {
            	 HttpRequest request=requests[i];
                HttpResponse response=request.getResponse();
                String id="entry"+i;
                out.println("<tr name=\"row_entry\" onclick=\"toggleDetails(this,'"+id+"')\">");
                out.println("<td align=\"left\">"+format.format(request.getDate())+"</td>");
                out.println("<td align=\"right\">"+request.getTime()+"</td>");
                out.println("<td align=\"left\">"+request.getMethod()+"</td>");
                out.println("<td align=\"left\">"+request.getURI()+"</td>");
                out.println("</tr>");
            }
            out.println("</table");
            for(int i=0;i<requests.length;i++) {
                HttpRequest request=requests[i];
                HttpResponse response=request.getResponse();   
                String id="entry"+i;
                out.println("<div name=\"detail_entry\" id=\""+id+"\" style=\"display:none\">");
                out.println("<table>");
                out.println("<tr>");
                out.println("<td><b>Request:</b><br/><textarea cols=\"70\" rows=\"25\">");
                out.println(request.toString());
                out.println("</textarea></td>");
                out.println("<td><b>Response:</b><br/><textarea cols=\"70\" rows=\"25\">");
                out.println(charToEntity(response.toString()));
                out.println("</textarea></td>");
                out.println("</tr>");
                out.println("</table>");
                out.println("</div");
            }
            out.println("</div></body></html>");
        } else sendError(out,"Monitor is disabled");
        out.close();
    }
    
    private String getJS() {
        String js=
            "<script type=\"text/javascript\">" +
            "function toggleDetails(src,id) {" +
            "   var elems=document.getElementsByName('row_entry');"+
            "   for(var i=0;i<elems.length;i++) {" +
            "       elems[i].style.color='black';" +
            "   }" +
            "   src.style.color='#666666';" +
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
*/
}
