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
import java.util.Map;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.core.dom.AbstractProject;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.exception.EditorInitializationException;
import de.schlund.pfixcore.editor2.core.exception.EditorParsingException;
import de.schlund.pfixcore.editor2.core.spring.PageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;
import de.schlund.pfixcore.workflow.Navigation;
import de.schlund.pfixcore.workflow.NavigationFactory;
import de.schlund.pfixcore.workflow.Navigation.NavigationElement;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.targets.PageInfo;
import de.schlund.pfixxml.targets.PageTargetTree;
import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.targets.TargetGeneratorFactory;

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

    private Collection toppages;

    private Collection allpages;

    private HashMap pagemap;

    private HashMap targetmap;

    private TargetGenerator tgen;

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
            String name, String comment, String dependFile)
            throws EditorInitializationException {
        this.projectName = name;
        this.projectComment = comment;
        this.variantfactory = variantfactory;
        this.themefactory = themefactory;
        this.pagefactory = pagefactory;

        Navigation navi;
        try {
            navi = NavigationFactory.getInstance().getNavigation(dependFile);
        } catch (Exception e) {
            String err = "Cannot not load navigation for project " + name + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            throw new EditorInitializationException(err, e);
        }

        TargetGenerator gen;
        try {
            gen = TargetGeneratorFactory.getInstance().createGenerator(
                    PathFactory.getInstance().createPath(dependFile));
        } catch (Exception e) {
            String err = "Cannot create TargetGenerator for project " + name
                    + "!";
            Logger.getLogger(this.getClass()).error(err, e);
            throw new EditorInitializationException(err, e);
        }
        this.tgen = gen;

        // Create hierarchical tree of pages
        PageTargetTree ptree = gen.getPageTargetTree();
        HashSet pages = new HashSet();
        NavigationElement[] navElements = navi.getNavigationElements();
        for (int i = 0; i < navElements.length; i++) {
            pages.addAll(this.recurseNavigationElement(navElements[i], null,
                    ptree));
        }
        this.toppages = pages;

        // Create pagename => page map
        HashMap pagemap = new HashMap();
        for (Iterator i = this.toppages.iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            this.recursePage(page, pagemap);
        }
        this.pagemap = pagemap;

        // Create collection containing all page objects
        HashSet allpages = new HashSet();
        for (Iterator i = pagemap.values().iterator(); i.hasNext();) {
            HashMap map = (HashMap) i.next();
            allpages.addAll(map.values());
        }
        this.allpages = allpages;

        // Create target map
        HashMap targets = new HashMap();
        for (Iterator i = allpages.iterator(); i.hasNext();) {
            Page p = (Page) i.next();
            // Recurse over targets
            this.recurseTarget(p.getPageTarget(), targets);
        }
        this.targetmap = targets;
    }

    private void recurseTarget(Target target, Map alltargets) {
        alltargets.put(target.getName(), target);
        for (Iterator i = target.getAuxDependencies(false).iterator(); i
                .hasNext();) {
            Target t = (Target) i.next();
            alltargets.put(t.getName(), t);
        }
        if (!target.isLeafTarget()) {
            this.recurseTarget(target.getParentXML(), alltargets);
            this.recurseTarget(target.getParentXSL(), alltargets);
        }
    }

    /**
     * Recurses over a page element adding the page and its subpages to the map
     * 
     * @param page
     *            Page object to recurse over
     * @param allpages
     *            Map to add page objects to
     */
    private void recursePage(Page page, HashMap allpages) {
        HashMap pagetable;
        if (allpages.containsKey(page.getName())) {
            pagetable = (HashMap) allpages.get(page.getName());
        } else {
            pagetable = new HashMap();
            allpages.put(page.getName(), pagetable);
        }
        pagetable.put(page.getVariant(), page);
        for (Iterator i = page.getSubPages().iterator(); i.hasNext();) {
            Page page2 = (Page) i.next();
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
    private Collection recurseNavigationElement(NavigationElement nav,
            Page parent, PageTargetTree ptree) {
        HashSet pages = new HashSet();
        Page defaultPage = null;

        String pageName = nav.getName();
        String pageHandler = nav.getHandler();
        Collection pinfos = ptree.getPageInfoForPageName(pageName);
        if (pinfos == null) {
            String msg = "Could not load PageInfo from PageTree for page "
                    + pageName + "! No target for page defined?";
            Logger.getLogger(this.getClass()).warn(msg);
        } else {
            for (Iterator iter = pinfos.iterator(); iter.hasNext();) {
                PageInfo pinfo = (PageInfo) iter.next();
                String variantName = pinfo.getVariant();
                Variant pageVariant;
                if (variantName != null) {
                    pageVariant = this.variantfactory.getVariant(variantName);
                } else {
                    pageVariant = null;
                }
                ThemeList pageThemes = new ThemeListImpl(this.themefactory,
                        ptree.getTargetForPageInfo(pinfo).getThemes());
                MutablePage page = this.pagefactory
                        .getMutablePage(pageName, pageVariant, pageHandler,
                                pageThemes, null, this, pinfo);
                if (page == null) {
                    String err = "Page returned by pagefactory is null!";
                    Logger.getLogger(this.getClass()).error(err);
                }
                pages.add(page);
                if (pageVariant == null) {
                    defaultPage = page;
                }
            }
        }
        HashSet subpages = new HashSet();

        if (nav.hasChildren()) {
            NavigationElement[] elements = nav.getChildren();
            for (int i = 0; i < elements.length; i++) {
                Collection subpageElements = this.recurseNavigationElement(
                        elements[i], defaultPage, ptree);
                subpages.addAll(subpageElements);
            }
        }

        for (Iterator iter = pages.iterator(); iter.hasNext();) {
            PageImpl page = (PageImpl) iter.next();
            if (page == null) {
                String err = "Page returned by iteration is null!";
                Logger.getLogger(this.getClass()).error(err);
            }
            page.addSubPages(subpages);
        }

        return pages;
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
    public Collection getAllPages() {
        return new HashSet(this.allpages);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getTopPages()
     */
    public Collection getTopPages() {
        return new HashSet(this.toppages);
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
            return (Page) ((HashMap) this.pagemap.get(pagename)).get(variant);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Project#getPageByName(java.lang.String)
     */
    public Collection getPageByName(String name) {
        if (!this.pagemap.containsKey(name)) {
            return new HashSet();
        } else {
            return new HashSet(((HashMap) this.pagemap.get(name)).values());
        }
    }

    public Target getTarget(String name) {
        return (Target) this.targetmap.get(name);
    }

    public TargetGenerator getTargetGenerator() {
        return this.tgen;
    }

    public Collection getAllIncludeParts() {
        HashSet includes = new HashSet();
        for (Iterator i = this.getAllPages().iterator(); i.hasNext();) {
            Page page = (Page) i.next();
            try {
                includes.addAll(page.getPageTarget().getIncludeDependencies(true));
            } catch (EditorParsingException e) {
                // Log error and continue
                String err = "Could not get include dependencies for page " + page.getFullName() + "!";
                Logger.getLogger(this.getClass()).error(err, e);
            }
        }
        return includes;
    }

}