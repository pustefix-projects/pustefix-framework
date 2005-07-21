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

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

/**
 * Implementation of IncludePartThemeVariant using a DOM tree
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludePartThemeVariantImpl implements IncludePartThemeVariant {

    private Theme theme;

    private IncludePart part;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private IncludeFactoryService includefactory;

    private ThemeFactoryService themefactory;

    private ImageFactoryService imagefactory;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;
    
    private ConfigurationService configuration;
    
    private SecurityManagerService securitymanager;

    private AuxDependency auxdep;

    public IncludePartThemeVariantImpl(ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory,
            IncludeFactoryService includefactory,
            ThemeFactoryService themefactory, ImageFactoryService imagefactory,
            FileSystemService filesystem, PathResolverService pathresolver,
            ConfigurationService configuration,
            SecurityManagerService securitymanager,
            Theme theme, IncludePart part) {
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.includefactory = includefactory;
        this.themefactory = themefactory;
        this.imagefactory = imagefactory;
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.configuration = configuration;
        this.securitymanager = securitymanager;
        this.theme = theme;
        this.part = part;

        this.auxdep = AuxDependencyFactory.getInstance().getAuxDependency(
                DependencyType.TEXT,
                PathFactory.getInstance().createPath(
                        this.getIncludePart().getIncludeFile().getPath()),
                this.getIncludePart().getName(), this.getTheme().getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getTheme()
     */
    public Theme getTheme() {
        return this.theme;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getIncludePart()
     */
    public IncludePart getIncludePart() {
        return this.part;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getXML()
     */
    public Node getXML() {
        Node parentXml = this.getIncludePart().getContentXML();
        if (parentXml == null) {
            return null;
        }
        try {
            return XPath.selectNode(parentXml, "product[@name='"
                    + this.getTheme().getName() + "']");
        } catch (TransformerException e) {
            // Should NEVER happen
            // So if it does, assume there is no node
            Logger.getLogger(this.getClass()).error("XPath error!", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#setXML(org.w3c.dom.Node)
     */
    public void setXML(Node xml) throws EditorIOException, EditorParsingException, EditorSecurityException {
        File xmlFile = new File(this.pathresolver.resolve(this.getIncludePart()
                .getIncludeFile().getPath()));
        Object lock = this.filesystem.getLock(xmlFile);
        synchronized (lock) {
            Document doc;
            Element root;
            Element part;
            Element theme;
            if (this.getIncludePart().getIncludeFile().getContentXML() == null) {
                // File does not yet exist and has to be created
                // Honor default namespace-prefixes during creation!
                
                // Do security check
                this.securitymanager.checkCreateIncludePartThemeVariant(this.getIncludePart(), this.getTheme());
                if (!xmlFile.exists()) {
                    try {
                        xmlFile.createNewFile();
                    } catch (IOException e) {
                        String err = "Could not create file "
                                + xmlFile.getAbsolutePath() + "!";
                        Logger.getLogger(this.getClass()).error(err, e);
                        throw new EditorIOException(err, e);
                    }
                }
                doc = Xml.createDocument();
                root = doc.createElement("include_parts");
                doc.appendChild(root);
                
                // Add predefined namespace mappings
                Map nsMappings = this.configuration.getPrefixToNamespaceMappings();
                for (Iterator i = nsMappings.keySet().iterator(); i.hasNext();) {
                    String prefix = (String) i.next();
                    String url = (String) nsMappings.get(prefix);
                    root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, url);
                }
                
                part = doc.createElement("part");
                root.appendChild(part);
                part.setAttribute("name", this.getIncludePart().getName());
                theme = doc.createElement("product");
                part.appendChild(theme);
                theme.setAttribute("name", this.getTheme().getName());
            } else {
                // Do security check
                this.securitymanager.checkEditIncludePartThemeVariant(this);
                
                try {
                    doc = this.filesystem.readXMLDocumentFromFile(xmlFile);
                } catch (FileNotFoundException e) {
                    String err = "File " + xmlFile.getAbsolutePath() + " could not be found although Pustefix core could obviously read it!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new EditorIOException(err, e);
                } catch (SAXException e) {
                    String err = "Error during parsing file " + xmlFile.getAbsolutePath() + "!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new EditorParsingException(err, e);
                } catch (IOException e) {
                    String err = "File " + xmlFile.getAbsolutePath() + " could not be read although Pustefix core could obviously read it!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new EditorIOException(err, e);
                } catch (ParserConfigurationException e) {
                    String err = "Error during initialization of XML parser!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new RuntimeException(err, e);                  
                }
                root = doc.getDocumentElement();
                try {
                    part = (Element) XPath.selectNode(root, "part[@name='" + this.getIncludePart().getName() + "']");
                } catch (TransformerException e) {
                    // Should never happen as a DOM document is always well-formed!
                    String err = "XPath error!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new RuntimeException(err, e);
                }
                if (part == null) {
                    // Part is not yet existing, so create it
                    part = doc.createElement("part");
                    part.setAttribute("name", this.getIncludePart().getName());
                    root.appendChild(part);
                }
                try {
                    theme = (Element) XPath.selectNode(part, "product[@name='" + this.getTheme().getName() + "']");
                } catch (TransformerException e) {
                    // Should never happen as a DOM document is always well-formed!
                    String err = "XPath error!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new RuntimeException(err, e);
                }            
                if (theme == null) {
                    // No branch for this theme - create it
                    theme = doc.createElement("product");
                    theme.setAttribute("name", this.getTheme().getName());
                    part.appendChild(theme);
                }
            }
            
            // Copy over all nodes except attributes to the theme node
            NodeList nlist = xml.getChildNodes();
            for (int i = 0; i < nlist.getLength(); i++) {
                Node child = nlist.item(i);
                if (child.getNodeType() != Node.ATTRIBUTE_NODE) {
                    theme.appendChild(doc.importNode(child, true));
                }
            }
            
            // Save file
            try {
                this.filesystem.storeXMLDocumentToFile(xmlFile, doc);
            } catch (IOException e) {
                String err = "File " + xmlFile.getAbsolutePath() + " could not written!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            }
            
            // Tell IncludeFactory to refresh
            this.includefactory.refreshIncludeFile(this.getIncludePart().getIncludeFile().getPath());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getIncludeDependencies(boolean)
     */
    public Collection getIncludeDependencies(boolean recursive)
            throws EditorParsingException {
        HashSet includes = new HashSet();
        Collection childs = this.auxdep.getChildrenForAllThemes();
        for (Iterator i = childs.iterator(); i.hasNext();) {
            AuxDependency child = (AuxDependency) i.next();
            if (child.getType() == DependencyType.TEXT) {
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludeFile(child.getPath().getRelative())
                        .createPart(child.getPart()).createThemeVariant(
                                themefactory.getTheme(child.getProduct()));
                includes.add(variant);
                if (recursive) {
                    includes.addAll(variant.getIncludeDependencies(true));
                }
            }
        }
        return includes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getImageDependencies(boolean)
     */
    public Collection getImageDependencies(boolean recursive)
            throws EditorParsingException {
        HashSet images = new HashSet();
        Collection childs = this.auxdep.getChildrenForAllThemes();
        for (Iterator i = childs.iterator(); i.hasNext();) {
            AuxDependency child = (AuxDependency) i.next();
            if (child.getType() == DependencyType.IMAGE) {
                Image image = this.imagefactory.getImage(child.getPath()
                        .getRelative());
                images.add(image);
            } else if ((child.getType() == DependencyType.TEXT) && recursive) {
                IncludePartThemeVariant variant = this.includefactory
                        .getIncludeFile(child.getPath().getRelative())
                        .createPart(child.getPart()).createThemeVariant(
                                themefactory.getTheme(child.getProduct()));
                images.addAll(variant.getImageDependencies(true));
            }
        }
        return images;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#getAffectedPages()
     */
    public Collection getAffectedPages() {
        HashSet pageinfos = new HashSet();
        for (Iterator i = this.auxdep.getAffectedTargets().iterator(); i
                .hasNext();) {
            de.schlund.pfixxml.targets.Target pfixTarget = (de.schlund.pfixxml.targets.Target) i
                    .next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }

        HashSet pages = new HashSet();
        for (Iterator i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = (PageInfo) i2.next();
            String projectName = pageinfo.getTargetGenerator().getName();
            Project project = projectfactory.getProjectByName(projectName);
            Variant variant = null;
            if (pageinfo.getVariant() != null) {
                variant = variantfactory.getVariant(pageinfo.getVariant());
            }
            Page page = project.getPage(pageinfo.getName(), variant);
            pages.add(page);
        }

        return pages;
    }
}
