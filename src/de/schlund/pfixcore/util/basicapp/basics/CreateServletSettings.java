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
import org.apache.log4j.Logger;
import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.helper.StringUtils;


/**
 * This class sets the ServletSettings. That means
 * the user is asked to give the necessary informations
 * by command line.
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */


public class CreateServletSettings {
    private static final Logger LOG   = Logger.getLogger(CreateServletSettings.class);
    
    public String getServletItems(String item) throws IOException {
        LOG.debug("CreateServletSettings starts now");
        
        BufferedReader servletIn = new BufferedReader(new InputStreamReader(System.in));
        int counter              = 0;
        String input             = null;
        
        do {
            StringBuffer buffy      = new StringBuffer();
            // desciding which System.out has to appear
            buffy.append("\nPlease type in the ");
            
            if (item.equals(AppValues.SERVLETITEMS[0])) {
                buffy.append(AppValues.SERVLETITEMS[0]);
            } else if (item.equals(AppValues.SERVLETITEMS[1])) {
                buffy.append(AppValues.SERVLETITEMS[1]);
                buffy.append(" e.g. \"/xml/config\"");
            }
            
            buffy.append(" for Servlet " + CreateBasicSettings.servletCounter);
            System.out.println(buffy.toString());
            input = servletIn.readLine();
            
            if (!StringUtils.checkString(input).equals("")) {
                // formatting the String
                input = StringUtils.giveCorrectString(input);
                // checking for correct Path
                if (item.equals(AppValues.SERVLETITEMS[1])) {
                    input = StringUtils.giveCorrectPath(input);
                }
                return input;
            } else {
                counter++;
            }
           
        } while (counter < 3);
        
        return "";
    }
}
