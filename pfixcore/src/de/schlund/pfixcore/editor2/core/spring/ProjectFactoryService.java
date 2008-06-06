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

package de.schlund.pfixcore.editor2.core.spring;

import java.util.Collection;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixxml.targets.TargetGenerator;

/**
 * Provides methods to retrieve a project by name and to retrieve a list of all
 * projects
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.core.dom.Project
 */
public interface ProjectFactoryService {
    /**
     * Returns the Project object for the specified project name.
     * 
     * @param projectName
     *            Name of the project
     * @return Project object for the corresponding project or <code>null</code>
     *         if no project can be found for the specified projectName
     */
    Project getProjectByName(String projectName);

    /**
     * Returns the project that is using the supplied target generator
     * 
     * @param tgen
     *            target generator to look for
     * @return Project matching tgen
     */
    Project getProjectByPustefixTargetGenerator(TargetGenerator tgen);

    /**
     * Returns a list of all projects which are marked as editable
     * 
     * @return List of all editable projects
     * @see Project
     */
    Collection<Project> getProjects();

    /**
     * Returns the project that is using target generator with the supplied name
     * 
     * @param targetGenerator
     *            Name of the {@link TargetGenerator} to look for
     * @return Project matching target generator
     */
    Project getProjectByPustefixTargetGeneratorName(String targetGenerator);
}
