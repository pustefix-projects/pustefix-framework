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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;
import org.apache.axis.encoding.Base64;

/**
 * This class implements a "Dereferer" servlet to get rid of Referer
 * headers. <b>ALL LINKS THAT GO TO AN OUTSIDE DOMAIN MUST USE THIS SERVLET!</b>
 * If this servlet is bound to e.g. /xml/deref than every outside link
 * (say to http://www.gimp.org) must be written like <a href="/xml/deref?link=http://www.gimp.org">Gimp</a>
 *
 */

public class DerefServer extends ServletManager {
    static protected Category DEREFLOG = Category.getInstance("LOGGER_DEREF");
    static protected Category CAT = Category.getInstance(DerefServer.class);
    
    protected boolean allowSessionCreate() {
        return (false);
    }

    protected boolean needsSession() {
        return (false);
    }

    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        RequestParam linkparam    = preq.getRequestParam("link");
        RequestParam enclinkparam = preq.getRequestParam("enclink");
        
        if (linkparam != null && linkparam.getValue() != null) {
            CAT.debug(" ==> Handle link: " + linkparam.getValue());
            handleLink(linkparam.getValue(), preq, res);
            return;
        } else if (enclinkparam != null && enclinkparam.getValue() != null) {
            CAT.debug(" ==> Handle enclink: " + enclinkparam.getValue());
            handleEnclink(enclinkparam.getValue(), preq, res);
            return;
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }


    private void handleLink(String link, PfixServletRequest preq, HttpServletResponse res) throws Exception {
        HttpSession  session = preq.getSession(false);

        link = link.trim();
        if (link == null || link.equals("")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        if (session == null) {
            OutputStream       out     = res.getOutputStream();
            OutputStreamWriter writer  = new OutputStreamWriter(out, res.getCharacterEncoding());
            String             enclink = Base64.encode(link.getBytes("utf8"));
            
            writer.write("<html><head>");
            writer.write("<meta http-equiv=\"refresh\" content=\"0; URL=/xml/deref?enclink=" + enclink + "\">");
            writer.write("</head><body bgcolor=\"#ffffff\"><center><small>");
            writer.write("<a style=\"color:#dddddd;\" href=\"/xml/deref?enclink=" + enclink + "\">" + "---> Redirect --->" + "</a>");
            writer.write("</small></center></body></html>");
            writer.flush();
        } else {
            String thelink = preq.getScheme() + "://" + preq.getServerName() + ":" + preq.getServerPort() +
                SessionHelper.getClearedURI(preq) + "?link=" + link;
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", thelink);
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        }
    }

    private void handleEnclink(String enclink, PfixServletRequest preq, HttpServletResponse res) throws Exception {
        HttpSession  session = preq.getSession(false);
        
        if (session == null) {
            String link =  new String( Base64.decode(enclink), "utf8");
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", link);
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        } else {
            // This problem should have been taken care of already...
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

        
//     private boolean goodReferer(PfixServletRequest preq) {
// //        String referer = preq.getRequest().getHeader("Referer");
// //        String server = preq.getServerName();
       
// //        if (referer == null) {
// //            // got no referer, maybe disabled or bad request...
// //            return false;
// //        }

// //        if (referer.startsWith("http://" + server) || referer.startsWith("https://" + server)) {
// //        	   // ok, referer and servername match!!
// //            return true;
// //        }
       
// //        return false;

//         // EEEEEEK. IE doesn't send a referer in case of a javascript triggered popup... kill it for now.
//         return true;
//     }

//     private boolean isLocalUrl(String link) {
//         if (link.startsWith("/")) {
//             return true;
//         }
//         return false;
//     }

}
