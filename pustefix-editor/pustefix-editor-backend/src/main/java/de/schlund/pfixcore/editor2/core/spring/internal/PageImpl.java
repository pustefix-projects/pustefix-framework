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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.pustefixframework.editor.common.dom.AbstractPage;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.ThemeList;
import org.pustefixframework.editor.common.dom.Variant;

import de.schlund.pfixcore.editor2.core.spring.PustefixTargetUpdateService;
import de.schlund.pfixcore.editor2.core.spring.TargetFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ThemeFactoryService;
import de.schlund.pfixxml.targets.TargetGenerationException;

/**
 * Implementation of Page using classes from Pustefix Core
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageImpl extends AbstractPage implements MutablePage {
    
    private String name;

    private Variant variant;

    private ProjectImpl project;

    private TargetFactoryService targetfactory;

    private PustefixTargetUpdateService updater;

    private ArrayList<Page> childPages = new ArrayList<Page>();

    private ThemeFactoryService themefactory;

    /**
     * Creates a page using the specified parameters
     * 
     * @param targetfactory
     *            Reference to the TargetFactoryService
     * @param updater
     *            Reference to the PustefixTargetUpdateService
     * @param themefactory
     *            Reference to the ThemesFactoryService
     * @param pageName
     *            Name of the page to create (without variant)
     * @param variant
     *            Variant of the page (or <code>null</code> for default)
     * @param project
     *            Project this page belongs to
     */
    public PageImpl(TargetFactoryService targetfactory,
            PustefixTargetUpdateService updater,
            ThemeFactoryService themefactory, String pageName, Variant variant,
            Project project) {
        this.targetfactory = targetfactory;
        this.name = pageName;
        this.variant = variant;
        
        // Cast without check
        // There is no other implementation of Project
        // and even if there was one, we would have no
        // chance than throwing an exception
        this.project = (ProjectImpl) project;
        this.updater = updater;
        this.themefactory = themefactory;
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
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getPageTarget()
     */
    public Target getPageTarget() {
        return this.targetfactory.getTargetFromPustefixTarget(this
                .getPfixTarget(), this.project);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Page#getThemes()
     */
    public ThemeList getThemes() {
        de.schlund.pfixxml.targets.Target target = this.getPfixTarget();
        if (target == null) {
            return new ThemeList() {

                public List<Theme> getThemes() {
                    return Collections.emptyList();
                }

                public boolean includesTheme(Theme theme) {
                    return false;
                }

                public boolean themeOverridesTheme(Theme t1, Theme t2) {
                    return false;
                }
            };
        }
        return new ThemeListImpl(this.themefactory, target.getThemes());
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
    public Collection<Page> getSubPages() {
        HashSet<Page> pages = new HashSet<Page>(this.childPages);
        return pages;
    }

    public void setSubPages(Collection<Page> pages) {
        this.childPages = new ArrayList<Page>(pages);
    }

    public void registerForUpdate() {
        if (this.getPfixTarget() != null) {
            this.updater.registerTargetForUpdate(this.getPfixTarget());
        }
    }

    public void update() {
        if (this.getPfixTarget() == null) {
            return;
        }
        try {
            this.getPfixTarget().getValue();
        } catch (TargetGenerationException e) {
            // Ignore errors during target generation
        }
    }

    private de.schlund.pfixxml.targets.Target getPfixTarget() {
        return project.getTargetGenerator().getPageTargetTree()
                .getTargetForPageInfo(
                        project.getTargetGenerator().getPageInfoFactory()
                                .getPage(
                                        this.getName(),
                                        (variant == null) ? null : this.variant
                                                .getName()));
    }

}
