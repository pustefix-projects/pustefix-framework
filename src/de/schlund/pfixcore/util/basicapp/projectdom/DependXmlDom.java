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
import org.w3c.dom.Text;

import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.helper.XmlUtils;
import de.schlund.pfixcore.util.basicapp.objects.Project;
import de.schlund.pfixcore.util.basicapp.objects.ServletObject;


/**
 * Represents the dom of depend.xml.in
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
        servletAmount = project.getServletList().size();
        
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
        // change attribute project
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_MAKE,
                 AppValues.DEPENDATT_PROJECT, 
                 projectName, false);
        
        // change attribute language
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_MAKE,
                 AppValues.DEPENDATT_LANG, 
                 projectLang, false);
        
        // change attribute handler in the page tag for the default servlet
        String defServletHandler = ((ServletObject)(project.getServletList().get(0))).getServletName();
        buffy.append(AppValues.XMLCONSTANT);
        buffy.append(defServletHandler);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_PAGE,
                 AppValues.DEPENDATT_HANDLER, buffy.toString(), false);
        
        buffy.setLength(0);
        
        // change attribute stylesheet in the include tag for standardmaster
        buffy.append(projectName);
        buffy.append(AppValues.XSLFOLDER);
        buffy.append(AppValues.SKINXSL);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_INCLUDE,
                 AppValues.DEPENDATT_STYLESHEET, buffy.toString(), false);
        
        buffy.setLength(0);
        
        // change attribute stylesheet in the include tag for standardmetatags
        buffy.append(projectName);
        buffy.append(AppValues.XSLFOLDER);
        buffy.append(AppValues.METATAGSXSL);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_INCLUDE,
                 AppValues.DEPENDATT_STYLESHEET, buffy.toString(), true);
        
        buffy.setLength(0);
        
        // change the attribute xml in the standardpage tag
        buffy.append(projectName);
        buffy.append(AppValues.XMLCONSTANT);
        buffy.append(AppValues.FRAMEXML);
        
        domDoc = XmlUtils.changeAttributes(domDoc, AppValues.DEPENDTAG_STDPAGE,
                 AppValues.DEPENDATT_XML, buffy.toString(), false);
        buffy.setLength(0);
        
        // setting the amount of servlets in depend.xml
        if (servletAmount > 0) {
            domDoc = insertNavigationTags(domDoc, servletAmount);
            domDoc = insertStandardpageTags(domDoc, servletAmount);
        }
    }
    
    
    /**
     * If the user has decided to use more than one
     * servlet, a page for every servlet must be added 
     * to the current dom.
     * It will happen here.
     * @param domDoc The current document
     * @param myAmount The amount of servlets, the user
     * wants to add
     */
    private Document insertNavigationTags(Document domDoc, int myAmount) {
        // the node we need is called navigation
        Node navigationNode = domDoc.getElementsByTagName(
                AppValues.DEPENDTAG_NAVIGATION).item(0);
        
        // an ArrayList with the servlet spec
        ArrayList<ServletObject> tmpList  = project.getServletList();
        Element newElement = null;
        String tmpName     = null;
        String myHandler   = null;
        Text lastIndent    = domDoc.createTextNode("  ");
        
        for (int i = 0; i < myAmount; i++) {
            Text txtIndent     = domDoc.createTextNode("    ");
            Text txtReturn  = domDoc.createTextNode("\n");
            tmpName         = ((ServletObject)tmpList.get(i)).getServletName();
            myHandler       = AppValues.XMLCONSTANT + tmpName;
            newElement      = domDoc.createElement(AppValues.DEPENDTAG_PAGE);
            
            newElement.setAttribute(AppValues.DEPENDATT_NAME, AppValues.DEPENDATT_HOME + (i + 1));
            newElement.setAttribute(AppValues.DEPENDATT_HANDLER, myHandler);
   
            navigationNode.appendChild(newElement);
            navigationNode.appendChild(txtReturn);
            navigationNode.insertBefore(txtIndent, newElement);
        }
        
        navigationNode.appendChild(lastIndent);
        return domDoc;
    }
    
    
    /**
     * Generating the standardpage tag for each site defined 
     * in the navigation tag.
     * @return The changed document
     */
    private Document insertStandardpageTags(Document domDoc, int servletAmount) {
        Element rootNode = domDoc.getDocumentElement();
        Text myReturn    = domDoc.createTextNode("\n");
        Text firstIndent = domDoc.createTextNode("  ");
        
        rootNode.appendChild(firstIndent);
        
        for (int i = 0; i < servletAmount; i++) {
             Text txtIndent     = domDoc.createTextNode("  ");
             Text txtReturn  = domDoc.createTextNode("\n");
             
             Element newStdPage = domDoc.createElement(AppValues.DEPENDTAG_STDPAGE);
             newStdPage.setAttribute(AppValues.DEPENDATT_NAME, AppValues.DEPENDATT_HOME + (i + 1));
             newStdPage.setAttribute(AppValues.DEPENDATT_XML, projectName + AppValues.XMLCONSTANT + AppValues.FRAMEXML);
             
             rootNode.appendChild(newStdPage);
             rootNode.appendChild(txtReturn);
             rootNode.appendChild(txtIndent);
        }
        
        rootNode.appendChild(myReturn);
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

