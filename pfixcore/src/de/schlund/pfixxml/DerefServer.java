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

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;

import de.schlund.pfixxml.serverutil.ContainerUtil;

/**
 * This class implements a "Dereferer" servlet to get rid of Referer
 * headers. <b>ALL LINKS THAT GO TO AN OUTSIDE DOMAIN MUST USE THIS SERVLET!</b>
 * If this servlet is bound to e.g. /xml/deref than every outside link
 * (say to www.gimp.org) must be written like <a href="/xml/deref?link=www.gimp.org">Gimp</a>
 *
 *
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

    protected void process(PfixServletRequest preq, HttpServletResponse res)
        throws Exception {
        HttpSession session = preq.getSession(false);
        RequestParam link = preq.getRequestParam("link");
        ContainerUtil conutil = getContainerUtil();

        if (link == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (session == null) {
            DEREFLOG.info(
                preq.getServerName()
                    + "|"
                    + link
                    + "|"
                    + preq.getRequest().getHeader("Referer"));
            res.setHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
            res.setHeader("Pragma", "no-cache");
            res.setHeader(
                "Cache-Control",
                "no-cache, no-store, private, must-revalidate");
            res.setHeader("Location", link.getValue());
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        } else {
            PrintWriter writer = res.getWriter();
            String thelink =
                preq.getScheme()
                    + "://"
                    + preq.getServerName()
                    + ":"
                    + preq.getServerPort()
                    + conutil.getClearedURI(preq, res)
                    + "?link="
                    + link.getValue();
            writer.println("<html><head>");
            writer.println(
                "<meta http-equiv=\"refresh\" content=\"0; URL="
                    + thelink
                    + "\">");
            writer.println(
                "</head><body bgcolor=\"#ffffff\"><a href="
                    + thelink
                    + ">"
                    + thelink
                    + "</a></body></html>");
        }
    }
}
