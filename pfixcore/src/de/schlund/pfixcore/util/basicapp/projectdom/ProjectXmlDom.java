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
import de.schlund.pfixcore.util.basicapp.objects.Project;


/**
 * Represents the dom of project.xml.in
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class ProjectXmlDom {
    /** The current dom */
    private Document domDoc    = null;
    /** A String Buffer to get e.g. correctPaths */
    private StringBuffer buffy = new StringBuffer();
    /** The current Project */
    Project project            = null;
    private String projectName = null;
    
   
    /**
     * Constructor initializes the Project Object
     * and the dom for the current document
     * @param project The current project
     * @param domDoc the current Dom given by HandleXmlFiles
     */
    public ProjectXmlDom(Project project, Document domDoc) {
        this.domDoc = domDoc;
        projectName = project.getProjectName();
        prepareProjectXml();
    }
    
    
    /**
     * Preparing the project.xml whicht means, that the content of some 
     * of the doms attributes and tags will be changed
     * @param buffy A StringBuffer for the new values
     * @param domDoc The dom
     * @return The new dom
     */
    private void prepareProjectXml() {
        // change the tag depend
        buffy.append(projectName);
        buffy.append(AppValues.CONFFOLDER);
        buffy.append(AppValues.DEPENDXML);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_DEPEND,
                buffy.toString(), /*true if firstChild ->*/ false);
        buffy.setLength(0);
        
        // change attribute name for the project tag
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.PROJECTTAG_PROJECT,
                AppValues.PROJECTATT_NAME, projectName);
        
        // change the text content of simplepage
        buffy.append(projectName);
        buffy.append(".");
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_SERVERNAME,
                buffy.toString(), /*true if firstChild ->*/ true);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_SERVERALIAS,
                buffy.toString(), /*true if firstChild ->*/ true);
        buffy.setLength(0);
        
        // change the passthrough tag
        buffy.append(projectName);
        buffy.append(AppValues.IMGFOLDER);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_PASSTHROUGH,
                buffy.toString(), true);
        buffy.setLength(0);
        
        // changing the propfile tag
        buffy.append(projectName);
        buffy.append(AppValues.CONFFOLDER);
        buffy.append(AppValues.CONFIGPROP);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_PROPFILE,
                buffy.toString(), /*true if firstChild ->*/ false);
        buffy.setLength(0);
            
        // changing the text nod for docroot
        buffy.append(projectName);
        buffy.append(AppValues.HTDOCSFOLDER);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_DOCROOT,
                buffy.toString(), false);
        buffy.setLength(0);
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
