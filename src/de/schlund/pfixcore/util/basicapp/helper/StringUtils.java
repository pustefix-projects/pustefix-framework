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

package de.schlund.pfixcore.util.basicapp.helper;

import java.io.File;
import org.apache.log4j.Logger;

/**
 * Just some utilities for e.g. formatting Strings and 
 * Integers
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */

public class StringUtils {
    
    private static final Logger LOG = Logger.getLogger(StringUtils.class);
    
    /** 
     * Checking projectname and language for a valid String. 
     * @return an empty String or the input given to the method
     * if it is valid 
     */
    public static String checkString(String input) {
        String emptyString = ""; 
        LOG.debug("Checking the String: " + input);
        
        if (input == null || input.length() == 0) {
            LOG.debug("String is not ok");
            System.out.println("Please type in a String.");
            return emptyString;
        } else {
            LOG.debug("String is ok");
        }
        
        return input;
     }
    
    
    /**
     * Some basic operations to avoid problems with
     * not allowed characters
     * @param The String given by the user
     * @return The input as a usable String
     */
    public static String giveCorrectString(String input) {
        input.toLowerCase();
        
        input = input.replaceAll("ä", "a");
        input = input.replaceAll("ä", "a");
        input = input.replaceAll("ü", "u");
        input = input.replaceAll("ö", "o");
        input = input.replaceAll(" ","_");
        input = input.trim();
        
        return input;
    }
    
    /**
     * Look if the path has got a leadint slash.
     * If not it will be added
     * @param path The path typed in by the user
     */
    public static String giveCorrectPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;  
        }
        
        return path;
    }
    
    /**
     * Check whether the project already exists
     * @return true if the project exists
     */
    public static boolean checkExistingProject(String input) {
        String[] projectNames = new File(AppValues.BASICPATH).list();
        boolean exists        = false;
        String tmpName        = null;
        
        for (int i = 0; i < projectNames.length; i++) {
            tmpName = projectNames[i];
            
            if (tmpName.equals(input)) {
                exists = true;
            }
        }
        
        return exists;
    }
    
    /**
     * Checking for a valid int in order to 
     * get the amount of servlets
     * @param the servlet amount
     */
    public static int checkServletAmount (String amount) 
    throws NumberFormatException {
        LOG.debug("Checking the String by regular expression...");
        int tmpInt = 0;
        amount.trim();
        
        if (amount.equals("")) {
            return tmpInt;
        } else if (amount.matches("[0-9]++")) {
            tmpInt = Integer.parseInt(amount);
            LOG.debug("String is a valid int");
            return tmpInt;
        } else {
            LOG.debug("RegEx fails. No int!");
            return -1;
        }
    }
}
