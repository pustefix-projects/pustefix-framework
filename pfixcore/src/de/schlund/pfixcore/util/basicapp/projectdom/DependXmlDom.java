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

import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.helper.XmlUtils;
import de.schlund.pfixcore.util.basicapp.objects.Project;
import de.schlund.pfixcore.util.basicapp.objects.ServletObject;


/**
 * A class just for initializing a new project
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */
public final class DependXmlDom {
    
    /** The current dom */
    private Document domDoc    = null;
    /** A String Buffer to get e.g. correctPaths */
    private StringBuffer buffy = new StringBuffer();
    /** The current Project */
    Project project            = null;
    private String projectName = null;
    private String projectLang = null;
    private int servletAmount  = 0; 
    
   
    /**
     * Constructor initializes the Project Object
     * and the dom for the current document
     * @param project The current project
     * @param domDoc the current Dom given by HandleXmlFiles
     */
    public DependXmlDom(Project project, Document domDoc) {
        this.domDoc   = domDoc;
        this.project  = project; 
        projectName   = project.getProjectName();
        projectLang   = project.getLanguage();
        servletAmount = project.getServletAmount();
        
        prepareDependXml();
    }
    
    
    /**
     * Preparing the depend.xml.in whicht means, that the content of some 
     * of the doms attributes and tags will be changed
     * @param buffy A StringBuffer for the new values
     * @param domDoc The dom
     * @return The new dom
     */
    private void prepareDependXml() {
        // change attribute make
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_MAKE,
                 AppValues.DEPENDATT_CACHE, 
                 AppValues.DEPENDATT_CACHEPREFIX + projectName);
        
        // change attribute project
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_MAKE,
                 AppValues.DEPENDATT_PROJECT, 
                 projectName);
        
        // change attribute language
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_MAKE,
                 AppValues.DEPENDATT_LANG, 
                 projectLang);
        
        // change attribute stylesheet in the include tag
        buffy.append(projectName);
        buffy.append(AppValues.XSLFOLDER);
        buffy.append(AppValues.SKINXSL);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_INCLUDE,
                 AppValues.DEPENDATT_STYLESHEET, buffy.toString());
        
        buffy.setLength(0);
        
        // change the attribute xml in the standardpage tag
        buffy.append(projectName);
        buffy.append(AppValues.XMLFOLDER);
        buffy.append(AppValues.FRAMEXML);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_STDPAGE,
                 AppValues.DEPENDATT_XML, buffy.toString());
        
        buffy.setLength(0);
        
        // setting the amount of servlets in depend.xml
        if (servletAmount > 0) {
            domDoc = insertServlets(domDoc, servletAmount);
        }
    }
    
    
    /**
     * If the user has decided to use more than one
     * servlet they must be added to the current dom.
     * This will happen in this method
     * @param domDoc The current document
     * @param myAmount The amount of servlets, the user
     * wants to add
     */
    private Document insertServlets(Document domDoc, int myAmount) {
        // the node we need is called navigation
        Node navigationNode = domDoc.getElementsByTagName(
                AppValues.DEPENDTAG_NAVIGATION).item(0);
        
        // an ArrayList with the servlet spec
        ArrayList tmpList   = project.getServletList();
        Element newElement  = null;
        String tmpName      = null;
        String tmpHandler   = null;
        
        for (int i = 0; i < myAmount; i++) {
            tmpName    = ((ServletObject)tmpList.get(i)).getServletName();
            tmpHandler = ((ServletObject)tmpList.get(i)).getServletPath();
            newElement = domDoc.createElement(AppValues.DEPENDTAG_PAGE);
            newElement.setAttribute(AppValues.DEPENDATT_NAME, tmpName);
            newElement.setAttribute(AppValues.DEPENDATT_HANDLER, tmpHandler);
            navigationNode.appendChild(newElement);
        }
        
        return domDoc;
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

