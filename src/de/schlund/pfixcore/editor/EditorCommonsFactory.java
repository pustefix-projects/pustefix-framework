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

package de.schlund.pfixcore.editor;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.util.PropertiesUtils;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.util.FactoryInit;

/**
 * EditorCommonsFactory.java
 *
 *
 * Created: Fri Nov 23 23:08:28 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version
 *
 *
 */

public class EditorCommonsFactory implements FactoryInit {
    private static DocumentBuilderFactory dbfac    = DocumentBuilderFactory.newInstance();
    private static Category               CAT      = Category.getInstance(EditorCommonsFactory.class.getName());
    private static EditorCommonsFactory   instance = new EditorCommonsFactory();
    private        TreeSet                allincs  = new TreeSet();
    private        HashMap                incfiles = new HashMap();
    private        HashSet                commonfiles;
    private        boolean                inited   = false;
    
    private EditorCommonsFactory() {
        if (!dbfac.isNamespaceAware()) {
            CAT.warn("\n**** Switching DocumentBuilderFactory to be NS-aware ****");
            dbfac.setNamespaceAware(true);
        }
        if (dbfac.isValidating()) {
            CAT.warn("\n**** Switching DocumentBuilderFactory to be non-validating ****");
            dbfac.setValidating(false);
        }
    }
    
    public static EditorCommonsFactory getInstance() {
        return instance;
    }
    
    public synchronized void init(Properties properties) throws Exception {
        commonfiles = new HashSet(PropertiesUtils.selectProperties(properties, "editorcommonsfactory.includefile").values());
    }

    private synchronized void realInit() throws Exception {
        if (!inited) {
            for (Iterator iter = commonfiles.iterator(); iter.hasNext(); ) {
                String filename = (String) iter.next();
                readFile(filename);
            }
            inited = true;
        }
    }


    private void readFile(String filename) throws Exception {
        File comfile = new File(filename); 
        if (comfile.exists() && comfile.canRead() && comfile.isFile()) {
            Object LOCK = FileLockFactory.getInstance().getLockObj(filename);
            synchronized (LOCK) {
                CAT.debug("****** Adding file " + filename + " to common includefiles *******");
                Long modtime = new Long(comfile.lastModified());
                incfiles.put(filename, modtime);
                DocumentBuilder domp = dbfac.newDocumentBuilder();
                Document        doc  = domp.parse(filename);
                NodeList        nl   = XPathAPI.selectNodeList(doc, "/include_parts/part");
                for (int i = 0; i < nl.getLength(); i++) {
                    Element  part     = (Element) nl.item(i);
                    String   partname = part.getAttribute("name");
                    CAT.debug("     * Found part " + partname);
                    NodeList prodlist = XPathAPI.selectNodeList(part, "./product");
                    for (int j = 0; j < prodlist.getLength(); j++) {
                        Element product  = (Element) prodlist.item(j);
                        String  prodname = product.getAttribute("name");
                        if (partname != null && prodname != null) {
                            AuxDependency aux = AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, filename, partname, prodname);
                            allincs.add(aux);
                        }
                    }
                }
            }
        } else {
            CAT.warn(" ****** CAUTION! Can't read dynamic include file " + filename);
        } 
    }
    
    private void update() throws Exception {
        for (Iterator iter = incfiles.keySet().iterator(); iter.hasNext(); ) {
            String filename = (String) iter.next();
            File   file     = new File(filename);
            long   oldmod   = ((Long) incfiles.get(filename)).longValue();
            long   newmod   = file.lastModified();

            if (newmod > oldmod) {
                // remove all incs matching the current path
                HashSet tmp = new HashSet(allincs);
                for (Iterator i = tmp.iterator(); i.hasNext(); ) {
                    AuxDependency aux = (AuxDependency) i.next();
                    if (aux.getPath().equals(filename)) {
                        allincs.remove(aux);
                    }
                }
                // reread the current file
                readFile(filename);
            }
        }
    }


    public boolean isPathAllowed(String path) throws Exception {
        if (!inited) {
            realInit();
        }
        
        if (incfiles.get(path) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public TreeSet getAllCommons() throws Exception {
        if (!inited) {
            realInit();
        }
        
        synchronized(allincs) {
            update();
            return (TreeSet) allincs.clone();
        }
    }

}
