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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.helper.StringUtils;
import de.schlund.pfixcore.util.basicapp.objects.Project;
import de.schlund.pfixcore.util.basicapp.objects.ServletObject;

/**
 * The main settings for a new Project will be set here
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class CreateProjectSettings {

    private static final Logger LOG = Logger
            .getLogger(CreateProjectSettings.class);
    /** A Project defines all informations for building a new application */
    private Project project = null;
    /** Informations given by the user */
    public BufferedReader projectIn = new BufferedReader(new InputStreamReader(
            System.in));
    private ArrayList<ServletObject> servletList = null;
    /** A counter for the servlet objects id */
    private int servletCounter = 0;

    /** Constructor just prepares a new project */
    public CreateProjectSettings() {
        project = new Project();
        servletList = project.getServletList();
    }

    /**
     * A getter for the project.
     * @return a Project object. It consists of all
     * necessary informations.
     */
    public Project getCurrentProject() {
        return project;
    }

    /** init method for this class  */
    public void runGetSettings() {
        LOG.debug("Getting project settings starts now");
        servletCounter = 0;
        System.out.println("\n\n\n");
        System.out
                .println("**************************************************");
        System.out
                .println("*                                                *");
        System.out
                .println("*         Pustefix ProjectGenerator 1.0          *");
        System.out
                .println("*                                                *");
        System.out
                .println("**************************************************");
        System.out.println("\nPlease follow the instructions to create a new "
                + "project.");
        System.out.println("You can abort the process by pressing Ctrl + C.");
        
        try {
            // setting the basic items
            setProjectName();
            setProjectLanguage();
            setProjectComment();
            // setting servlets names
            setServletName();
            project.setServletList(servletList);
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
        }
    }

    /**
     * Sets the project name
     * @throws IOException
     */
    private void setProjectName() throws IOException {
        int counter = 0;
        String input = null;
        boolean goOn = true;
        
        // a loop will be done if the project name is required
        do {
            System.out.println("\nPlease type in the projects name e.g. "
                    + "\"myproject\"");
            input = projectIn.readLine();
            // checking for the right project setter
            if (!StringUtils.checkString(input).equals("")) {
                
                // check whether the project already exists
                if (!StringUtils.checkExistingProject(input)) {
                    project
                            .setProjectName(StringUtils
                                    .giveCorrectString(input));
                } else {
                    checkOverwriteProject(input);
                }
                
                goOn = false;
            } else {
                System.out
                        .println("The projects name is mandatory. Please type in\n"
                                + "a valid String");
                counter += 1;
                // nonsens has been typed in for three times. Check whether
                // the user wants to abort
                if (counter == 3) {
                    checkExit(0);
                    goOn = false;
                }
            }
        
        } while (goOn);
    }

    /**
     * Setting the default language for the new project.
     * English is set by default.
     * @throws IOException
     */
    private void setProjectLanguage() throws IOException {
        String input = null;
        
        System.out.println("\nPlease type in the projects default language "
                + "(it's english if you leave the field blank).");
        input = projectIn.readLine();
        
        if (StringUtils.checkString(input).equals("")) {
            project.setLanguage(AppValues.DEFAULTLNG);
        } else {
            project.setLanguage(input);
        }
    }

    /**
     * Setting a comment for the Project
     */
    private void setProjectComment() throws IOException {
        String input = null;
        
        System.out.println("\nPlease type in a comment for the Project");
        System.out
                .println("It will be \"projectname + comment\" if you leave it blank.");
        input = projectIn.readLine();
        
        if (StringUtils.checkString(input).equals("")) {
            project.setComment(project.getProjectName()
                    + AppValues.PRJCOMMENTSUFF);
            LOG.debug("Defaultcomment has been set");
        } else {
            project.setComment(input);
            LOG.debug("Projectcomment has been set by user: " + input);
        }
    }

    /**
     * Method for setting the defaults servlet name
     * @throws IOException
     */
    private void setServletName() throws IOException {
        String input = null;
        ServletObject myServletObject = new ServletObject(servletCounter);
        boolean goOn = true;
        int counter = 0;
        int myServCounter = servletCounter + 1;
        
        do {
            System.out.println("\nPlease type in a name for the servlet "
                    + myServCounter);
            input = projectIn.readLine();
            // checking for the right project setter
            if (!StringUtils.checkString(input).equals("")) {
                myServletObject.setServletName(StringUtils
                        .giveCorrectString(input));
                servletList.add(myServletObject);
                // increasing the counter
                servletCounter++;
                System.out.println("Servlet " + myServCounter
                        + " has been added!");
                goOn = false;
                storeMoreServlets();
            } else {
                counter += 1;
                // nonsens has been typed in for three times. Check whether
                // the user wants to abort
                if (counter == 3) {
                    checkExit(1);
                    goOn = false;
                }
            }
        } while (goOn);
        
    }

    /**
     * Ask the user if he wants to create some more
     * servlets
     * @throws IOException
     */
    private void storeMoreServlets() throws IOException {
        String input = null;
        boolean goOn = true;
        
        do {
            System.out
                    .println("\nWould you like to create another servlet? [yes] [no]");
            input = projectIn.readLine().toLowerCase();
            
            if (input.equals("yes") || input.equals("y")) {
                setServletName();
                goOn = false;
            } else if (input.equals("no") || input.equals("n")) {
                goOn = false;
            }
            
        } while (goOn);
    }

    /**
     * A method in order to check whether the user wants
     * to abort
     * @param method: An integer for the method to go on
     * @throws IOException
     */
    private void checkExit(int method) throws IOException {
        String exit = null;
        boolean goOn = true;
        
        do {
            System.out.println("\nYou havn't typed in a valid value yet!\n"
                    + "Do you want to abort? [Yes] [No]");
            exit = projectIn.readLine().toLowerCase();
            
            if (exit.equals("yes") || exit.equals("y")) {
                System.exit(0);
            } else if (exit.equals("n") || exit.equals("no")) {
                goOn = false;
                switch (method) {
                    case 0 :
                        setProjectName();
                        break;
                    case 1 :
                        setServletName();
                        break;
                }
            }
            
        } while (goOn);
    }

    /**
     * If the project already exists this method checks whether the user
     * really wants to overwrite it.
     * @param input The new project name typed in by the current user.
     */
    private void checkOverwriteProject(String input) throws IOException {
        LOG.debug("Checkout whether the user wants to overwrite existing project");
        String overwrite = null;
        boolean goOn = true;
        System.out.println("\nThe project already exists!");
        
        do {
            System.out.println("Do you really want to overwrite an existing "
                    + "application? [Yes] [No]");
            overwrite = projectIn.readLine().toLowerCase();
        
            if (overwrite.equals("yes") || overwrite.equals("y")) {
                project.setProjectName(input);
                goOn = false;
                ;
            } else if (overwrite.equals("no") || overwrite.equals("n")) {
                setProjectName();
                goOn = false;
            }
            
        } while (goOn);
    }

    /** 
     * Just a getter for the servlet counter
     * @return the counter for the amount of servlets 
     */
    public int getServletCounter() {
        return servletCounter;
    }
}
