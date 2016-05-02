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

package de.schlund.pfixcore.editor2.core.spring.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.editor.common.exception.EditorSecurityException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.editor2.core.spring.BackupService;
import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixxml.util.XPath;

/**
 * Implementation of IncludePart for DynIncludes.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynIncludePartImpl extends CommonIncludePartImpl {
    private ConfigurationService configuration;

    private BackupService backup;

    private FileSystemService filesystem;

    private PathResolverService pathresolver;

    public DynIncludePartImpl(ThemeFactoryService themefactory,
            ConfigurationService configuration, BackupService backup,
            FileSystemService filesystem, PathResolverService pathresolver,
            String partName,
            IncludeFile file, Element el, long serial) {
        super(themefactory, filesystem, pathresolver, backup, partName, file,
                el, serial);
        this.configuration = configuration;
        this.backup = backup;
        this.filesystem = filesystem;
        this.pathresolver = pathresolver;
    }

    protected IncludePartThemeVariant createIncludePartThemeVariant(Theme theme) {
        return new DynIncludePartThemeVariantImpl(configuration, backup,
                filesystem, pathresolver, theme, this);
    }

    public Collection<Theme> getPossibleThemes() {
        // For DynIncludes all thinkable themes are possible,
        // so return an empty list
        return new ArrayList<Theme>();
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
                        Theme theme = this.getThemeFactory().getTheme(themeName);
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
            return XPath.test(this.getContentXML(), XML_THEME_TAG_NAME + "[@name='"
                    + theme.getName() + "']");
        } catch (TransformerException e) {
            // Should NEVER happen
            // So if it does, assume variant for theme is not existing
            Logger.getLogger(this.getClass()).error("XPath error!", e);
            return false;
        }
    }

    @Override
    public void deleteThemeVariant(IncludePartThemeVariant variant) throws EditorSecurityException, EditorIOException, EditorParsingException {
        synchronized (this.cache) {
            super.deleteThemeVariant(variant);
            
            Logger.getLogger("LOGGER_EDITOR").warn(
                    "DYNTXT: remote_access: " + variant.toString() + ": DELETED");
            
            // Remove from cache
            this.cache.remove(variant.getTheme());
        }
    }
    
    
}
