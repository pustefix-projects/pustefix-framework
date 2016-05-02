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

package org.pustefixframework.editor.backend.remote;

import java.util.LinkedList;
import java.util.List;

import org.pustefixframework.editor.common.dom.Page;
import org.pustefixframework.editor.common.dom.Theme;
import org.pustefixframework.editor.common.dom.Variant;
import org.pustefixframework.editor.common.remote.service.RemotePageService;
import org.pustefixframework.editor.common.remote.transferobjects.PageTO;

import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.VariantFactoryService;


public class RemotePageServiceImpl implements RemotePageService {
    
    private ProjectFactoryService projectFactoryService;
    private VariantFactoryService variantFactoryService;
    
    public void setProjectFactoryService(ProjectFactoryService projectFactoryService) {
        this.projectFactoryService = projectFactoryService;
    }
    
    public void setVariantFactoryService(VariantFactoryService variantFactoryService) {
        this.variantFactoryService = variantFactoryService;
    }
    
    public PageTO getPage(String fullname) {
        return convertToTransferObject(findPage(fullname));
    }
    
    public List<PageTO> getPageByName(String name) {
        LinkedList<PageTO> pages = new LinkedList<PageTO>();
        for (Page p : projectFactoryService.getProject().getPageByName(name)) {
            pages.add(convertToTransferObject(p));
        }
        return pages;
    }
    
    public void registerForUpdate(String pageName) {
        findPage(pageName).registerForUpdate();
    }
    
    public void update(String pageName) {
        findPage(pageName).update();
    }
    
    private PageTO convertToTransferObject(Page page) {
        PageTO to = new PageTO();
        to.name = page.getName();
        to.variant = (page.getVariant() == null) ? null : page.getVariant().getName();
        if (page.getPageTarget() != null) {
            to.target = page.getPageTarget().getName();
        }
        for (Page p : page.getSubPages()) {
            to.subPages.add(convertToTransferObject(p));
        }
        for (Theme t : page.getThemes().getThemes()) {
            to.themes.add(t.getName());
        }
        return to;
    }
    
    private Page findPage(String fullname) {
        String pageName;
        Variant variant;
        int colonsPos = fullname.indexOf("::");
        if (colonsPos == -1) {
            pageName = fullname;
            variant = null;
        } else {
            pageName = fullname.substring(0, colonsPos);
            variant = variantFactoryService.getVariant(fullname.substring(colonsPos + 2));
        }
        Page page = projectFactoryService.getProject().getPage(pageName, variant);
        return page;
    }
}
