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

package de.schlund.pfixcore.editor2.frontend.util;

import de.schlund.pfixcore.editor2.frontend.resources.PagesResource;
import de.schlund.pfixcore.editor2.frontend.resources.ProjectsResource;
import de.schlund.pfixcore.editor2.frontend.resources.SessionResource;
import de.schlund.pfixcore.editor2.frontend.resources.TargetsResource;
import de.schlund.pfixcore.workflow.Context;

/**
 * Provides helper methods to get a ContextResource
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class EditorResourceLocator {
    /**
     * Returns SessionResource implementation 
     * 
     * @param context Context to use
     * @return SessionResource for context
     */
    public final static SessionResource getSessionResource(Context context) {
        return (SessionResource) context.getContextResourceManager()
                .getResource(SessionResource.class.getName());
    }
    
    /**
     * Returns ProjectsResource Implementation
     * 
     * @param context Context to use
     * @return ProjectsResource for context
     */
    public final static ProjectsResource getProjectsResource(Context context) {
        return (ProjectsResource) context.getContextResourceManager()
                .getResource(ProjectsResource.class.getName());
    }
    
    /**
     * Returns PagesResource Implementation
     * 
     * @param context Context to use
     * @return PagesResource for context
     */
    public final static PagesResource getPagesResource(Context context) {
        return (PagesResource) context.getContextResourceManager()
                .getResource(PagesResource.class.getName());
    }
    
    /**
     * Returns TargetsResource Implementation
     * 
     * @param context Context to use
     * @return TargetsResource for context
     */
    public final static TargetsResource getTargetsResource(Context context) {
        return (TargetsResource) context.getContextResourceManager()
                .getResource(TargetsResource.class.getName());
    }
}
