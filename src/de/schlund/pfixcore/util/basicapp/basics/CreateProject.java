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
package de.schlund.pfixcore.util.basicapp.basics;

import java.io.File;

import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.objects.Project;

/**
 * Building the project with all informations collected by
 * @see de.schlund.pfixcore.util.basicapp.basics.CreateProjectSettings
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */

public class CreateProject {
    /** 
     * The project settings initialized by 
     * @see de.schlund.pfixcore.util.basicapp.basics.CreateProjectSettings 
     */
    private String docRoot  = AppValues.BASICPATH;
    private boolean success = false;
    private String created  = "Folder has been created successfully";
    String projectFolder    = null;
    
    public CreateProject(Project project) {
        projectFolder = project.getProjectName(); 
    }
    
    
    /** Building the Project starts with this method */
    public void runCreateProject() {
        // building the project folders
        buildProjectFolder();
    }
    
    /** Builds the main project folder */
    private void buildProjectFolder() {
        System.out.println("\nCreating project in \"" + docRoot + "\" starts now");  
        System.out.println("\nCreating project folder: " + projectFolder);
        success = new File(docRoot + "/" + projectFolder).mkdir();
        
        if (success) {
            System.out.println(created);
        }
        
        buildSubPrjFolder();
    }
    
    
    /** Method for building the appfolders */
    private void buildSubPrjFolder() {
        String projectpath = docRoot + "/" + projectFolder;        
        System.out.println("\nCreating subfolder starts now");
        
        for (int i = 0; i < AppValues.FOLDERNAMES.length; i++) {
            StringBuffer buffy = new StringBuffer(projectpath);
            buffy.append("/");
            
            if (AppValues.FOLDERNAMES[i].equals(AppValues.TXTFOLDER)) {
                System.out.println("Creating folder " + AppValues.FOLDERNAMES[i] + " & subfolders");
            } else {
                System.out.println("Creating folder " + AppValues.FOLDERNAMES[i] + "...");
            }
            
            buffy.append(AppValues.FOLDERNAMES[i]);
            
            if (AppValues.FOLDERNAMES[i].equals(AppValues.TXTFOLDER)) {
                buffy.append(AppValues.TXTSUBFOLDER);
                success = new File(buffy.toString()).mkdirs();
            } else {
                success = new File(buffy.toString()).mkdir();
            }
            
            if (success) {
                System.out.println(created);
            }
        }       
    }
    
}

