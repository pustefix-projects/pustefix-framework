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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


/**
 * A property file for basicapp called newproject.prop
 * exists. Thic class will store the content static 
 * variables in order to allow a fast access if needed
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class PropResourceManager {
    
    /** A system property for finding the property file */
    private static final String pathToProps;
    /** A variable containing our properties */
    private static final Properties props = new Properties();
    
    /** A static initializer to set the really basic vars */
    static {
        pathToProps = System.getProperty("newprjprops");
        loadPropertyFile();
    }
    
    /**
     * This method loads the property file in order to give their values
     * back if there is a request
     */
    private static void loadPropertyFile() {
        try {
            props.load(new FileInputStream(pathToProps));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Giving back the requested property value
     * @return The Property value
     * @param The key describing the value
     */
    public static String getPropertyValue(String key) {
        String value = props.getProperty(key);
        return value;
    }
}
