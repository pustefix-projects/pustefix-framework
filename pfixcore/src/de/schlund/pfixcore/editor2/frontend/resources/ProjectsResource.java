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

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.workflow.ContextResource;

/**
 * Context resource providing a list of all projects and methods to select a
 * specific project.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ProjectsResource extends ContextResource {
    /**
     * Selects a project using the supplied name
     * 
     * @param projectName
     *            Name of the project
     * @return <code>true</code> if specified project was found and selected,
     *         <code>false</code> if an error occured during selection (e.g.
     *         project not found)
     */
    boolean selectProject(String projectName);

    /**
     * Returns project that is selected
     * 
     * @return Project object or <code>null</code> if no project is selected
     */
    Project getSelectedProject();

    /**
     * Selects a project using the name of the corresponding
     * {@link de.schlund.pfixxml.targets.TargetGenerator}.
     * 
     * @param targetGenerator
     *            Name of the target generator
     * @return <code>true</code> if specified project was found and selected,
     *         <code>false</code> if an error occured during selection (e.g.
     *         project not found)
     */
    boolean selectProjectByTargetGeneratorName(String targetGenerator);
}
