package org.pustefixframework.pfxinternals;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.pustefixframework.admin.mbeans.Admin;

import de.schlund.pfixxml.serverutil.SessionAdmin;

public class ReloadAction implements Action {

    private Logger LOG = Logger.getLogger(ReloadAction.class);
    
    private long startTime = System.currentTimeMillis();
    private long reloadTimeout = 1000 * 5;
    
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
        
        SessionAdmin sessionAdmin = pageContext.getApplicationContext().getBean(SessionAdmin.class);
        
        if((System.currentTimeMillis() - startTime) > reloadTimeout) {
            LOG.info("Scheduled webapp reload.");
            try {
                ObjectName mbeanName = new ObjectName(Admin.JMX_NAME);
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                if(server != null && server.isRegistered(mbeanName)) {
                    sessionAdmin.invalidateSessions();
                    File workDir = (File)pageContext.getServletContext().getAttribute("javax.servlet.context.tempdir");
                    if(workDir != null) {
                        int port = (Integer)server.getAttribute(mbeanName, "Port");
                        Socket sock = new Socket("localhost", port);
                        OutputStream out = sock.getOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
                        writer.println("reload");
                        writer.println(workDir.getPath());
                        writer.close();
                    } else {
                        LOG.warn("Missing servlet context attribute 'javax.servlet.context.tempdir'.");
                    }
                } else {
                    LOG.warn("Can't do reload because Admin mbean isn't available.");
                }
            } catch(Exception x) {
                LOG.error("Error during webapp reload", x);
            }
        } else {
            LOG.warn("Skipped repeated webapp reload scheduling.");
        }
        String page = req.getParameter("page");
        if(page == null) {
            res.sendRedirect(req.getContextPath() + "/pfxinternals/actions");
        } else {
            sendReloadPage(req, res);
        }
        return;
        
    }
    
    private void sendReloadPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        PrintWriter writer = res.getWriter();
        writer.println("<html>");
        writer.println("  <head>");
        writer.println("    <title>Pustefix internals - Reloading webapp</title>");
        String url = req.getRequestURI();
        url = url.replace("pfxinternals", req.getParameter("page"));
        writer.println("    <meta http-equiv=\"refresh\" content=\"1; URL=" + url + "\"></meta>");
        writer.println("    <style type=\"text/css\">");
        writer.println("      body {background: white; color: black;}");
        writer.println("      table {width: 100%; height: 100%;}");
        writer.println("      td {text-align: center; vertical-align: middle; font-size:150%; font-style:italic; font-family: serif;}");
        writer.println("      span {color:white;}");
        writer.println("    </style>");
        writer.println("    <script type=\"text/javascript\">");
        writer.println("      var no = -1;");
        writer.println("      function showProgress() {");
        writer.println("        no++;");
        writer.println("        if(no == 10) {");
        writer.println("          no = 0;");
        writer.println("          for(var i=0; i<10; i++) document.getElementById(i).style.color = \"white\";");
        writer.println("        }");
        writer.println("        document.getElementById(no).style.color = \"black\";");
        writer.println("      }");
        writer.println("      window.setInterval(\"showProgress()\", 500);");
        writer.println("    </script>");
        writer.println("  </head>");
        writer.print("<body><table><tr><td>");
        writer.print("Reloading webapp ");
        for(int i=0; i<10; i++) {
            writer.print("<span id=\"" + i + "\">.</span>");
        }
        writer.println("</td></tr></table></body></html>");
        writer.close();
    }

}
