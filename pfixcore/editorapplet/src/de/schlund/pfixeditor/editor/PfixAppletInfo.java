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



package de.schlund.pfixeditor.editor;

import java.net.*;
import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * IncludesFinalizer.java
 *
 *
 * Created: Mon Jul 07 17:30:33 2003
 *
 * @author <a href="mailto:zaich@schlund.de">Volker Zaich</a>
 *
 *
 */



public class PfixAppletInfo {

    URL		         url;      
    InputStream	         input;
    org.w3c.dom.Document doc;
        
    public PfixAppletInfo(String location) {


        try {
            url = new URL(parseLocation(location));            
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            input = connection.getInputStream();            
            buildDom();
             
        } catch (Exception exc ) {
            System.out.println("Connection failed");
            System.out.println(exc.getMessage());            
            
        }                               
    }




    private void buildDom() {

        try {
            DocumentBuilderFactory docBuilderFactory;
            DocumentBuilder docBuilder;
            
            docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            docBuilderFactory.setValidating(false);
            docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(this.input);
            
        } catch (Exception exc) {
            System.out.println("Could not parse DomTree");
            System.out.println(exc.getMessage());
            
        }
        
    }
      
    // Location for Applet Info
    public String parseLocation(String location) {
         String preString = location.substring(0, location.lastIndexOf("/"));
         String afterString = location.substring(location.lastIndexOf(";"), location.length());
         
         String urlLocation = preString + "/AppletInfo" + afterString +  "?__xmlonly=1&__nostore=1";

         return urlLocation;

    }


    public String[] getIncludeElements() {
        org.w3c.dom.NodeList  nl          = this.doc.getElementsByTagName("include");
                    String [] incElements = new String[nl.getLength()];
        
        for (int i = 0; i < nl.getLength(); i++) {
            org.w3c.dom.Element el = (org.w3c.dom.Element) nl.item(i);
            
            String part       = el.getAttribute("part");
            String path       = el.getAttribute("path");            
            String newPath    = path.substring(path.indexOf("example/"), path.length());
            String includeStr = "<pfx:include href=\"" + newPath + "\" part=\"" + part + "\"/>";

            incElements[i] = includeStr;
            
        }

        
        return incElements;
        
    }
    
    
   
    
    public URL getUrl() {
        return this.url;
    }


    public InputStream getInputStream() {
        return this.input;
    }
    
    


    

    
    

    
}
