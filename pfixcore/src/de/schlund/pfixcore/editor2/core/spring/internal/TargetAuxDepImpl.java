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



import de.schlund.pfixcore.editor2.core.dom.AbstractTarget;
import de.schlund.pfixcore.editor2.core.dom.AbstractTheme;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.dom.TargetType;
import de.schlund.pfixcore.editor2.core.dom.Theme;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.exception.EditorIOException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.spring.FileSystemService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.TargetDependencyRelation;
import de.schlund.pfixxml.targets.PageInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Implementation of Target using the AuxDependency informationen provided by
 * the Pustefix generator. This implemenation should only be used for leaf
 * targets, which are only used as auxiliary dependencies (no XML or XSL
 * parents).
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetAuxDepImpl extends AbstractTarget {

    private AuxDependency auxdep;

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
            AuxDependency auxdep) {
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
        return this.auxdep.getPath().getRelative();
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
        File file = new File(this.pathresolver.resolve(this.auxdep.getPath()
                .getRelative()));
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
    public Collection getAuxDependencies() {
        // Leaf targets don't have dependencies
        return new ArrayList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getIncludeDependencies(boolean)
     */
    public Collection getIncludeDependencies(boolean recursive) {
        // Leaf targets don't have dependencies
        return new ArrayList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getImageDependencies(boolean)
     */
    public Collection getImageDependencies(boolean recursive) {
        // Leaf targets don't have dependencies
        return new ArrayList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getAffectedPages()
     */
    public Collection getAffectedPages() {
        HashSet pageinfos = new HashSet();
        HashSet pages     = new HashSet();
        Set     afftargets = TargetDependencyRelation.getInstance().getAffectedTargets(auxdep);
        if (afftargets == null) {
            return pages;
        }
        
        for (Iterator i = afftargets.iterator(); i.hasNext();) {
            de.schlund.pfixxml.targets.Target pfixTarget = (de.schlund.pfixxml.targets.Target) i.next();
            pageinfos.addAll(pfixTarget.getPageInfos());
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getProject()
     */
    public Project getProject() {
        String name = this.getName();
        if (name.indexOf("/") == -1) {
            // Do special handling
            return null;
        } else {
            String prjName = name.substring(0, name.indexOf("/"));
            return this.projectfactory.getProjectByName(prjName);
        }
    }

    public Map getTransformationParameters() {
        // Leaf target is not generated - so there are no parameters
        return new HashMap();
    }

    public Map getParameters() {
        return new HashMap();
    }

    public ThemeList getThemeList() {
        return new ThemeList() {

            public Collection getThemes() {
                ArrayList list = new ArrayList();
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
