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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.AbstractIncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.IncludePart;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.lucefix.PfixQueueManager;
import de.schlund.pfixcore.lucefix.Tripel;
import de.schlund.pfixxml.util.MD5Utils;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;

/**
 * Common implementation of IncludePartThemeVariant
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonIncludePartThemeVariantImpl extends
        AbstractIncludePartThemeVariant {
    private final static String XML_THEME_TAG_NAME = "theme";
    
    private Theme theme;

    private IncludePart part;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private ConfigurationService configuration;

    private BackupService backup;

    public CommonIncludePartThemeVariantImpl(FileSystemService filesystem,
            PathResolverService pathresolver,
            ConfigurationService configuration, BackupService backup,
            Theme theme, IncludePart part) {
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.configuration = configuration;
        this.backup = backup;
        this.theme = theme;
        this.part = part;
    }

    /**
     * Override to implement ChangeLog entries
     */
    protected abstract void writeChangeLog();

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
            return XPath.selectNode(parentXml, XML_THEME_TAG_NAME + "[@name='"
                    + this.getTheme().getName() + "']");
        } catch (TransformerException e) {
            // Should NEVER happen
            // So if it does, assume there is no node
            Logger.getLogger(this.getClass()).error("XPath error!", e);
            return null;
        }
    }
    
    public void setXML(Node xml) throws EditorIOException, EditorParsingException, EditorSecurityException {
        setXML(xml, true);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant#setXML(org.w3c.dom.Node)
     */
    public void setXML(Node xml, boolean indent) throws EditorIOException,
            EditorParsingException, EditorSecurityException {
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

                part = doc.createElement("part");
                // Keep proper indention level
                Node temp = doc.createTextNode("\n  ");
                root.appendChild(temp);
                root.appendChild(part);
                temp = doc.createTextNode("\n");
                root.appendChild(temp);
                part.setAttribute("name", this.getIncludePart().getName());
                // Keep proper indention level
                temp = doc.createTextNode("\n    ");
                part.appendChild(temp);
                theme = doc.createElement(XML_THEME_TAG_NAME);
                part.appendChild(theme);
                temp = doc.createTextNode("\n  ");
                part.appendChild(temp);
                theme.setAttribute("name", this.getTheme().getName());
            } else {
                try {
                    doc = this.filesystem.readXMLDocumentFromFile(xmlFile);
                } catch (FileNotFoundException e) {
                    String err = "File "
                            + xmlFile.getAbsolutePath()
                            + " could not be found although Pustefix core could obviously read it!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new EditorIOException(err, e);
                } catch (SAXException e) {
                    String err = "Error during parsing file "
                            + xmlFile.getAbsolutePath() + "!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new EditorParsingException(err, e);
                } catch (IOException e) {
                    String err = "File "
                            + xmlFile.getAbsolutePath()
                            + " could not be read although Pustefix core could obviously read it!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new EditorIOException(err, e);
                }
                root = doc.getDocumentElement();
                try {
                    part = (Element) XPath.selectNode(root, "part[@name='"
                            + this.getIncludePart().getName() + "']");
                } catch (TransformerException e) {
                    // Should never happen as a DOM document is always
                    // well-formed!
                    String err = "XPath error!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new RuntimeException(err, e);
                }
                if (part == null) {
                    // Part is not yet existing, so create it
                    part = doc.createElement("part");
                    part.setAttribute("name", this.getIncludePart().getName());
                    // Keep indention
                    Node temp = doc.createTextNode("  ");
                    root.appendChild(temp);
                    root.appendChild(part);
                    temp = doc.createTextNode("\n");
                    root.appendChild(temp);
                    temp = doc.createTextNode("\n  ");
                    part.appendChild(temp);
                }
                try {
                    theme = (Element) XPath.selectNode(part, XML_THEME_TAG_NAME + "[@name='"
                            + this.getTheme().getName() + "']");
                } catch (TransformerException e) {
                    // Should never happen as a DOM document is always
                    // well-formed!
                    String err = "XPath error!";
                    Logger.getLogger(this.getClass()).error(err, e);
                    throw new RuntimeException(err, e);
                }
                if (theme == null) {
                    // No branch for this theme - create it
                    theme = doc.createElement(XML_THEME_TAG_NAME);
                    theme.setAttribute("name", this.getTheme().getName());
                    // Keep indention
                    Node temp = doc.createTextNode("  ");
                    part.appendChild(temp);
                    part.appendChild(theme);
                    temp = doc.createTextNode("\n  ");
                    part.appendChild(temp);
                } else {
                    // Branch is already existing, so make a backup
                    this.backup.backupInclude(this);

                    // Replace node
                    Node oldTheme = theme;
                    Document parentdoc = oldTheme.getOwnerDocument();
                    theme = parentdoc.createElement(XML_THEME_TAG_NAME);
                    theme.setAttribute("name", this.getTheme().getName());
                    Node parent = oldTheme.getParentNode();
                    // Insert at right location to keep indention
                    Node temp = oldTheme.getNextSibling();
                    parent.removeChild(oldTheme);
                    parent.insertBefore(theme, temp);
                }
            }
            
            // Create namespace declarations in root node if needed
            Collection<String> partPrefixes = extractPrefixesFromTree(xml);
            Collection<String> declaredPrefixes = extractPrefixDeclarationsFromFileRoot(xmlFile);
            Collection<String> knownPrefixes = this.configuration.getPrefixToNamespaceMappings().keySet();
            for (String prefix: partPrefixes) {
                if (knownPrefixes.contains(prefix) && !declaredPrefixes.contains(prefix)) {
                    String url = this.configuration.getPrefixToNamespaceMappings().get(prefix);
                    root.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:" + prefix, url);
                }
            }
            
            // Copy over all nodes except attributes to the theme node
            // Keep indention
            Node temp;
            if (indent) {
                temp = doc.createTextNode("\n");
                theme.appendChild(temp);
            }
            NodeList nlist = xml.getChildNodes();
            for (int i = 0; i < nlist.getLength(); i++) {
                Node child = nlist.item(i);
                if (child.getNodeType() != Node.ATTRIBUTE_NODE) {
                    theme.appendChild(doc.importNode(child, true));
                }
            }
            if (indent) {
                temp = doc.createTextNode("\n    ");
                theme.appendChild(temp);
            }

            // Log change
            this.writeChangeLog();

            // Save file
            try {
                this.filesystem.storeXMLDocumentToFile(xmlFile, doc);
            } catch (IOException e) {
                String err = "File " + xmlFile.getAbsolutePath()
                        + " could not written!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            }

            // Force refresh of file cache
            this.getIncludePart().getIncludeFile().getContentXML(true);

            // Register affected pages for regeneration
            Page affectedpage = null;
            for (Iterator<Page> i = this.getAffectedPages().iterator(); i.hasNext();) {
                Page page = i.next();
                page.registerForUpdate();
                affectedpage = page;
            }

            // Regenerate exactly ONE affected page synchronously
            // to make sure changes in the dependency tree are
            // visible at once
            if (affectedpage != null) {
                affectedpage.update();
            } else {
                // This theme branch does not have any affected pages,
                // probably because it was just created - so look for
                // pages using other branches, which will use this branch
                // when being regenerated
                pageloop: for (Iterator<IncludePartThemeVariant> i = this.getIncludePart()
                        .getThemeVariants().iterator(); i.hasNext();) {
                    IncludePartThemeVariant iv = i.next();
                    for (Iterator<Page> i2 = iv.getAffectedPages().iterator(); i2
                            .hasNext();) {
                        Page p = i2.next();
                        if (p.getThemes().themeOverridesTheme(this.getTheme(),
                                iv.getTheme())) {
                            p.update();
                            break pageloop;
                        }
                    }
                }
            }

            PfixQueueManager.getInstance(null).queue(
                    new Tripel(getTheme().getName(),
                            getIncludePart().getName(), getIncludePart()
                                    .getIncludeFile().getPath(),
                            Tripel.Type.EDITORUPDATE));

        }
    }

    private Collection<String> extractPrefixDeclarationsFromFileRoot(File xmlFile) {
        XMLReader xreader;
        try {
            xreader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException("Could not create XMLReader", e);
        }
        
        final Collection<String> prefixes = new HashSet<String>();
        ContentHandler nsPrefixHandler = new DefaultHandler() {
            private boolean foundFirstElement = false;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (foundFirstElement == false) {
                    foundFirstElement = true;
                }
            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                if (!foundFirstElement) {
                    prefixes.add(prefix);
                }
            }
        };
        
        xreader.setContentHandler(nsPrefixHandler);
        try {
            xreader.parse(new InputSource(new FileInputStream(xmlFile)));
        } catch (FileNotFoundException e) {
            // File might not yet be valid - ignore
        } catch (IOException e) {
            // File might not yet be valid - ignore
        } catch (SAXException e) {
            // File might not yet be valid - ignore
        }
        
        return prefixes;
    }

    private Collection<String> extractPrefixesFromTree(Node xml) {
        Collection<String> prefixes = new HashSet<String>();
        String prefix = xml.getPrefix();
        if (prefix != null) {
            prefixes.add(prefix);
        }
        
        NodeList childs = xml.getChildNodes();
        for (int i = 0; i < childs.getLength(); i++) {
            Node node = childs.item(i);
            prefixes.addAll(extractPrefixesFromTree(node));
        }
        
        return prefixes;
    }

    public String getMD5() {
        Node xml = this.getXML();
        if (xml == null) {
            return "0";
        }
        String md5 = MD5Utils.hex_md5(xml);
        return md5;
    }

    public Collection<String> getBackupVersions() {
        return backup.listIncludeVersions(this);
    }

    public boolean restore(String version) throws EditorSecurityException {
        return backup.restoreInclude(this, version);
    }
}
