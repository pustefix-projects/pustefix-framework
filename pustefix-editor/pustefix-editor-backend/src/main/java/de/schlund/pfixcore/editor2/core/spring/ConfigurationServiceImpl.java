/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.editor2.core.spring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.exception.EditorInitializationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixxml.targets.TargetGenerator;


public class ConfigurationServiceImpl implements ConfigurationService {
    private HashMap<String, String> map = new HashMap<String, String>();
    private FileSystemService filesystem;
    private TargetGenerator targetGenerator;
    
    public void setFileSystemService(FileSystemService filesystem) {
        this.filesystem = filesystem;
    }
    
    public void init() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException, EditorInitializationException {
        if (targetGenerator == null) {
            throw new IllegalStateException("TargetGenerator has not been set");
        }
        Document doc = filesystem.readCustomizedXMLDocumentFromFile(targetGenerator.getConfigPath(), null);
        Element rootElement = doc.getDocumentElement();
        Element namespacesElement = (Element) rootElement.getElementsByTagName("namespaces").item(0);
        NodeList nlist = namespacesElement.getElementsByTagName("namespace-declaration");
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
    
    @Inject
    public void setTargetGenerator(TargetGenerator targetGenerator) {
        this.targetGenerator = targetGenerator;
    }
}
