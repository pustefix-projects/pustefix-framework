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

package de.schlund.pfixcore.editor2.core.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.pustefixframework.config.Constants;
import org.pustefixframework.editor.common.exception.EditorInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ConfigurationServiceImpl implements ConfigurationService {
    private HashMap<String, String> map = new HashMap<String, String>();
    private PathResolverService pathresolver;
    private FileSystemService filesystem;
    private String projectsFile;
    
    public void setPathResolverService(PathResolverService pathresolver) {
        this.pathresolver = pathresolver;
    }
    
    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }
    
    public void setProjectsFilePath(String path) {
        this.projectsFile = path;
    }
    
    public void init() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, EditorInitializationException {
        Document doc = filesystem.readCustomizedXMLDocumentFromFile(new File(pathresolver.resolve(projectsFile)), Constants.NS_PROJECT);
        Element rootElement = doc.getDocumentElement();
        Element namespacesElement = (Element) rootElement.getElementsByTagNameNS(Constants.NS_PROJECT, "namespaces").item(0);
        NodeList nlist = namespacesElement.getElementsByTagNameNS(Constants.NS_PROJECT, "namespace-declaration");
        for (int i = 0; i < nlist.getLength(); i++) {
            Element node = (Element) nlist.item(i);
            if (!node.hasAttribute("prefix")) {
                String err = "Mandatory attribute prefix is missing for tag namespace-declaration!";
                Logger.getLogger(this.getClass()).error(err);
                throw new EditorInitializationException(err);
            }
            if (!node.hasAttribute("url")) {
                String err = "Mandatory attribute url is missing for tag namespace-declaration!";
                Logger.getLogger(this.getClass()).error(err);
                throw new EditorInitializationException(err);
            }
            String prefix = node.getAttribute("prefix");
            String url = node.getAttribute("url");
            this.map.put(prefix, url);
        }
    }

    public Map<String, String> getPrefixToNamespaceMappings() {
        return new HashMap<String, String>(this.map);
    }

}
