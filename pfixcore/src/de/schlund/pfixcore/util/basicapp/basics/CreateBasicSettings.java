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

public final class CreateBasicSettings {
    
    private static final Logger LOG       = Logger.getLogger(CreateBasicSettings.class);
    /** A Project defines all informations for building a new application */
    private Project project               = null;
    /** Informations given by the user */
    public BufferedReader projectIn       = new BufferedReader(
            new InputStreamReader(System.in));
    private ArrayList servletList         = null;
    /** A counter for the servlet objects id */
    public static int servletCounter      = 0;
    /** A class creating the servlet settings */
    CreateServletSettings servletSettings = new CreateServletSettings();
    
    
    /** Constructor just prepares a new project */
    public CreateBasicSettings() {
        project     = new Project();
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
        System.out.println("**************************************************");
        System.out.println("*                                                *");
        System.out.println("*         Pustefix ProjectGenerator 1.0          *");
        System.out.println("*                                                *");
        System.out.println("**************************************************");
        
        System.out.println("\nPlease follow the instructions to create a new " +
                "project.");
        
        System.out.println("You can abort the process by pressing \"return\" " +
                "for three times.");
        
        try {
            // loop over the basic items
            for (int i = 0; i < AppValues.ITEMS.length; i++) {
                setProjectMains(AppValues.ITEMS[i]);
            }
            // ask for the servlet amount
            setServletAmount();
            
            // create the servlet settings (which means name and handler)
           for (int x = 0; x < project.getServletAmount(); x++) {
               // servletCounter is also the servlets id
               servletCounter = x + 1;
               setServletMains();
           }
           
           project.setServletList(servletList);
                      
        } catch (IOException e) {
            LOG.debug(e.getMessage(), e);
        }
    }
    
    
    /**
     * Sets the project name
     * @throws IOException
     */
    private void setProjectMains(String item) throws IOException {
        int counter  = 0;
        String input = null;
        boolean goOn = true;
        
        // a loop in order to verify the input
        do {
            StringBuffer buffy = new StringBuffer("\nPlease type in the projects ");
            // some further informations for the values to type in
            if (item.equals(AppValues.ITEMS[0])) {
                buffy.append(AppValues.ITEMS[0]);
                buffy.append(" e.g. \"myproject\"");
            } else if (item.equals(AppValues.ITEMS[1])) {
                buffy.append(AppValues.ITEMS[1]);
                buffy.append(" e.g. \"en\" if you want to choose english.\n");
                buffy.append("Available language codes are listed here:\n" +
                        "http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt");
            }
            
            System.out.println(buffy.toString());
            input = projectIn.readLine();
            
            // checking for the right project setter
            if (!StringUtils.checkString(input).equals("")) {
                if (item.equals(AppValues.ITEMS[0])) {
                    // check whether the project already exists
                    if (!StringUtils.checkExistingProject(input)) {
                        project.setProjectName(StringUtils.giveCorrectString(input));
                    } else {
                        checkOverwriteProject(input);
                    }
                } else if (item.equals(AppValues.ITEMS[1])) {
                    project.setLanguage(input);
                }
                goOn = false;
            } else {
                counter += 1;
                // nonsens has been typed in for three times. Check whether
                // the user wants to abort
                if (counter == 3) {
                    checkExit(0, item);
                    goOn = false;
                }
            }
        } while (goOn);
    }
    
    
    /**
     * Sets the servlet settings, by creating a new
     * Servlet. The questions for the new Servlets
     * are asked by the class 
     * @see de.schlund.pfixcore.util.basicapp.basics.CreateServletSettings
     * @throws IOException
     */
    private void setServletMains() throws IOException {
        // each ServletObject represents one servlet
        ServletObject myServletObject = new ServletObject(servletCounter);
        String tmpString              = null;
        
        // running through the items (e.g. servletname and path)
        for (int i = 0; i < AppValues.SERVLETITEMS.length; i++) {
            tmpString = servletSettings.getServletItems(AppValues.SERVLETITEMS[i]);
            
            if (tmpString.equals("")) {
                // if checkExit returns to setServletMains the method will 
                // start again and no servlet will be added to the list
                checkExit(2, tmpString);
            } else {
                if (i % 2 == 0) {
                    myServletObject.setServletName(tmpString);
                } else {
                    myServletObject.setServletPath(tmpString);
                }
            }
        }
        // if all settings have been made, the servlet Object will
        // be added
        LOG.debug("Adding a new ServletObject to the project. Id is: " + servletCounter);
        servletList.add(myServletObject);
    }

    
    /**
     * Setting the servlet amount given by the user
     */
    private void setServletAmount() throws IOException{
        int counter  = 0;
        String input = null;
        boolean goOn = true;
        int myAmount = 0;
        
        // a loop in order to verify the input
        do {
            System.out.println("\nPlease type in the servlets amount (it has to be" +
                    " a valid int).");
            System.out.println("If you leave this field blank or if the amount " +
                    "is 0 just \nthe standardservlet will be generated.");
            input    = projectIn.readLine();
            myAmount = StringUtils.checkServletAmount(input);
            // setting the projects servlet amount
            if (myAmount >= 0) {
                project.setServletAmount(myAmount);              
                goOn = false;
            } else {
                counter += 1;
                // nonsens has been typed in for three times. Check whether
                // the user wants to abort
                if (counter == 3) {
                    checkExit(1, input);
                    goOn = false;
                }
            }
        } while (goOn);
    }
    

    /**
     * A method in order to check whether the user wants
     * to abort
     * @param method: An integer for the method to go on
     * @throws IOException
     */
    private void checkExit(int method, String item) throws IOException {
        String exit  = null;
        boolean goOn = true;
        
        do {
            System.out.println("\nYou havn't typed in a valid value yet!\n" + 
                "Do you want to abort? [Yes] [No]");
            exit = projectIn.readLine().toLowerCase();
            
            if (exit.equals("yes") || exit.equals("y")) {
                System.exit(0);
            } else if (exit.equals("n") || exit.equals("no")){
                goOn = false;
                
                switch (method) {
                    case 0:
                        setProjectMains(item);
                        break;
                    case 1:
                        setServletAmount();
                        break;
                    case 2:
                        setServletMains();
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
        boolean goOn     = true;
        
        System.out.println("\nThe project already exists!");
        
        do {
            System.out.println("Do you really want to overwrite an existing " +
                    "application? [Yes] [No]");
            overwrite = projectIn.readLine().toLowerCase();
        
            if (overwrite.equals("yes") || overwrite.equals("y")) {
                project.setProjectName(input);
                goOn = false;;
            } else if (overwrite.equals("no") || overwrite.equals("n")){
                setProjectMains(AppValues.ITEMS[0]);
               goOn = false;
            }           
            
        } while(goOn);
        
       
    }
}
