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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.AbstractTarget;
import org.pustefixframework.editor.common.dom.AbstractTheme;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.TargetType;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.ThemeList;
import org.pustefixframework.editor.common.dom.Variant;
import org.pustefixframework.editor.common.exception.EditorIOException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.targets.AuxDependencyFile;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.TargetDependencyRelation;

/**
 * Implementation of Target using the AuxDependency informationen provided by
 * the Pustefix generator. This implemenation should only be used for leaf
 * targets, which are only used as auxiliary dependencies (no XML or XSL
 * parents).
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetAuxDepImpl extends AbstractTarget {

    private AuxDependencyFile auxdep;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private PathResolverService pathresolver;

    private FileSystemService filesystem;

    /**
     * Creates a Target using an AuxDependency to retrieve information
     * 
     * @param projectfactory
     *            Reference to ProjectFactoryService
     * @param variantfactory
     *            Reference to VariantFactoryService
     * @param auxdep
     *            AuxDependency object to use
     */
    public TargetAuxDepImpl(ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory,
            PathResolverService pathresolver, FileSystemService filesystem,
            AuxDependencyFile auxdep) {
        this.auxdep = auxdep;
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.pathresolver = pathresolver;
        this.filesystem = filesystem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getName()
     */
    public String getName() {
        return this.auxdep.getPath().toURI().toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getType()
     */
    public TargetType getType() {
        return TargetType.TARGET_AUX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getContentXML()
     */
    public Document getContentXML() throws EditorIOException,
            EditorParsingException {
        File file = new File(this.pathresolver.resolve(auxdep.getPath().toURI().toString()));
        Object lock = this.filesystem.getLock(file);
        synchronized (lock) {
            try {
                return this.filesystem.readXMLDocumentFromFile(file);
            } catch (FileNotFoundException e) {
                String err = "File " + file.getAbsolutePath()
                        + " could not be found!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            } catch (SAXException e) {
                String err = "Error during parsing file "
                        + file.getAbsolutePath() + "!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorParsingException(err, e);
            } catch (IOException e) {
                String err = "File " + file.getAbsolutePath()
                        + " could not be read!";
                Logger.getLogger(this.getClass()).error(err, e);
                throw new EditorIOException(err, e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getParentXML()
     */
    public Target getParentXML() {
        // Leaf targets don't have parents
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getParentXSL()
     */
    public Target getParentXSL() {
        // Leaf targets don't have parents
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getAuxDependencies()
     */
    public Collection<Target> getAuxDependencies() {
        // Leaf targets don't have dependencies
        return new ArrayList<Target>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getIncludeDependencies(boolean)
     */
    public Collection<IncludePartThemeVariant> getIncludeDependencies(
            boolean recursive) {
        // Leaf targets don't have dependencies
        return new ArrayList<IncludePartThemeVariant>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getImageDependencies(boolean)
     */
    public Collection<Image> getImageDependencies(boolean recursive) {
        // Leaf targets don't have dependencies
        return new ArrayList<Image>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getAffectedPages()
     */
    public Collection<Page> getAffectedPages() {
        HashSet<PageInfo> pageinfos = new HashSet<PageInfo>();
        HashSet<Page> pages = new HashSet<Page>();
        Set<de.schlund.pfixxml.targets.Target> afftargets = TargetDependencyRelation.getInstance()
                .getAffectedTargets(auxdep);
        if (afftargets == null) {
            return pages;
        }

        for (Iterator<de.schlund.pfixxml.targets.Target> i = afftargets.iterator(); i.hasNext();) {
            de.schlund.pfixxml.targets.Target pfixTarget = i.next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }
        for (Iterator<PageInfo> i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = i2.next();
            Project project = projectfactory.getProject();
            Variant variant = null;
            if (pageinfo.getVariant() != null) {
                variant = variantfactory.getVariant(pageinfo.getVariant());
            }
            Page page = project.getPage(pageinfo.getName(), variant);
            pages.add(page);
        }
        return pages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getProject()
     */
    public Project getProject() {
        return this.projectfactory.getProject();
    }

    public Map<String, Object> getParameters() {
        return new HashMap<String, Object>();
    }

    public ThemeList getThemeList() {
        return new ThemeList() {

            public List<Theme> getThemes() {
                ArrayList<Theme> list = new ArrayList<Theme>();
                list.add(new AbstractTheme("default") {
                });
                return list;
            }

            public boolean includesTheme(Theme theme) {
                if (theme.getName().equals("default")) {
                    return true;
                } else {
                    return false;
                }
            }

            public boolean themeOverridesTheme(Theme t1, Theme t2) {
                return false;
            }

        };
    }

}
