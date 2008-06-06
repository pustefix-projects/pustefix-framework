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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.AbstractProject;
import de.schlund.pfixcore.editor2.core.dom.Image;
import de.schlund.pfixcore.editor2.core.dom.IncludePartThemeVariant;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.exception.EditorInitializationException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PustefixTargetUpdateService;
import de.schlund.pfixcore.editor2.core.spring.TargetFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixcore.workflow.Navigation;
import de.schlund.pfixcore.workflow.NavigationFactory;
import de.schlund.pfixcore.workflow.Navigation.NavigationElement;
import de.schlund.pfixxml.event.ConfigurationChangeEvent;
import de.schlund.pfixxml.event.ConfigurationChangeListener;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyFactory;
import de.schlund.pfixxml.targets.AuxDependencyImage;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.PageTargetTree;
import de.schlund.pfixxml.targets.TargetDependencyRelation;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;
import de.schlund.pfixxml.targets.Themes;

/**
 * Implementation of Project using a XML file to read project information during
 * construction.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProjectImpl extends AbstractProject {
    private String projectName;

    private String projectComment;

    private VariantFactoryService variantfactory;

    private ThemeFactoryService themefactory;

    private PageFactoryService pagefactory;

    private Collection<Page> toppages;

    private Collection<Page> allpages;

    private Map<String, Map<Variant, Page>> pagemap;

    private TargetGenerator tgen;

    private IncludeFactoryService includefactory;

    private ImageFactoryService imagefactory;

    private TargetFactoryService targetfactory;

    private FileResource dependPath;

    /**
     * Creates a new Project object
     * 
     * @param pathresolver
     *            Reference to PathResolverService
     * @param name
     *            Name of the project to create
     * @param dependFile
     *            "depend.xml" file to read project configuration from
     * @throws EditorInitializationException
     *             if navigation information cannot be loaded
     */
    public ProjectImpl(VariantFactoryService variantfactory,
            ThemeFactoryService themefactory, PageFactoryService pagefactory,
            IncludeFactoryService includefactory,
            ImageFactoryService imagefactory,
            TargetFactoryService targetfactory,
            PustefixTargetUpdateService updater, String name, String comment,
            String dependFile) throws EditorInitializationException {
        this.projectName = name;
        this.projectComment = comment;
        this.variantfactory = variantfactory;
        this.themefactory = themefactory;
        this.pagefactory = pagefactory;
        this.includefactory = includefactory;
        this.imagefactory = imagefactory;
        this.targetfactory = targetfactory;

        this.dependPath = ResourceUtil.getFileResourceFromDocroot(dependFile);

        TargetGenerator gen;
        try {
            gen = TargetGeneratorFactory.getInstance().createGenerator(
                    this.dependPath);
        } catch (Exception e) {
            String err = "Cannot create TargetGenerator for project " + name
                    + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            throw new EditorInitializationException(err, e);
        }
        this.tgen = gen;

        // Register target generator for updates
        updater.registerTargetGeneratorForUpdateLoop(tgen);

        // Register event listener to be informed about changes
        // in TargetGenerator

        this.tgen.addListener(new ConfigurationChangeListener() {

            public void configurationChanged(ConfigurationChangeEvent event) {
                reloadConfig();

            }

        });

        this.pagemap = new HashMap<String, Map<Variant, Page>>();

        // Load configuration
        this.reloadConfig();
    }

    private synchronized void reloadConfig() {
        Navigation navi = this.getNavigation();
        TargetGenerator gen = this.getTargetGenerator();

        // Create hierarchical tree of pages
        PageTargetTree ptree = gen.getPageTargetTree();
        HashSet<Page> pages = new HashSet<Page>();
        NavigationElement[] navElements = navi.getNavigationElements();
        for (int i = 0; i < navElements.length; i++) {
            pages.addAll(this.recurseNavigationElement(navElements[i], null,
                    ptree));
        }
        
        // Create pagename => page map
        HashMap<String, Map<Variant, Page>> pagemap = new HashMap<String, Map<Variant, Page>>();
        for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
            Page page = i.next();
            this.recursePage(page, pagemap);
        }

        // Create collection containing all page objects
        HashSet<Page> allpages = new HashSet<Page>();
        for (Iterator<Map<Variant, Page>> i = pagemap.values().iterator(); i
                .hasNext();) {
            Map<Variant, Page> map = i.next();
            allpages.addAll(map.values());
        }
        
        // Add pages from target definitions which are not present in navigation tree
        for (PageInfo pinfo : gen.getPageTargetTree().getPageInfos()) {
            String pageName = pinfo.getName();
            String pageHandler = "none";
            String variantName = pinfo.getVariant();
            Variant pageVariant;
            if (variantName != null) {
                pageVariant = this.variantfactory.getVariant(variantName);
            } else {
                pageVariant = null;
            }
            ThemeList pageThemes = new ThemeListImpl(this.themefactory,
                    ptree.getTargetForPageInfo(pinfo).getThemes());
            if (!pagemap.containsKey(pageName) || !pagemap.get(pageName).containsKey(pageVariant)) {
                // Create new page only if there has not been a page
                // with the same name and same variant before
                MutablePage page;
                page = this.pagefactory.getMutablePage(pageName,
                        pageVariant, pageHandler, pageThemes, this);
                page.setHandlerPath(pageHandler);
                pages.add(page);
                this.recursePage(page, pagemap);
                allpages.add(page);
            }
        }

        this.toppages = pages;
        this.pagemap = pagemap;
        this.allpages = allpages;

    }

    /**
     * Recurses over a page element adding the page and its subpages to the map
     * 
     * @param page
     *            Page object to recurse over
     * @param allpages
     *            Map to add page objects to
     */
    private void recursePage(Page page, HashMap<String, Map<Variant, Page>> allpages) {
        Map<Variant, Page> pagetable;
        if (allpages.containsKey(page.getName())) {
            pagetable = allpages.get(page.getName());
        } else {
            pagetable = new HashMap<Variant, Page>();
            allpages.put(page.getName(), pagetable);
        }
        pagetable.put(page.getVariant(), page);
        for (Iterator<Page> i = page.getSubPages().iterator(); i.hasNext();) {
            Page page2 = i.next();
            this.recursePage(page2, allpages);
        }
    }

    /**
     * Recurses over NavigationElement object to retrieve the navigation
     * structure of the page tree
     * 
     * @param nav
     *            Element to recurse over
     * @param parent
     *            Parent page object - needed to create new page objects
     * @param ptree
     *            Tree of navigation objects used by the Pustefix generator
     * @return Collection containing page objects for all variants of the page
     *         specified by the NavigationElement
     */
    private Collection<Page> recurseNavigationElement(NavigationElement nav,
            Page parent, PageTargetTree ptree) {
        HashSet<MutablePage> pages = new HashSet<MutablePage>();
        Page defaultPage = null;

        String pageName = nav.getName();
        String pageHandler = nav.getHandler();
        Collection<PageInfo> pinfos = ptree.getPageInfoForPageName(pageName);
        if (pinfos == null) {
            String msg = "Could not load PageInfo from PageTree for page "
                    + pageName + "! No target for page defined?";
            Logger.getLogger(this.getClass()).warn(msg);
        } else {
            for (Iterator<PageInfo> iter = pinfos.iterator(); iter.hasNext();) {
                PageInfo pinfo = iter.next();
                String variantName = pinfo.getVariant();
                Variant pageVariant;
                if (variantName != null) {
                    pageVariant = this.variantfactory.getVariant(variantName);
                } else {
                    pageVariant = null;
                }
                ThemeList pageThemes = new ThemeListImpl(this.themefactory,
                        ptree.getTargetForPageInfo(pinfo).getThemes());
                MutablePage page = (MutablePage) this.getPage(pageName,
                        pageVariant);
                if (page == null) {
                    // Create new page only if there has not been a page
                    // with the same name and same variant before
                    page = this.pagefactory.getMutablePage(pageName,
                            pageVariant, pageHandler, pageThemes, this);
                }
                page.setHandlerPath(pageHandler);
                pages.add(page);
                if (pageVariant == null) {
                    defaultPage = page;
                }
            }
        }
        
        // make sure a default page exists, even if there is no target
        // otherwise, subpages cannot not be handled correctly
        if (defaultPage == null) {
            MutablePage page;
            page = this.pagefactory.getMutablePage(pageName, null, pageHandler, new ThemeListImpl(this.themefactory, new Themes("default")), this);
            page.setHandlerPath(pageHandler);
            pages.add(page);
            defaultPage = page;
        }
        
        HashSet<Page> subpages = new HashSet<Page>();

        if (nav.hasChildren()) {
            NavigationElement[] elements = nav.getChildren();
            for (int i = 0; i < elements.length; i++) {
                Collection<Page> subpageElements = this
                        .recurseNavigationElement(elements[i], defaultPage,
                                ptree);
                subpages.addAll(subpageElements);
            }
        }

        for (Iterator<MutablePage> iter = pages.iterator(); iter.hasNext();) {
            MutablePage page = iter.next();
            if (page == null) {
                String err = "Page returned by iteration is null!";
                Logger.getLogger(this.getClass()).error(err);
            }
            page.setSubPages(subpages);
        }

        return new LinkedHashSet<Page>(pages);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getName()
     */
    public String getName() {
        return this.projectName;
    }

    public String getComment() {
        return this.projectComment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getAllPages()
     */
    public Collection<Page> getAllPages() {
        return new HashSet<Page>(this.allpages);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getTopPages()
     */
    public Collection<Page> getTopPages() {
        return new HashSet<Page>(this.toppages);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getPage(java.lang.String,
     *      de.schlund.pfixcore.editor2.core.dom.Variant)
     */
    public Page getPage(String pagename, Variant variant) {
        if (!this.pagemap.containsKey(pagename)) {
            return null;
        } else {
            return this.pagemap.get(pagename).get(variant);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getPageByName(java.lang.String)
     */
    public Collection<Page> getPageByName(String name) {
        if (!this.pagemap.containsKey(name)) {
            return new HashSet<Page>();
        } else {
            return new HashSet<Page>(this.pagemap.get(name).values());
        }
    }

    public Target getTarget(String name) {
        de.schlund.pfixxml.targets.Target pfixTarget = this.tgen
                .getTarget(name);
        if (pfixTarget != null) {
            return this.targetfactory.getTargetFromPustefixTarget(pfixTarget,
                    this);
        }
        return null;
    }

    public TargetGenerator getTargetGenerator() {
        return this.tgen;
    }

    private Navigation getNavigation() {
        try {
            return NavigationFactory.getInstance().getNavigation(this.dependPath,tgen.getXsltVersion());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not get navigation object for prokec \""
                            + this.getName() + "\"!");
        }
    }

    public Collection<IncludePartThemeVariant> getAllIncludeParts() {
        HashSet<IncludePartThemeVariant> includes = new HashSet<IncludePartThemeVariant>();
        TreeSet<AuxDependency> deps = TargetDependencyRelation.getInstance()
                .getProjectDependenciesForType(this.tgen, DependencyType.TEXT);
        if (deps == null) {
            return includes;
        }

        for (Iterator<AuxDependency> i = deps.iterator(); i.hasNext();) {
            AuxDependency auxdep = i.next();
            try {
                includes.add(this.includefactory
                        .getIncludePartThemeVariant(auxdep));
            } catch (EditorParsingException e) {
                // Ignore exception and go on
            }
        }
        return includes;
    }

    public Collection<Image> getAllImages() {
        HashSet<Image> images = new HashSet<Image>();
        TreeSet<AuxDependency> deps = TargetDependencyRelation.getInstance()
                .getProjectDependenciesForType(this.tgen, DependencyType.IMAGE);
        if (deps == null) {
            return images;
        }
        for (Iterator<AuxDependency> i = deps.iterator(); i.hasNext();) {
            AuxDependencyImage auxdep = (AuxDependencyImage) i.next();
            images.add(this.imagefactory.getImage(auxdep.getPath().getRelativePath()));
        }
        return images;
    }

    public IncludePartThemeVariant findIncludePartThemeVariant(String file,
            String part, String theme) {
        AuxDependency auxdep = AuxDependencyFactory
                .getInstance()
                .getAuxDependencyInclude(
                        ResourceUtil.getFileResourceFromDocroot(file), part, theme);

        TreeSet<AuxDependency> deps = TargetDependencyRelation.getInstance()
                .getProjectDependencies(tgen);
        if (deps == null) {
            return null;
        }

        if (deps.contains(auxdep)) {
            try {
                return this.includefactory.getIncludePartThemeVariant(auxdep);
            } catch (EditorParsingException e) {
                String msg = "Failed to get include part " + part + ":" + theme
                        + "@" + file + "!";
                Logger.getLogger(this.getClass()).warn(msg, e);
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean hasIncludePart(String file, String part, String theme) {
        AuxDependency aux = AuxDependencyFactory
                .getInstance()
                .getAuxDependencyInclude(
                        ResourceUtil.getFileResourceFromDocroot(file), part, theme);
        TreeSet<TargetGenerator> generators = TargetDependencyRelation.getInstance()
                .getAffectedTargetGenerators(aux);
        if (generators == null) {
            return false;
        }

        return generators.contains(this.tgen);
    }

}