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

package de.schlund.pfixcore.workflow;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.apache.xpath.*;
import org.apache.log4j.*;
import java.util.*;

/**
 *
 *
 */

public class Navigation {
    private Category CAT = Category.getInstance(Navigation.class.getName());
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();

    private NavigationElement pageroot = new NavigationElement("__NONE__", "__NONE__");
    
    public Navigation(String navifilename) throws Exception {
        DocumentBuilder domp     = dbfac.newDocumentBuilder();
        Document        navitree = domp.parse(navifilename);
        NodeList        nl       = XPathAPI.selectNodeList(navitree, "/make/navigation/page");
        recursePagetree(pageroot, nl);
    }

    private void recursePagetree(NavigationElement parent, NodeList nl) throws Exception {
        for (int i = 0; i < nl.getLength(); i++) {
            Element page    = (Element) nl.item(i);
            String  name    = page.getAttribute("name");
            String  handler = page.getAttribute("handler");
            
            NavigationElement elem = new NavigationElement(name, handler);
            parent.addChild(elem);
            NodeList tmp = XPathAPI.selectNodeList(page, "./page");
            if (tmp.getLength() > 0) {
                recursePagetree(elem, tmp);
            }
        }
    }
    
    public NavigationElement[] getNavigationElements() {
        return pageroot.getChildren();
    }

    public class NavigationElement {
        private ArrayList children = new ArrayList();
        private String    name;
        private String    handler;
        
        public NavigationElement (String name, String handler) {
            this.name = name;
            this.handler = handler;
        }
        
        public void addChild(NavigationElement elem) {
            children.add(elem);  
        }
        
        public String getName() {
            return name;
        }
        
        public String getHandler() {
            return handler;
        }
        
        public boolean hasChildren() {
            return !children.isEmpty();
        }
        
        public NavigationElement[] getChildren() {
            return (NavigationElement[]) children.toArray(new NavigationElement[] {});
        }
    } // NavigationElement
}
