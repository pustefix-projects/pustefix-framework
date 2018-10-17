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
package org.pustefixframework.pfxinternals;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadAction implements Action {

    private Logger LOG = LoggerFactory.getLogger(ReloadAction.class);

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {

        LOG.info("Scheduled webapp reload.");
        
        String path = req.getContextPath();
        if(path.equals("")) {
            path = "/";
        }
        String reloadURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() +
                "/manager/text/reload?path=" + path;
        String targetURL;
        String page = req.getParameter("page");
        if(page == null) {
            targetURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() +
                "/pfxinternals/actions";
        } else {
            targetURL = req.getRequestURI();
            targetURL = targetURL.replace("pfxinternals", req.getParameter("page"));
        }
        sendReloadPage(targetURL, reloadURL, res);
        return;
    }

    private void sendReloadPage(String targetURL, String reloadURL, HttpServletResponse res) throws IOException {

        res.setContentType("text/html");
        PrintWriter writer = res.getWriter();
        writer.println("<html>");
        writer.println("  <head>");
        writer.println("    <title>Pustefix internals - Reloading webapp</title>");
        writer.println("    <meta http-equiv=\"refresh\" content=\"1; URL=" + targetURL + "\"></meta>");
        writer.println("    <style type=\"text/css\">");
        writer.println("      body {background: white; color: black; font-size:2em;}");
        writer.println("      .centered {position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);}");
        writer.println("      iframe {width:400px; height:50px; visibility:hidden;}");
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
        writer.println("      var interval = window.setInterval(\"showProgress()\", 500);");
        writer.println("       function frameload() {");
        writer.println("         window.clearInterval(interval);");
        writer.println("         document.getElementById(\"status\").style.visibility=\"visible\";");
        writer.println("       } ");
        writer.println("    </script>");
        writer.println("  </head>");
        writer.println("  <body>");
        writer.println("    <div class=\"centered\">");
        writer.println("      <iframe id=\"status\" onload=\"frameload()\" src=\"" + reloadURL + "\"></iframe>");
        writer.println("      <div>");
        writer.println("        Reloading webapp ");
        for(int i=0; i<10; i++) {
            writer.print("<span id=\"" + i + "\">.</span>");
        }
        writer.println("      </div>");
        writer.println("    </div>");
        writer.println("  </body>");
        writer.println("</html>");
        writer.close();
    }

}
