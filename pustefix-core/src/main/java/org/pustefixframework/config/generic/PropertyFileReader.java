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

package org.pustefixframework.config.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;

import com.marsching.flexiparse.parser.ClasspathConfiguredParser;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.resources.FileResource;

/**
 * Helper class providing methods to read in Properties from customizable
 * Pustefix XML property files 
 * 
 * @author mleidig
 *
 */
public class PropertyFileReader {
    
    public static void read(File file, Properties properties) throws ParserException, FileNotFoundException {
        read(new FileInputStream(file), properties);
    }
    
    public static void read(FileResource resource, Properties properties) throws ParserException, IOException {
        read(resource.getInputStream(), properties);
    }
    
    public static void read(InputStream in, Properties properties) throws ParserException {
        
        PropertiesBasedCustomizationInfo customizationInfo = new PropertiesBasedCustomizationInfo(BuildTimeProperties.getProperties());
        ClasspathConfiguredParser parser = new ClasspathConfiguredParser("META-INF/org/pustefixframework/config/generic/properties-config.xml");
        
        parser.parse(in, customizationInfo, properties);
        
        //TODO: alternative error check and support of empty property files 
        if(properties.size()==0) throw new ParserException("No properties found. Check if your property file "+
                "is valid and uses the correct XML namespace.");
    }
    
}
