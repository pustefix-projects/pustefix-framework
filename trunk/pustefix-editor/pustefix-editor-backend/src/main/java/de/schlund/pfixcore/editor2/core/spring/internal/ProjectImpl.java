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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.pustefixframework.editor.common.dom.AbstractProject;
import org.pustefixframework.editor.common.dom.Image;
import org.pustefixframework.editor.common.dom.IncludeFile;
import org.pustefixframework.editor.common.dom.IncludePartThemeVariant;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.ThemeList;
import org.pustefixframework.editor.common.dom.Variant;
import org.pustefixframework.editor.common.exception.EditorInitializationException;
import org.pustefixframework.editor.common.exception.EditorParsingException;
import org.pustefixframework.util.xml.DOMUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor2.core.spring.ConfigurationService;
import de.schlund.pfixcore.editor2.core.spring.DynIncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ImageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.IncludeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.PustefixTargetUpdateService;
import de.schlund.pfixcore.editor2.core.spring.TargetFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.event.ConfigurationChangeEvent;
import de.schlund.pfixxml.event.ConfigurationChangeListener;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.targets.AuxDependency;
import de.schlund.pfixxml.targets.AuxDependencyImage;
import de.schlund.pfixxml.targets.DependencyType;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.PageTargetTree;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.Themes;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * Implementation of Project using a XML file to read project information during
 * construction.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProjectImpl extends AbstractProject {
    
    private String projectName;

    private String projectComment;
    
    private boolean includePartsEditableByDefault;

    private VariantFactoryService variantfactory;

    private ThemeFactoryService themefactory;

    private PageFactoryService pagefactory;

    private Collection<Page> toppages;

    private Collection<Page> allpages;

    private Map<String, Map<Variant, Page>> pagemap;

    private TargetGenerator tgen;

    private IncludeFactoryService includefactory;
    
    private DynIncludeFactoryService dynincludefactory;

    private ImageFactoryService imagefactory;

    private TargetFactoryService targetfactory;
    
    private ConfigurationService configuration;

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
            DynIncludeFactoryService dynincludefactory,
            ImageFactoryService imagefactory,
            TargetFactoryService targetfactory,
            ConfigurationService configuration,
            PustefixTargetUpdateService updater, String name, String comment,
            boolean includePartsEditableByDefault,
            TargetGenerator tgen) throws EditorInitializationException {
        this.projectName = name;
        this.projectComment = comment;
        this.includePartsEditableByDefault = includePartsEditableByDefault;
        this.variantfactory = variantfactory;
        this.themefactory = themefactory;
        this.pagefactory = pagefactory;
        this.includefactory = includefactory;
        this.dynincludefactory = dynincludefactory;
        this.imagefactory = imagefactory;
        this.targetfactory = targetfactory;
        this.configuration = configuration;

        this.tgen = tgen;

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
        SiteMap navi = this.getNavigation();
        TargetGenerator gen = this.getTargetGenerator();

        // Create hierarchical tree of pages
        PageTargetTree ptree = gen.getPageTargetTree();
        HashSet<Page> pages = new HashSet<Page>();
        List<Element>  navElements = DOMUtils.getChildElementsByTagName(navi.getSiteMapXMLElement(XsltVersion.XSLT1, null), "page");
        for (Element navElement : navElements) {
            pages.addAll(this.recurseNavigationElement(navElement, null,
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
                        pageVariant, pageThemes, this);
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
    private Collection<Page> recurseNavigationElement(Element nav,
            Page parent, PageTargetTree ptree) {
        HashSet<MutablePage> pages = new HashSet<MutablePage>();
        Page defaultPage = null;

        String pageName = nav.getAttribute("name");
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
                            pageVariant, pageThemes, this);
                }
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
            page = this.pagefactory.getMutablePage(pageName, null, new ThemeListImpl(this.themefactory, new Themes("default")), this);
            pages.add(page);
            defaultPage = page;
        }
        
        HashSet<Page> subpages = new HashSet<Page>();

        List<Element> childPages = DomUtils.getChildElementsByTagName(nav, "page");
        if (!childPages.isEmpty()) {
            for (Element childPage: childPages) {
                Collection<Page> subpageElements = this
                        .recurseNavigationElement(childPage, defaultPage,
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

    private SiteMap getNavigation() {
        try {
            return tgen.getSiteMap();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not get navigation object for prokec \""
                            + this.getName() + "\"!");
        }
    }

    public Collection<IncludePartThemeVariant> getAllIncludeParts() {
        HashSet<IncludePartThemeVariant> includes = new HashSet<IncludePartThemeVariant>();
        TreeSet<AuxDependency> deps = tgen.getTargetDependencyRelation()
                .getProjectDependenciesForType(DependencyType.TEXT);
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
        TreeSet<AuxDependency> deps = tgen.getTargetDependencyRelation()
                .getProjectDependenciesForType(DependencyType.IMAGE);
        if (deps == null) {
            return images;
        }
        for (Iterator<AuxDependency> i = deps.iterator(); i.hasNext();) {
            AuxDependencyImage auxdep = (AuxDependencyImage) i.next();
            images.add(this.imagefactory.getImage(auxdep.getPath().toURI().toString()));
        }
        return images;
    }

    public IncludePartThemeVariant findIncludePartThemeVariant(String file,
            String part, String theme) {
        AuxDependency auxdep = tgen.getAuxDependencyFactory()
                .getAuxDependencyInclude(
                        ResourceUtil.getResource(file), part, theme);

        TreeSet<AuxDependency> deps = tgen.getTargetDependencyRelation()
                .getProjectDependencies();
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
        AuxDependency aux = tgen.getAuxDependencyFactory()
                .getAuxDependencyInclude(
                        ResourceUtil.getResource(file), part, theme);
        return aux != null;
    }

    public IncludeFile getDynIncludeFile(String path) {
        return dynincludefactory.getIncludeFile(path);
    }

    public Collection<IncludeFile> getDynIncludeFiles() {
        return dynincludefactory.getDynIncludeFiles();
    }

    public Map<String, String> getPrefixToNamespaceMappings() {
        return configuration.getPrefixToNamespaceMappings();
    }

    public boolean isIncludePartsEditableByDefault() {
        return includePartsEditableByDefault;
    }

}
