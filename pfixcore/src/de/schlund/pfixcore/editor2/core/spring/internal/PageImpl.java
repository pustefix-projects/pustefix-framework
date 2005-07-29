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

import de.schlund.pfixcore.editor2.core.dom.AbstractPage;
import de.schlund.pfixcore.editor2.core.dom.Page;
import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.dom.Target;
import de.schlund.pfixcore.editor2.core.dom.ThemeList;
import de.schlund.pfixcore.editor2.core.dom.Variant;
import de.schlund.pfixcore.editor2.core.spring.PustefixTargetUpdateService;
import de.schlund.pfixcore.editor2.core.spring.TargetFactoryService;
import de.schlund.pfixxml.targets.PageInfo;

/**
 * Implementation of Page using classes from PFIXCORE
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageImpl extends AbstractPage implements MutablePage {
    private String name;

    private Variant variant;

    private String handler;

    private ThemeList themes;

    private Project project;

    private Page parentPage;

    private Collection childPages;

    private PageInfo pinfo;

    private TargetFactoryService targetfactory;

    private PustefixTargetUpdateService updater;

    /**
     * Creates a page using the specified parameters
     * 
     * @param targetfactory
     *            Reference to the TargetFactoryService
     * @param updater
     *            Reference to the PustefixTargetUpdateService
     * @param pageName
     *            Name of the page to create (without variant)
     * @param variant
     *            Variant of the page (or <code>null</code> for default)
     * @param handler
     *            String describing the handler being used to serve this page
     * @param themes
     *            List of the themes being used by this page
     * @param parent
     *            Parent of this page in navigation (or <code>null</code> if
     *            this is a top-level page)
     * @param childs
     *            Childs of this page in navigation
     * @param project
     *            Project this page belongs to
     * @param tgen
     *            TargetGenerator used for this page's target
     * @param pinfo
     *            Used to identify this page within the Pustefix generator
     */
    public PageImpl(TargetFactoryService targetfactory,
            PustefixTargetUpdateService updater, String pageName,
            Variant variant, String handler, ThemeList themes, Page parent,
            Project project, PageInfo pinfo) {
        this.targetfactory = targetfactory;
        this.name = pageName;
        this.variant = variant;
        this.handler = handler;
        this.themes = themes;
        this.project = project;
        this.parentPage = parent;
        this.childPages = new ArrayList();
        this.pinfo = pinfo;
        this.updater = updater;
        // Register page for updating
        updater.registerTargetForInitialUpdate(this.pinfo.getTargetGenerator()
                .getPageTargetTree().getTargetForPageInfo(this.pinfo));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getName()
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getVariant()
     */
    public Variant getVariant() {
        return this.variant;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getHandlerPath()
     */
    public String getHandlerPath() {
        return this.handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getPageTarget()
     */
    public Target getPageTarget() {
        return this.targetfactory.getTargetFromPustefixTarget(this.pinfo
                .getTargetGenerator().getPageTargetTree().getTargetForPageInfo(
                        this.pinfo), this.getProject());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getThemes()
     */
    public ThemeList getThemes() {
        return this.themes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getProject()
     */
    public Project getProject() {
        return this.project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getSubPages()
     */
    public Collection getSubPages() {
        HashSet pages = new HashSet(this.childPages);
        return pages;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getParentPage()
     */
    public Page getParentPage() {
        return this.parentPage;
    }

    /**
     * Adds the specified Page objects to the list of sub-pages of this Page
     * 
     * @param page
     *            Collection containing Page objects to add as sub-page
     */
    public void addSubPages(Collection pages) {
        this.childPages.addAll(pages);
    }

    public void registerForUpdate() {
        this.updater.registerTargetForUpdate(this.pinfo.getTargetGenerator()
                .getPageTargetTree().getTargetForPageInfo(this.pinfo));
    }

}
