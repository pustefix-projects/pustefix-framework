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

import de.schlund.pfixcore.util.basicapp.helper.AppWorker;
import de.schlund.pfixcore.util.basicapp.objects.Project;

/**
 * Just a main for running the app
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */

public final class InitNewPfixProject {
        
    public static void main(String[] args) {
        // init log4j
        AppWorker.initLogging();
        // The main settings for a new Project
        CreateProjectSettings settings = new CreateProjectSettings();
        settings.runGetSettings();
        // Creating the Project
        CreateProject createPrj = new CreateProject(settings.getCurrentProject());
        createPrj.runCreateProject();
        // copy or prepare the files for their determined folder
        HandleXMLFiles handleFiles = new HandleXMLFiles(settings.getCurrentProject());
        handleFiles.runHandleXMLFiles(); 
        
        System.out.println("\nYour project has been successfully created.");
        System.out.println("To see how it works type in \"ant\".");
        System.out.println("Afterwards restart Apache httpd and Tomcat.");
        System.out.println("Then type in \"http://" + Project.getStaticPrjName() + 
                ".HOSTNAME.DOMAIN\"");
    }
}
