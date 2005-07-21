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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import de.schlund.pfixcore.editor2.core.dom.AbstractIncludePart;
import de.schlund.pfixcore.editor2.core.dom.IncludeFile;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.util.XPath;

/**
 * Implementation of IncludePart using a DOM tree
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class IncludePartImpl extends AbstractIncludePart {
    private IncludeFile file;

    private Hashtable cache;

    private ThemeFactoryService themefactory;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private String name;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    public IncludePartImpl(ThemeFactoryService themefactory,
            ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory,
            IncludeFactoryService includefactory,
            ImageFactoryService imagefactory, String partName,
            IncludeFile file) {
        this.themefactory = themefactory;
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.includefactory = includefactory;
        this.imagefactory = imagefactory;
        this.name = partName;
        this.file = file;
        this.cache = new Hashtable();
    }

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
        if (this.getIncludeFile().getContentXML() == null) {
            return null;
        }
        Node parentXml = this.getIncludeFile().getContentXML()
                .getDocumentElement();
        try {
            return XPath.selectNode(parentXml, "part[@name='"
                    + this.getName() + "']");
        } catch (TransformerException e) {
            // This should never happen, so log error, and do like
            // nothing happened
            Logger.getLogger(this.getClass()).error("XPath error", e);
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getThemeVariant(de.schlund.pfixcore.editor2.core.dom.Theme)
     */
    public IncludePartThemeVariant getThemeVariant(Theme theme) {
        if (this.cache.containsKey(theme)) {
            return (IncludePartThemeVariant) this.cache.get(theme);
        }
        synchronized (this.cache) {
            if (!this.cache.containsKey(theme)) {
                if (this.getContentXML() == null) {
                    return null;
                }
                try {
                    if (!XPath.test(this.getContentXML(),
                            "product[@name='" + theme.getName() + "']")) {
                        return null;
                    }
                } catch (TransformerException e) {
                    // Should NEVER happen
                    // So if it does, assume variant for theme is not existing
                    Logger.getLogger(this.getClass()).error("XPath error!", e);
                    return null;
                }
                IncludePartThemeVariant incPartVariant = this.includefactory.getIncludePartThemeVariant(theme, this);
                this.cache.put(theme, incPartVariant);
            }
        }
        return (IncludePartThemeVariant) this.cache.get(theme);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getThemeVariants()
     */
    public Collection getThemeVariants() {
        List nlist;
        if (this.getContentXML() == null) {
            return new ArrayList();
        }
        try {
            nlist = XPath.select(this.getContentXML(),
                    "product/@name");
        } catch (TransformerException e) {
            // This should never happen, so log error and do like
            // nothing happened
            Logger.getLogger(this.getClass()).error("XPath error", e);
            return new ArrayList();
        }

        HashSet variants = new HashSet();
        for (Iterator i = nlist.iterator(); i.hasNext();) {
            String themeName = ((Node) i.next()).getNodeValue();
            Theme theme = this.themefactory.getTheme(themeName);
            variants.add(this.getThemeVariant(theme));
        }

        return variants;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.IncludePart#getPossibleThemes()
     */
    public Collection getPossibleThemes() {
        HashSet pages = new HashSet();
        for (Iterator i = this.getThemeVariants().iterator(); i.hasNext();) {
            IncludePartThemeVariant partVar = (IncludePartThemeVariant) i
                    .next();
            pages.addAll(partVar.getAffectedPages());
        }
        HashSet themes = new HashSet();
        for (Iterator i = pages.iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            themes.addAll(page.getThemes().getThemes());
        }
        return themes;
        // AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT,
        // PathFactory.getInstance().createPath(this.getIncludeFile().getPath()),
        // this.getName(), theme);
    }

    public IncludePartThemeVariant createThemeVariant(Theme theme) {
        IncludePartThemeVariant variant = this.getThemeVariant(theme);
        if (variant != null) {
            return variant;
        }
        synchronized (cache) {
            if (!this.cache.containsKey(theme)) {
                variant = this.includefactory.getIncludePartThemeVariant(theme, this);
                this.cache.put(theme, variant);
            }
        }
        return (IncludePartThemeVariant) this.cache.get(theme);
    }

    public boolean hasThemeVariant(Theme theme) {
        if (this.getContentXML() == null) {
            return false;
        }
        try {
            return XPath.test(this.getContentXML(),
                    "product[@name='" + theme.getName() + "']");
        } catch (TransformerException e) {
            // Should NEVER happen
            // So if it does, assume variant for theme is not existing
            Logger.getLogger(this.getClass()).error("XPath error!", e);
            return false;
        }
    }
}
