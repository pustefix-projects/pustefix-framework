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

package de.schlund.pfixcore.util.basicapp.projectdom;

import org.w3c.dom.Document;

import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.helper.XmlUtils;


/**
 * Represents the dom of config.prop.in
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class ConfigXmlDom {
    /** The current dom */
    private Document domDoc    = null;
      
    /**
     * Constructor initializes the Project Object
     * and the dom for the current document
     * @param project The current project
     * @param domDoc the current Dom given by HandleXmlFiles
     */
    public ConfigXmlDom(Document domDoc) {
        this.domDoc = domDoc;
        prepareConfigProp();
    }
    
    
    /**
     * Preparing the project.xml whicht means, that the content of some 
     * of the doms attributes and tags will be changed
     * @param buffy A StringBuffer for the new values
     * @param domDoc The dom
     * @return The new dom
     */
    private void prepareConfigProp() {
        String curPageName = AppValues.PAGEDEFAULT;
        
        // change attribute name -> tag is flowstep
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.CONFIGTAG_FLOWSTEP,
                 AppValues.CONFIGATT_NAME, curPageName, false);
        
        // change attribute name -> tag is pagerequest
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.CONFIGTAG_PAGEREQUEST,
                 AppValues.CONFIGATT_NAME, curPageName, false);
    }
    
    
    /**
     * Gives the current dom back to 
     * HandleXmlFiles
     * @return Document The current dom
     */
    public Document getDom() {
        return domDoc;
    }   
}
