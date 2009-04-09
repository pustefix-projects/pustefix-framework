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

package de.schlund.pfixcore.editor2.core.spring;

import java.util.HashMap;

import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.ThemeList;
import org.pustefixframework.editor.common.dom.Variant;

import de.schlund.pfixcore.editor2.core.spring.internal.MutablePage;
import de.schlund.pfixcore.editor2.core.spring.internal.PageImpl;

/**
 * Implementation of PageFactoryService
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PageFactoryServiceImpl implements PageFactoryService {
    /**
     * Simple class used as a key for the page cache
     * 
     * @author Sebastian Marsching <sebastian.marsching@1und1.de>
     */
    private class PageKey {
        private Project project;

        private String pagename;

        private Variant variant;

        private PageKey(Project project, String pagename, Variant variant) {
            this.project = project;
            this.pagename = pagename;
            this.variant = variant;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PageKey) {
                PageKey pk = (PageKey) obj;
                if (this.variant == null) {
                    return (this.project.equals(pk.project))
                            && (this.pagename.equals(pk.pagename))
                            && (pk.variant == null);
                } else {
                    return (this.project.equals(pk.project))
                            && (this.pagename.equals(pk.pagename))
                            && (this.variant.equals(pk.variant));
                }
            } else {
                return false;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            if (this.variant == null) {
                return ("PAGEKEY: " + this.project.getName() + "/" + this.pagename)
                        .hashCode();
            } else {
                return ("PAGEKEY: " + this.project.getName() + "/"
                        + this.pagename + "::" + this.variant.getName())
                        .hashCode();
            }
        }
    }

    private TargetFactoryService targetfactory;

    private HashMap<PageKey, Page> cache;

    private PustefixTargetUpdateService pustefixtargetupdate;

    private ThemeFactoryService themefactory;

    public void setTargetFactoryService(TargetFactoryService targetfactory) {
        this.targetfactory = targetfactory;
    }

    public void setPustefixTargetUpdateService(
            PustefixTargetUpdateService pustefixtargetupdate) {
        this.pustefixtargetupdate = pustefixtargetupdate;
    }
    
    public void setThemeFactoryService(ThemeFactoryService themefactory) {
        this.themefactory = themefactory;
    }

    public PageFactoryServiceImpl() {
        this.cache = new HashMap<PageKey, Page>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.spring.PageFactoryService#createPage(java.lang.String,
     *      de.schlund.pfixcore.editor2.core.dom.Variant, java.lang.String,
     *      de.schlund.pfixcore.editor2.core.dom.ThemeList,
     *      de.schlund.pfixcore.editor2.core.dom.Page,
     *      de.schlund.pfixcore.editor2.core.dom.Project,
     *      de.schlund.pfixxml.targets.PageInfo)
     */
    public MutablePage getMutablePage(String pageName, Variant variant,
            String handler, ThemeList themes, Project project) {
        PageKey pk = new PageKey(project, pageName, variant);

        synchronized (this.cache) {
            if (!this.cache.containsKey(pk)) {
                Page page = new PageImpl(this.targetfactory,
                        this.pustefixtargetupdate, this.themefactory, pageName, variant, project);
                this.cache.put(pk, page);
            }
            return (MutablePage) this.cache.get(pk);
        }
    }
}
