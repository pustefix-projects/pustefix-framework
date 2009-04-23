/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.editor.backend.config;


/**
 * Stores information (like name and description) about a project.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorProjectInfo {
    private String name;
    private String description;
    private boolean enableTargetUpdateService;
    
    /**
     * Sets the name of the project.
     * 
     * @param name project name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns name of the project.
     * 
     * @return project name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets description of the project.
     * 
     * @param description project description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Returns description of the project.
     * 
     * @return project description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the flag indicating whether the service peridically updating
     * all TargetGenerator targets should be enabled.
     * 
     * @param enableTargetUpdateService <code>true</code> to enable
     *   <code>false</code> to disable the target update service
     */
    public void setEnableTargetUpdateService(boolean enableTargetUpdateService) {
        this.enableTargetUpdateService = enableTargetUpdateService;
    }

    /**
     * Returns flag indicating whether the service peridically updating
     * all TargetGenerator targets should be enabled.
     * 
     * @return enabled state for the target update service
     */
    public boolean isEnableTargetUpdateService() {
        return enableTargetUpdateService;
    }
}
