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
 * Represents the dom of the pages to be displayed
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class PageXmlDom {
    /** The current dom */
    private Document domDoc    = null;
      
    /**
     * Constructor initializes the Page Object
     * and the dom for the current document
     * @param project The current project
     * @param domDoc the current Dom given by HandleXmlFiles
     */
    public PageXmlDom(Project project, Document domDoc) {
        this.domDoc = domDoc;
        preparePage();
    }
    
    
  
    private void preparePage() {
        String pageName = AppValues.PAGEDEFAULT;
        // setting the current path
        domDoc = XmlUtils.changeTextValues(domDoc, "theme", "Page: " + pageName, false);
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
