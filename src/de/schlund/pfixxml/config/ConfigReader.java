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

package de.schlund.pfixxml.config;

import java.io.IOException;
import java.util.Properties;

import org.xml.sax.SAXException;

import de.schlund.pfixcore.exception.PustefixCoreException;
import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;
import de.schlund.pfixxml.config.impl.DirectOutputServletConfigImpl;
import de.schlund.pfixxml.resources.FileResource;

/**
 * Generates configuration objects by reading corresponding configuration
 * files.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ConfigReader {
    
    /**
     * Used to read a configuration for the 
     * {@link de.schlund.pfixxml.ContextXMLServer}.
     * 
     * @param file configuration file to read
     * @param globalProperties globally defined properties the configuration
     * should inherit from
     * @return configuration initialized from file
     * @throws PustefixCoreException if a parsing error occurs
     */
    public static ContextXMLServletConfig readContextXMLServletConfig(FileResource file, Properties globalProperties) throws PustefixCoreException {
        try {
            return ContextXMLServletConfigImpl.readFromFile(file, globalProperties);
        } catch (SAXException e) {
            throw new PustefixCoreException("Error while reading configuration file " + file + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PustefixCoreException("Error while reading configuration file " + file + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Used to read a configuration for the 
     * {@link de.schlund.pfixxml.DirectOutputServlet}.
     * 
     * @param file configuration file to read
     * @param globalProperties globally defined properties the configuration
     * should inherit from
     * @return configuration initialized from file
     * @throws PustefixCoreException if a parsing error occurs
     */
    public static DirectOutputServletConfig readDirectOutputServletConfig(FileResource file, Properties globalProperties) throws PustefixCoreException {
        try {
            return DirectOutputServletConfigImpl.readFromFile(file, globalProperties);
        } catch (SAXException e) {
            throw new PustefixCoreException("Error while reading configuration file " + file + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new PustefixCoreException("Error while reading configuration file " + file + ": " + e.getMessage(), e);
        }
    }
}
