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

package de.schlund.pfixcore.editor.handlers;

import java.util.*;
import java.text.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.resources.*;
import javax.servlet.http.*;
import org.w3c.dom.*;
/**
 * @author jtl
 *
 *
 */
public class SessionManagerState extends StateImpl {
    
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        ContextResourceManager crm         = context.getContextResourceManager();
        ResultDocument         resdoc      = new ResultDocument();
        SessionAdmin           sessadmin   = SessionAdmin.getInstance();
        String                 editorcname = context.getName();
        
        Element root  = resdoc.createNode("allsessions");
        int     count = 0;
        for (Iterator i = sessadmin.getAllSessionIds().iterator(); i.hasNext(); ) {
            String sessid = (String) i.next();
            try {
                SessionInfoStruct info     = sessadmin.getInfo(sessid);
                HttpSession       sess     = info.getSession();
                LinkedList        trail    = info.getTraillog();
                SimpleDateFormat  dateform = new SimpleDateFormat("d.M.yy, HH:mm:ss");
                Element           sesselem = resdoc.createSubNode(root, "session");
                sesselem.setAttribute("num", "" + count++);
                sesselem.setAttribute("id", info.getSessionIdURI());
                sesselem.setAttribute("sessid", info.getSession().getId());
                sesselem.setAttribute("created", "" + info.getCreationTime());
                sesselem.setAttribute("createdtext", dateform.format(new Date(info.getCreationTime())));
                sesselem.setAttribute("lastaccess", "" + info.getLastAccess());
                sesselem.setAttribute("hits", "" + info.getNumberOfHits());
                if (info.getLastAccess() != -1) {
                    sesselem.setAttribute("lastaccesstext", dateform.format(new Date(info.getLastAccess())));
                }

                Element stepelem = null;
                if (trail != null && trail.size() > 0) {
                    for (Iterator j = trail.listIterator(); j.hasNext(); ) {
                        SessionInfoStruct.TrailElement step = (SessionInfoStruct.TrailElement) j.next();
                        if (stepelem != null &&
                            stepelem.getAttribute("stylesheet").equals(step.getStylesheetname()) &&
                            stepelem.getAttribute("servlet").equals(step.getServletname())) {
                            stepelem.setAttribute("mult", "" + (((new Long(stepelem.getAttribute("mult"))).longValue()) + 1));
                        } else {
                            stepelem = resdoc.createSubNode(sesselem, "step");
                            stepelem.setAttribute("servlet", step.getServletname());
                            stepelem.setAttribute("stylesheet", step.getStylesheetname());
                            stepelem.setAttribute("counter", "" + step.getCounter());
                            stepelem.setAttribute("mult", "1");
                        }
                    }
                }
                Object ctmp = sess.getAttribute(editorcname);
                if (ctmp instanceof AuthContext) {
                    ContextResourceManager manager = ((AuthContext) ctmp).getContextResourceManager();
                    if (manager != null) {
                        EditorSessionStatus newcpr = (EditorSessionStatus) manager.getResource(EditorRes.ESESSION);
                        if (newcpr != null) {
                            EditorUser theuser = newcpr.getUser();
			    if (theuser != null) {
				sesselem.setAttribute("editoruserid", theuser.getId());
				sesselem.setAttribute("editorusername", theuser.getUserInfo().getName());
				sesselem.setAttribute("editoruserphone", theuser.getUserInfo().getPhone());
			    }
                            EditorProduct newprod = newcpr.getProduct();
                            if (newprod != null) {
                                sesselem.setAttribute("editorproduct", newprod.getComment());
                            } else {
                                sesselem.setAttribute("editorproduct", "N/A");
                            }
                        }
                    }
                }
            } catch (IllegalStateException e) {
                i.remove();
            }
        }
        return resdoc;
    }
}
