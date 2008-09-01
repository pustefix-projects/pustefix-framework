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

package de.schlund.pfixcore.util.basicapp.objects;

import java.util.ArrayList;

import org.apache.log4j.Logger;


/**
 * This class represents all informations for 
 * a new project
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */

public final class Project {
    private static final Logger LOG = Logger.getLogger(Project.class);
    
    /** A String for the projectname */
    private String projectName    = null;
    /** A String for the language */
    private String language       = null;
    /** A String project description */
    private String description    = null;
    /** A static field for the projectname */
    private static String PRJNAME = null;
    
    //  --> Start getter and setter
    /**
     * @return Returns the language.
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * @param language The language to set.
     */
    public void setLanguage(String language) {
        LOG.debug("Setting default language: " + language);
        this.language = language;
    }
    
    /**
     * @return Returns the projectName.
     */
    public String getProjectName() {
        return projectName;
    }
    
    /** A getter for the static variable */
    public static String getStaticPrjName() {
        return PRJNAME;
    }
    
    /**
     * @param projectName The projectName to set.
     */
    public void setProjectName(String projectName) {
        LOG.debug("Setting the projectname: " + projectName);
        this.projectName = projectName;
        PRJNAME          = projectName;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
  
}
