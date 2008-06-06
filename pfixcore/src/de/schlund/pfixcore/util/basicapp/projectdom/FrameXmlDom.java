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
 * This class will deliver a new Dom of the frame.xml with all necessary
 * changes for a new project
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class FrameXmlDom {
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
    public FrameXmlDom(Project project, Document domDoc) {
        this.domDoc = domDoc;
        projectName = project.getProjectName();
        
        prepareFrameXml();
    }
    
    
    /**
     * HandleXmlFiles has given a dom to this class.
     * This method will change all necessary attributes.
     */
    private void prepareFrameXml() {
        buffy.append(projectName);
        buffy.append(AppValues.PATHTO_PAGES);
        buffy.append(AppValues.CONTENTXML);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.FRAMETAG_INCLUDE,
                AppValues.FRAMEATT_HREF, buffy.toString(), false);
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

