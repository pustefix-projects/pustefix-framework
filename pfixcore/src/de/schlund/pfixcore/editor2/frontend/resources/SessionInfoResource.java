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
 */

package de.schlund.pfixcore.editor2.frontend.resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.w3c.dom.Element;

import de.schlund.pfixcore.beans.InsertStatus;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.spring.UserManagementService;
import de.schlund.pfixcore.editor2.core.vo.EditorUser;
import de.schlund.pfixcore.editor2.frontend.util.ContextStore;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;

public class SessionInfoResource {
    private UserManagementService usermanagement;
    
    @Inject
    public void setUserManagementService(UserManagementService usermanagement) {
        this.usermanagement = usermanagement;
    }

    @InsertStatus
    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        SessionAdmin sessadmin = SessionAdmin.getInstance();
        DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Map<Context, String> contextmap = ContextStore.getInstance().getContextMap();

        Set<String> allsessionids = new LinkedHashSet<String>(sessadmin.getAllSessionIds());
        for (Iterator<String> i = allsessionids.iterator(); i.hasNext();) {
            String sessId = i.next();
            try {
                SessionInfoStruct info = sessadmin.getInfo(sessId);
                if (info != null) {
                    Element sessionNode = resdoc.createSubNode(elem, "session");
                    String visitId = (String) info.getSession().getAttribute(AbstractPustefixRequestHandler.VISIT_ID);

                    // This is very dirty, but there is no other way to do
                    // this task: We iterate through all known contexts and
                    // look if one of them has the same visit id as the
                    // session
                    for (Iterator<Context> j = contextmap.keySet().iterator(); j.hasNext();) {
                        Context foreignctx = j.next();
                        if (foreignctx.getVisitId().equals(visitId)) {
                            String username = contextmap.get(foreignctx);
                            sessionNode.setAttribute("username", username);
                            EditorUser userinfo = usermanagement.getUser(username);
                            if (userinfo != null) {
                                sessionNode.setAttribute("userphone", userinfo.getPhoneNumber());
                                sessionNode.setAttribute("userfullname", userinfo.getFullname());
                            }
                            if (EditorResourceLocator.getSessionResource(foreignctx).isInIncludeEditView()) {
                                IncludePartThemeVariant incPart = EditorResourceLocator.getIncludesResource(foreignctx).getSelectedIncludePart();
                                if (incPart != null) {
                                    sessionNode.setAttribute("incpart", incPart.toString());
                                }
                            }
                        }
                    }
                    sessionNode.setAttribute("id", info.getSessionIdURI());
                    sessionNode.setAttribute("created", dateformat.format(new Date(info.getData().getCreation())));
                    sessionNode.setAttribute("lastAccess", dateformat.format(new Date(info.getData().getLastAccess())));
                    sessionNode.setAttribute("requestCount", Long.toString(info.getNumberOfHits()));

                    Collection<SessionInfoStruct.TrailElement> trail = info.getTraillog();
                    SessionInfoStruct.TrailElement lastStep = null;
                    Element stepNode = null;
                    for (Iterator<SessionInfoStruct.TrailElement> i2 = trail.iterator(); i2.hasNext();) {
                        SessionInfoStruct.TrailElement step = i2.next();
                        if (lastStep != null && lastStep.getStylesheetname().equals(step.getStylesheetname()) && lastStep.getServletname().equals(step.getServletname())) {
                            stepNode.setAttribute("counter", Integer.toString(Integer.parseInt(stepNode.getAttribute("counter")) + 1));
                        } else {
                            stepNode = resdoc.createSubNode(sessionNode, "step");
                            stepNode.setAttribute("stylesheet", step.getStylesheetname());
                            stepNode.setAttribute("counter", "1");
                        }
                        lastStep = step;
                    }
                }
            } catch (IllegalStateException e) {
                // Ignore and go on
            }
        }
    }

    public void reset() throws Exception {
    // Do nothing
    }

}
