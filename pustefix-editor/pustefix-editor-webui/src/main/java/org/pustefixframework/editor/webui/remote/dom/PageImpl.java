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

package org.pustefixframework.editor.webui.remote.dom;

import java.util.Collection;
import java.util.LinkedList;

import org.pustefixframework.editor.common.dom.AbstractPage;
import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.common.dom.Target;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.ThemeList;
import org.pustefixframework.editor.common.dom.Variant;
import org.pustefixframework.editor.common.remote.transferobjects.PageTO;
import org.pustefixframework.editor.webui.remote.dom.util.RemoteServiceUtil;



public class PageImpl extends AbstractPage {
    
    private RemoteServiceUtil remoteServiceUtil;
    private String name;
    private Variant variant;
    private volatile PageTO pageTO;

    public PageImpl(RemoteServiceUtil remoteServiceUtil, String name) {
        this.remoteServiceUtil = remoteServiceUtil;
        int colonsPos = name.indexOf("::");
        if (colonsPos == -1) {
            this.name = name;
            this.variant = null;
        } else {
            this.name = name.substring(0, colonsPos);
            this.variant = new VariantImpl(name.substring(colonsPos + 2));
        }
    }
    
    public PageImpl(RemoteServiceUtil remoteServiceUtil, PageTO pageTO) {
        this.remoteServiceUtil = remoteServiceUtil;
        this.name = pageTO.name;
        this.pageTO = pageTO;
    }
    
    public String getHandlerPath() {
        initPageTO();
        return pageTO.handlerPath;
    }
    
    public String getName() {
        return name;
    }
    
    public Target getPageTarget() {
        initPageTO();
        if (pageTO.target == null) {
            return null;
        }
        return new TargetImpl(remoteServiceUtil, pageTO.target);
    }
    
    public Project getProject() {
        return new ProjectImpl(remoteServiceUtil);
    }
    
    public Collection<Page> getSubPages() {
        initPageTO();
        LinkedList<Page> pages = new LinkedList<Page>();
        for (PageTO pageTO : this.pageTO.subPages) {
            pages.add(new PageImpl(remoteServiceUtil, pageTO));
        }
        return pages;
    }
    
    public ThemeList getThemes() {
        initPageTO();
        LinkedList<Theme> themes = new LinkedList<Theme>();
        for (String themeName : pageTO.themes) {
            themes.add(new ThemeImpl(themeName));
        }
        return new ThemeListImpl(themes);
    }
    
    public Variant getVariant() {
        return variant;
    }
    
    public void registerForUpdate() {
        remoteServiceUtil.getRemotePageService().registerForUpdate(getName());
    }
    
    public void update() {
        remoteServiceUtil.getRemotePageService().update(getName());
    }
    
    private void initPageTO() {
        if (pageTO == null) {
            pageTO = remoteServiceUtil.getRemotePageService().getPage(name);
        }
    }

    @Override
    public int compareTo(Page page) {
        if (page instanceof PageImpl) {
            PageImpl p = (PageImpl) page;
            if (this.remoteServiceUtil.equals(p.remoteServiceUtil)) {
                return super.compareTo(page);
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof PageImpl) {
            PageImpl p = (PageImpl) obj;
            return this.remoteServiceUtil.equals(p.remoteServiceUtil);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ("PAGE: " + super.hashCode() + remoteServiceUtil.hashCode()).hashCode();
    }
    
}
