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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.dom.AbstractIncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.exception.EditorSecurityException;
import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixxml.util.XPath;

/**
 * Common implementation of IncludePart
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CommonIncludePartImpl extends AbstractIncludePart {
    private final static String XML_THEME_TAG_NAME = "product";

    private IncludeFile file;

    private HashMap<Theme, IncludePartThemeVariant> cache;

    private ThemeFactoryService themefactory;

    private String name;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    private BackupService backup;

    private Node cacheXML = null;

    private long cacheSerial = -1;

    private long cacheXMLSerial = -1;

    public CommonIncludePartImpl(ThemeFactoryService themefactory,
            FileSystemService filesystem, PathResolverService pathresolver,
            BackupService backup, String partName, IncludeFile file,
            Element el, long serial) {
        this.themefactory = themefactory;
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
        this.backup = backup;
        this.name = partName;
        this.file = file;
        this.cache = new HashMap<Theme, IncludePartThemeVariant>();
        this.cacheXML = el;
        this.cacheXMLSerial = serial;
    }

    /**
     * Override in implementation to create IncludePartThemeVariant
     */
    protected abstract IncludePartThemeVariant createIncludePartThemeVariant(
            Theme theme);

    /**
     * Override in implementation to do security check
     */
    protected abstract void securityCheckDeleteIncludePartThemeVariant(
            IncludePartThemeVariant variant) throws EditorSecurityException;

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getIncludeFile()
     */
    public IncludeFile getIncludeFile() {
        return this.file;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getContentXML()
     */
    public Node getContentXML() {
        synchronized (this.cache) {
            if (this.cacheXML != null
                    && this.cacheXMLSerial == this.getIncludeFile().getSerial()) {
                return this.cacheXML;
            } else {
                long newSerial = this.getIncludeFile().getSerial();
                Document fileXml = this.getIncludeFile().getContentXML();
                if (fileXml == null) {
                    return null;
                }
                Node parentXml = fileXml.getDocumentElement();
                try {
                    this.cacheXML = XPath.selectNode(parentXml, "part[@name='"
                            + this.getName() + "']");
                } catch (TransformerException e) {
                    // This should never happen, so log error, and do like
                    // nothing happened
                    Logger.getLogger(this.getClass()).error("XPath error", e);
                    this.cacheXML = null;
                }
                this.cacheXMLSerial = newSerial;
                return this.cacheXML;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getThemeVariant(de.schlund.pfixcore.editor2.core.dom.Theme)
     */
    public IncludePartThemeVariant getThemeVariant(Theme theme) {
        synchronized (this.cache) {
            if (this.cache.containsKey(theme)) {
                return (IncludePartThemeVariant) this.cache.get(theme);
            } else {
                if (!this.hasThemeVariant(theme)) {
                    return null;
                } else {
                    IncludePartThemeVariant incPartVariant = createIncludePartThemeVariant(theme);
                    this.cache.put(theme, incPartVariant);
                    return incPartVariant;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getThemeVariants()
     */
    public Collection<IncludePartThemeVariant> getThemeVariants() {
        synchronized (this.cache) {
            this.refreshCache();
            return new HashSet<IncludePartThemeVariant>(this.cache.values());
        }
    }

    private void refreshCache() {
        synchronized (this.cache) {
            if (this.getIncludeFile().getSerial() == this.cacheSerial) {
                return;
            }
            long newSerial = this.getIncludeFile().getSerial();
            Node xml = this.getContentXML();

            // Reset cache
            this.cache.clear();
            if (xml == null) {
                return;
            }

            NodeList nlist = xml.getChildNodes();
            for (int i = 0; i < nlist.getLength(); i++) {
                Node n = nlist.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE
                        && n.getNodeName().equals(XML_THEME_TAG_NAME)) {
                    Element el = (Element) n;
                    String themeName = el.getAttribute("name");
                    if (themeName != null) {
                        Theme theme = this.themefactory.getTheme(themeName);
                        this.cache.put(theme, this
                                .createIncludePartThemeVariant(theme));
                    }
                }
            }
            
            this.cacheSerial = newSerial;
        }
    }

    public IncludePartThemeVariant createThemeVariant(Theme theme) {
        synchronized (cache) {
            IncludePartThemeVariant variant = (IncludePartThemeVariant) this.cache
                    .get(theme);
            if (variant != null) {
                return variant;
            } else {
                variant = this.createIncludePartThemeVariant(theme);
                this.cache.put(theme, variant);
                return variant;
            }
        }
    }

    public boolean hasThemeVariant(Theme theme) {
        if (this.getContentXML() == null) {
            return false;
        }
        try {
            return XPath.test(this.getContentXML(), "product[@name='"
                    + theme.getName() + "']");
        } catch (TransformerException e) {
            // Should NEVER happen
            // So if it does, assume variant for theme is not existing
            Logger.getLogger(this.getClass()).error("XPath error!", e);
            return false;
        }
    }

    public void deleteThemeVariant(IncludePartThemeVariant variant)
            throws EditorSecurityException, EditorIOException,
            EditorParsingException {
        if (!variant.getIncludePart().equals(this)) {
            String err = "Attempt to delete IncludePartThemeVariant within invalid context!";
            Logger.getLogger(this.getClass()).error(err);
            throw new RuntimeException(err);
        }

        if (variant.getTheme().getName().equals("default")) {
            throw new EditorSecurityException(
                    "Removal of default branch is never allowed!");
        }

        securityCheckDeleteIncludePartThemeVariant(variant);

        synchronized (this.cache) {
            if (!this.hasThemeVariant(variant.getTheme())) {
                // Variant is not existing any more, so ignore
                return;
            }

            // Remove from cache
            this.cache.remove(variant.getTheme());

            // Remove from file, if existing there
            File xmlFile = new File(this.pathresolver.resolve(this
                    .getIncludeFile().getPath()));
            Object lock = this.filesystem.getLock(xmlFile);
            synchronized (lock) {
                Document doc;
                Element root;
                Element part;
                Element theme;
                if (this.getIncludeFile().getContentXML() == null) {
                    // File does not yet exist, so part is not existing
                    // on filesystem
                    return;
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
                                + this.getName() + "']");
                    } catch (TransformerException e) {
                        // Should never happen as a DOM document is always
                        // well-formed!
                        String err = "XPath error!";
                        Logger.getLogger(this.getClass()).error(err, e);
                        throw new RuntimeException(err, e);
                    }
                    if (part == null) {
                        // This part is not existing in file,
                        // so there is nothing to delete
                        return;
                    }
                    try {
                        theme = (Element) XPath.selectNode(part,
                                "product[@name='"
                                        + variant.getTheme().getName() + "']");
                    } catch (TransformerException e) {
                        // Should never happen as a DOM document is always
                        // well-formed!
                        String err = "XPath error!";
                        Logger.getLogger(this.getClass()).error(err, e);
                        throw new RuntimeException(err, e);
                    }
                    if (theme == null) {
                        // No branch for this theme - no branch
                        // to delete :-)
                        return;
                    } else {
                        // Make a backup
                        this.backup.backupInclude(variant);

                        Node parent = theme.getParentNode();
                        parent.removeChild(theme);
                    }

                    // Save file
                    try {
                        this.filesystem.storeXMLDocumentToFile(xmlFile, doc);
                    } catch (IOException e) {
                        String err = "File " + xmlFile.getAbsolutePath()
                                + " could not written!";
                        Logger.getLogger(this.getClass()).error(err, e);
                        throw new EditorIOException(err, e);
                    }

                    // Fore cache refresh
                    this.getIncludeFile().getContentXML(true);

                    // Register affected pages for regeneration
                    for (Iterator i = variant.getAffectedPages().iterator(); i
                            .hasNext();) {
                        Page page = (Page) i.next();
                        page.registerForUpdate();
                    }
                }
            }
        }
    }
}
