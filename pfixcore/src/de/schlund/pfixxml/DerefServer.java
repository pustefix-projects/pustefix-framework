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
import de.schlund.pfixxml.util.MD5Utils;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.axis.encoding.Base64;
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
    static protected Category CAT      = Category.getInstance(DerefServer.class);
    private static String     rand     = ""+Math.random();
    
    
    protected boolean allowSessionCreate() {
        return (false);
    }

    protected boolean needsSession() {
        return (false);
    }

    protected void process(PfixServletRequest preq, HttpServletResponse res) throws Exception {
        RequestParam linkparam    = preq.getRequestParam("link");
        RequestParam enclinkparam = preq.getRequestParam("enclink");
        RequestParam signparam    = preq.getRequestParam("sign");
        
        HttpServletRequest req     = preq.getRequest();
        String             referer = req.getHeader("Referer");

        CAT.debug("===> sign key: " + rand);
        CAT.debug("===> Referer: " + referer);
        
        if (linkparam != null && linkparam.getValue()              != null) {
            CAT.debug(" ==> Handle link: " + linkparam.getValue());
            handleLink(linkparam.getValue(), preq, res);
            return;
        } else if (enclinkparam != null && enclinkparam.getValue() != null &&
                   signparam != null && signparam.getValue() != null) {
            CAT.debug(" ==> Handle enclink: " + enclinkparam.getValue());
            CAT.debug("     with sign: " + signparam.getValue());
            handleEnclink(enclinkparam.getValue(), signparam.getValue(), preq, res);
            return;
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }


    private void handleLink(String link, PfixServletRequest preq, HttpServletResponse res) throws Exception {
        link = link.trim();
        if (link == null || link.equals("")) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        OutputStream       out      = res.getOutputStream();
        OutputStreamWriter writer   = new OutputStreamWriter(out, res.getCharacterEncoding());
        String             enclink  = Base64.encode(link.getBytes("utf8"));
        String             reallink = preq.getScheme() + "://" + preq.getServerName() + ":" + preq.getServerPort() +
            SessionHelper.getClearedURI(preq) + "?enclink=" + URLEncoder.encode(enclink, "utf8") +
            "&sign=" + MD5Utils.hex_md5(enclink+rand, "utf8");
        
        CAT.debug("===> Meta Refresh to link: " + reallink);
        
        writer.write("<html><head>");
        writer.write("<meta http-equiv=\"refresh\" content=\"0; URL=" + reallink +  "\">");
        writer.write("</head><body bgcolor=\"#ffffff\"><center>");
        writer.write("<a style=\"color:#bbbbbb;\" href=\"" + reallink + "\">" + "---> Redirect --->" + "</a>");
        writer.write("</center></body></html>");
        writer.flush();
    }

    private void handleEnclink(String enclink, String sign, PfixServletRequest preq, HttpServletResponse res) throws Exception {
        if (MD5Utils.hex_md5(enclink+rand, "utf8").equals(sign)) {
            String link = new String( Base64.decode(enclink), "utf8");
            CAT.debug("===> Relocate to link: " + link);
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader("Cache-Control", "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", link);
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        } else {
            CAT.warn("===> Won't relocate because signature is wrong.");
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
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
