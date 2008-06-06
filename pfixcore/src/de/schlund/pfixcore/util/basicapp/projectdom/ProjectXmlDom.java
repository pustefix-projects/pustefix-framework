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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.schlund.pfixcore.util.basicapp.helper.AppValues;
import de.schlund.pfixcore.util.basicapp.helper.XmlUtils;
import de.schlund.pfixcore.util.basicapp.objects.Project;
import de.schlund.pfixcore.util.basicapp.objects.ServletObject;


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
        this.domDoc  = domDoc;
        projectName  = project.getProjectName();
        this.project = project;
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
                AppValues.PROJECTATT_NAME, projectName, false);
        
        // change the tag comment
        buffy.append(project.getComment());
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_COMMENT,
                 buffy.toString(), true);
        buffy.setLength(0);
        
        // change the text content of servername and serveralias
        buffy.append(projectName);
        buffy.append(".");
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_SERVERNAME,
                buffy.toString(), /*true if firstChild ->*/ true);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_SERVERALIAS,
                buffy.toString(), /*true if firstChild ->*/ true);
        buffy.setLength(0);
        
        // change the content of the defpath tag
        String defServletHandler = ((ServletObject)(project.getServletList().get(0))).getServletName();
        buffy.append(AppValues.XMLCONSTANT);
        buffy.append(defServletHandler);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_DEFPATH,
                 buffy.toString(),true);
        buffy.setLength(0);
        
        // change the passthrough tag
        buffy.append(projectName);
        buffy.append(AppValues.IMGFOLDER);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_PASSTHROUGH,
                buffy.toString(), true);
        buffy.setLength(0);
            
        // changing the text node for docroot
        buffy.append(projectName);
        buffy.append(AppValues.HTDOCSFOLDER);
        domDoc = XmlUtils.changeTextValues(domDoc, AppValues.PROJECTTAG_DOCUMENTROOT,
                buffy.toString(), false);
        buffy.setLength(0);
        
        // creating the servlets
        for (int i = 0; i < project.getServletList().size(); i++) {
            domDoc = insertServlets(domDoc, i);
        }
    }
    
    /**
     * inserting the servlets created by the user
     * @return the dom with all servlets
     */
    private Document insertServlets(Document domDoc, int currentServlet) {
        // the current servlets name
        String curServletName = ((ServletObject)(project.getServletList().
                get(currentServlet))).getServletName();
        
        // my new servlet node
        Element myNewServlet       = domDoc.createElement(AppValues.PROJECTTAG_SERVLET);
        Element newServletActive   = domDoc.createElement(AppValues.PROJECTTAG_ACTIVE);
        Text servletActiveChild    = domDoc.createTextNode(AppValues.PROJECTVALUE_TRUE);
        Element servletClass       = domDoc.createElement(AppValues.PROJECTTAG_CLASS);
        Text classValue            = domDoc.createTextNode(AppValues.PROJECTVALUE_CLASS);
        Element newServletPropfile = domDoc.createElement(AppValues.PROJECTTAG_PROPFILE);
        // Element newDocRootNode     = domDoc.createElement(AppValues.PROJECTTAG_DOCROOT);
        Element rootNode           = domDoc.getDocumentElement();
        Text afterDocRootNode      = domDoc.createTextNode(projectName + AppValues.CONFFOLDER + 
                curServletName + AppValues.CFGFILESUFF);
        
        // setting the attribute of the new servlet tag
        myNewServlet.setAttribute(AppValues.PROJECTATT_NAME, curServletName);
        myNewServlet.setAttribute(AppValues.PROJECTATT_EDITOR, AppValues.PROJECTVALUE_TRUE);

        //setting the text value of active
        newServletActive.appendChild(servletActiveChild);
        myNewServlet.appendChild(newServletActive);
        Text myActiveReturn = domDoc.createTextNode("\n");
        Text myActiveIndent = domDoc.createTextNode("    ");
        myNewServlet.insertBefore(myActiveReturn, newServletActive);
        myNewServlet.insertBefore(myActiveIndent, newServletActive);
        
        // setting the class tag
        servletClass.appendChild(classValue);
        myNewServlet.appendChild(servletClass);
        Text myClassReturn = domDoc.createTextNode("\n");
        Text myClassIndent = domDoc.createTextNode("    ");
        myNewServlet.insertBefore(myClassReturn, servletClass);
        myNewServlet.insertBefore(myClassIndent, servletClass);
        
        // prepare the propfile tag
        newServletPropfile.appendChild(afterDocRootNode);
        // newServletPropfile.insertBefore(newDocRootNode,afterDocRootNode);
        Text myPropfileReturn = domDoc.createTextNode("\n");
        Text myPropfileIndent = domDoc.createTextNode("    ");
        myNewServlet.appendChild(newServletPropfile);
        myNewServlet.insertBefore(myPropfileReturn, newServletPropfile);
        myNewServlet.insertBefore(myPropfileIndent, newServletPropfile);
        Text lastReturn = domDoc.createTextNode("\n");
        Text lastIndent = domDoc.createTextNode("  ");
        myNewServlet.appendChild(lastReturn);
        myNewServlet.appendChild(lastIndent);
        
        // go to the first servlet (it's the deref servlet
        Node servletNode = domDoc.getElementsByTagName(
                AppValues.PROJECTTAG_SERVLET).item(0);
        Text mainServletReturn = domDoc.createTextNode("\n");
        Text mainServletIndent = domDoc.createTextNode("  ");
        rootNode.insertBefore(myNewServlet, servletNode);
        rootNode.insertBefore(mainServletReturn, servletNode);
        rootNode.insertBefore(mainServletIndent, servletNode);
        
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
