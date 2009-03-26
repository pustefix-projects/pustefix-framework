package org.pustefixframework.editor.webui.handlers;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.webui.resources.ProjectsResource;
import org.pustefixframework.editor.webui.resources.SessionResource;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.StateImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

/**
 * Describe class LogoutState here.
 *
 *
 * Created: Tue Nov 15 11:27:26 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class LogoutState extends StateImpl {

    SessionResource sessionResource;
    ProjectsResource projectsResource;

    @Override
    public ResultDocument getDocument(Context context, PfixServletRequest req) throws Exception {;
        
        if (!isDirectTrigger(context, req) || isSubmitAuthTrigger(context, req)) {
            // we are not direct triggered or we got authdata, don't do a logout,
        } else {
            sessionResource.logout();
            projectsResource.reset();
        }

        return new ResultDocument();                
    }

    @Inject
    public void setSessionResource(SessionResource sessionResource) {
        this.sessionResource = sessionResource;
    }

    @Inject
    public void setProjectsResource(ProjectsResource projectsResource) {
        this.projectsResource = projectsResource;
    }
}
