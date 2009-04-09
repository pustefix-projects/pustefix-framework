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
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PathResolverService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.TargetFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.FileSystemResource;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFile;
import de.schlund.pfixxml.targets.AuxDependencyImage;
import de.schlund.pfixxml.targets.AuxDependencyManager;
import de.schlund.pfixxml.targets.AuxDependencyTarget;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.TargetDependencyRelation;
import de.schlund.pfixxml.targets.TargetGenerationException;
import de.schlund.pfixxml.targets.TargetImpl;
import de.schlund.pfixxml.targets.VirtualTarget;

/**
 * Implementation of Target using the Target object provided by the Pustefix
 * generator.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class TargetPfixImpl extends AbstractTarget {

    private de.schlund.pfixxml.targets.Target pfixTarget;

    private Project project;

    private TargetFactoryService targetfactory;

    private ProjectFactoryService projectfactory;

    private VariantFactoryService variantfactory;

    private IncludeFactoryService includefactory;

    private ThemeFactoryService themefactory;

    private ImageFactoryService imagefactory;

    private PathResolverService pathresolver;

    private FileSystemService filesystem;

    public TargetPfixImpl(TargetFactoryService targetfactory,
            ProjectFactoryService projectfactory,
            VariantFactoryService variantfactory,
            IncludeFactoryService includefactory,
            ThemeFactoryService themefactory, ImageFactoryService imagefactory,
            PathResolverService pathresolver, FileSystemService filesystem,
            de.schlund.pfixxml.targets.Target pfixTarget, Project project) {
        this.pfixTarget = pfixTarget;
        this.project = project;
        this.targetfactory = targetfactory;
        this.projectfactory = projectfactory;
        this.variantfactory = variantfactory;
        this.includefactory = includefactory;
        this.themefactory = themefactory;
        this.imagefactory = imagefactory;
        this.pathresolver = pathresolver;
        this.filesystem = filesystem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getName()
     */
    public String getName() {
        return this.pfixTarget.getTargetKey();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getType()
     */
    public TargetType getType() {
        if (this.pfixTarget.getType() == de.schlund.pfixxml.targets.TargetType.XML_LEAF
                || this.pfixTarget.getType() == de.schlund.pfixxml.targets.TargetType.XML_VIRTUAL) {
            return TargetType.TARGET_XML;
        } else if (this.pfixTarget.getType() == de.schlund.pfixxml.targets.TargetType.XSL_LEAF
                || this.pfixTarget.getType() == de.schlund.pfixxml.targets.TargetType.XSL_VIRTUAL) {
            return TargetType.TARGET_XSL;
        }
        // FIXME Do not return null but throw an exception
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getContentXML()
     */
    public Document getContentXML() throws EditorIOException,
            EditorParsingException {
        File file;
        if (this.pfixTarget.getType() == de.schlund.pfixxml.targets.TargetType.XML_LEAF
                || this.pfixTarget.getType() == de.schlund.pfixxml.targets.TargetType.XSL_LEAF) {
            file = new File(this.pathresolver.resolve(this.pfixTarget
                    .getTargetKey()));
        } else {
            // Make sure file is existing
            try {
                this.pfixTarget.getValue();
            } catch (TargetGenerationException e) {
                String msg = "Could not generate target " + this.getName()
                        + "!";
                Logger.getLogger(this.getClass()).warn(msg);
            }
            FileResource targetFile = this.pfixTarget.getTargetGenerator().getDisccachedir();
            if (targetFile instanceof DocrootResource) {
                String targetPath = ((DocrootResource) targetFile).getRelativePath();
                file = new File(this.pathresolver.resolve(targetPath + "/" + this.pfixTarget.getTargetKey()));
            } else if (targetFile instanceof FileSystemResource) {
                String targetPath = ((FileSystemResource) targetFile).getPathOnFileSystem();
                file = new File(targetPath);
            } else {
                throw new RuntimeException("TargetGenerator returned non-docroot and non-filesystem-based path: " + targetFile.toURI());
            }
            
        }
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
        if (this.pfixTarget.getXMLSource() == null) {
            return null;
        }
        return this.targetfactory.getTargetFromPustefixTarget(this.pfixTarget
                .getXMLSource(), this.getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getParentXSL()
     */
    public Target getParentXSL() {
        if (this.pfixTarget.getXSLSource() == null) {
            return null;
        }
        return this.targetfactory.getTargetFromPustefixTarget(this.pfixTarget
                .getXSLSource(), this.getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getAuxDependencies()
     */
    public Collection<Target> getAuxDependencies() {
        ArrayList<Target> deps = new ArrayList<Target>();
        if (this.pfixTarget instanceof TargetImpl) {
            TargetImpl vtarget = (TargetImpl) this.pfixTarget;
            AuxDependencyManager auxmanager = vtarget.getAuxDependencyManager();
            if (auxmanager == null) {
                return deps;
            }

            for (Iterator<AuxDependency> i = auxmanager.getChildren().iterator(); i.hasNext();) {
                AuxDependency auxdep = i.next();
                if (auxdep.getType() == DependencyType.FILE) {
                    deps.add(this.targetfactory
                            .getLeafTargetFromPustefixAuxDependency((AuxDependencyFile) auxdep));
                } else if (auxdep.getType() == DependencyType.TARGET) {
                    de.schlund.pfixxml.targets.Target ptarget = ((AuxDependencyTarget) auxdep)
                            .getTarget();
                    deps.add(this.targetfactory.getTargetFromPustefixTarget(
                            ptarget, this.project));
                }
            }
        }

        return deps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getIncludeDependencies(boolean)
     */
    public Collection<IncludePartThemeVariant> getIncludeDependencies(
            boolean recursive) throws EditorParsingException {
        ArrayList<IncludePartThemeVariant> deps = new ArrayList<IncludePartThemeVariant>();
        if (this.pfixTarget instanceof VirtualTarget) {
            if (recursive) {
                Set<AuxDependency> alldeps = TargetDependencyRelation.getInstance()
                        .getDependenciesForTarget(this.pfixTarget);
                if (alldeps != null) {
                    for (Iterator<AuxDependency> i = alldeps.iterator(); i.hasNext();) {
                        AuxDependency aux = i.next();
                        if (aux.getType() == DependencyType.TEXT) {
                            IncludePartThemeVariant variant = this.includefactory
                                    .getIncludePartThemeVariant(aux);
                            deps.add(variant);
                        }
                    }
                }

                if (this.getParentXML() != null) {
                    deps.addAll(this.getParentXML()
                            .getIncludeDependencies(true));
                }
                if (this.getParentXSL() != null) {
                    deps.addAll(this.getParentXSL()
                            .getIncludeDependencies(true));
                }

            } else {
                VirtualTarget vtarget = (VirtualTarget) this.pfixTarget;
                AuxDependencyManager auxmanager = vtarget
                        .getAuxDependencyManager();
                if (auxmanager == null) {
                    String msg = "Could not get AuxDependencyManager for target "
                            + this.getName() + "!";
                    Logger.getLogger(this.getClass()).warn(msg);
                    return deps;
                }

                for (Iterator<AuxDependency> i = auxmanager.getChildren().iterator(); i
                        .hasNext();) {
                    AuxDependency auxdep = i.next();
                    if (auxdep.getType() == DependencyType.TEXT) {
                        IncludePartThemeVariant variant = this.includefactory
                                .getIncludePartThemeVariant(auxdep);
                        deps.add(variant);
                    }
                }
            }

            return deps;
        } else {
            String msg = "Page target " + this.getName()
                    + " is no VirtualTarget!";
            Logger.getLogger(this.getClass()).warn(msg);
            return deps;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getImageDependencies(boolean)
     */
    public Collection<Image> getImageDependencies(boolean recursive)
            throws EditorParsingException {
        ArrayList<Image> deps = new ArrayList<Image>();
        if (this.pfixTarget instanceof VirtualTarget) {
            if (recursive) {
                Set<AuxDependency> alldeps = TargetDependencyRelation.getInstance()
                        .getDependenciesForTarget(this.pfixTarget);
                if (alldeps != null) {
                    for (Iterator<AuxDependency> i = alldeps.iterator(); i.hasNext();) {
                        AuxDependency auxdep = i.next();
                        if (auxdep.getType() == DependencyType.IMAGE) {
                            Image img = this.imagefactory.getImage(((AuxDependencyImage) auxdep).getPath().toURI().toString());
                            deps.add(img);
                        }
                    }
                }

                if (this.getParentXML() != null) {
                    deps.addAll(this.getParentXML().getImageDependencies(true));
                }
                if (this.getParentXSL() != null) {
                    deps.addAll(this.getParentXSL().getImageDependencies(true));
                }

            } else {
                VirtualTarget vtarget = (VirtualTarget) this.pfixTarget;
                AuxDependencyManager auxmanager = vtarget
                        .getAuxDependencyManager();
                if (auxmanager == null) {
                    String msg = "Could not get AuxDependencyManager for target "
                            + this.getName() + "!";
                    Logger.getLogger(this.getClass()).warn(msg);
                    return deps;
                }

                for (Iterator<AuxDependency> i = auxmanager.getChildren().iterator(); i
                        .hasNext();) {
                    AuxDependency auxdep = i.next();
                    if (auxdep.getType() == DependencyType.IMAGE) {
                        Image img = this.imagefactory.getImage(((AuxDependencyImage) auxdep).getPath().toURI().toString());
                        deps.add(img);
                    }
                }
            }

            return deps;
        } else {
            String msg = "Page target " + this.getName()
                    + " is no VirtualTarget!";
            Logger.getLogger(this.getClass()).warn(msg);
            return deps;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Target#getAffectedPages()
     */
    public Collection<Page> getAffectedPages() {
        Collection<PageInfo> pageinfos = this.pfixTarget.getPageInfos();
        HashSet<Page> pages = new HashSet<Page>();
        for (Iterator<PageInfo> i2 = pageinfos.iterator(); i2.hasNext();) {
            PageInfo pageinfo = i2.next();
            if (pageinfo == null) {
                continue;
            }
            Project project = projectfactory.getProject();
            Variant variant = null;
            if (pageinfo.getVariant() != null) {
                variant = this.variantfactory.getVariant(pageinfo.getVariant());
            }
            Page page = project.getPage(pageinfo.getName(), variant);
            if (page == null) {
                String msg = "Could not get page "
                        + pageinfo.getName()
                        + ((variant != null) ? " with variant "
                                + variant.getName() : "") + " from project "
                        + project.getName() + "! Omitting page!";
                Logger.getLogger(this.getClass()).warn(msg);
                continue;
            }
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
        return this.project;
    }

    public Map<String, Object> getParameters() {
        Map<String, Object> map = this.pfixTarget.getParams();
        if (map != null) {
            Map<String, Object> rv = new HashMap<String, Object>();
            // Remove non-String values, as they cannot be handeled correctly
            for (Object key : map.keySet()) {
                Object val = map.get(key);
                if (val instanceof String) {
                    rv.put((String) key, (String) val);
                }
            }
            return rv;
        } else {
            return new HashMap<String, Object>();
        }
    }

    public ThemeList getThemeList() {
        if (this.pfixTarget instanceof VirtualTarget) {
            VirtualTarget vtarget = (VirtualTarget) this.pfixTarget;
            ThemeList themes = new ThemeListImpl(this.themefactory, vtarget
                    .getThemes());
            return themes;
        } else {
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

    de.schlund.pfixxml.targets.Target getPfixTarget() {
        // This package level access method is used by
        // IncludePartThemeVariantImpl to retrieve
        // the Pustefix target object when lookup up
        // dependencies
        return this.pfixTarget;
    }

}
