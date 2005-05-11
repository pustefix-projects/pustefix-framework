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

package de.schlund.pfixxml;

import de.schlund.pfixxml.serverutil.SessionHelper;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

/**
 * This class implements a "Dereferer" servlet to get rid of Referer
 * headers. <b>ALL LINKS THAT GO TO AN OUTSIDE DOMAIN MUST USE THIS SERVLET!</b>
 * If this servlet is bound to e.g. /xml/deref than every outside link
 * (say to http://www.gimp.org) must be written like <a href="/xml/deref?link=http://www.gimp.org">Gimp</a>
 *
 */

public class DerefServer extends ServletManager {
    static protected Category DEREFLOG = Category.getInstance("LOGGER_DEREF");

    protected boolean allowSessionCreate() {
        return (false);
    }

    protected boolean needsSession() {
        return (false);
    }

    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        HttpSession  session = preq.getSession(false);
        RequestParam link    = preq.getRequestParam("link");

        if (link == null || link.getValue() == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (link.getValue().indexOf("\"") != -1) {
            // we don't want \" within link!!
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        OutputStream       out    = res.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, res.getCharacterEncoding());
        if (session == null) {
            DEREFLOG.info(preq.getServerName() + "|" + link + "|" + preq.getRequest().getHeader("Referer"));
            String display = link.getValue();
            display = display.replaceAll("<", "&lt;");
            display = display.replaceAll(">", "&gt;");
            
            if (goodReferer(preq) || isLocalUrl(link.getValue())) {
            writer.write("<html><head>");
            writer.write("<meta http-equiv=\"refresh\" content=\"0; URL=" + link.getValue() + "\">");
            writer.write("</head><body bgcolor=\"#ffffff\"><center><small>");
            writer.write("<a style=\"color:#dddddd;\" href=\"" + link.getValue() + "\">" + display + "</a>");
            writer.write("</small></center></body></html>");
            } else {
                writer.write("<html><head>");
                writer.write("</head><body bgcolor=\"#ffffff\">");                
                writer.write("<h2>You will now enter another website!</h1>");
                writer.write("Please click on the following link:<br/>");                
                writer.write("<a href=\"" + link.getValue() + "\">" + display + "</a>");
                writer.write("</body></html>");                
            }
        } else {
            String thelink = preq.getScheme() + "://" + preq.getServerName() + ":" + preq.getServerPort() +
                SessionHelper.getClearedURI(preq) + "?link=" + link.getValue();
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", thelink);
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        }
        writer.flush();
    }

    private boolean isLocalUrl(String link) {
        
        if (link.startsWith("/")) {
            return true;
        }

        return false;
    }

    private boolean goodReferer(PfixServletRequest preq) {
//        String referer = preq.getRequest().getHeader("Referer");
//        String server = preq.getServerName();
       
//        if (referer == null) {
//            // got no referer, maybe disabled or bad request...
//            return false;
//        }

//        if (referer.startsWith("http://" + server) || referer.startsWith("https://" + server)) {
//        	   // ok, referer and servername match!!
//            return true;
//        }
       
//        return false;

        // EEEEEEK. IE doesn't send a referer in case of a javascript triggered popup... kill it for now.
        return true;
    }
}
