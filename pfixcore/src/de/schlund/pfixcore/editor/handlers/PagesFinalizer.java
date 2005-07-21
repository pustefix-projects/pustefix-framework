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
 *
 */

package de.schlund.pfixcore.editor.handlers;
import de.schlund.pfixcore.editor.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.Navigation.*;
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import java.util.Iterator;
import java.util.TreeSet;
import org.w3c.dom.*;

/**
 * PagesFinalizer.java
 *
 *
 * Created: Fri Nov 30 14:00:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class PagesFinalizer extends ResdocSimpleFinalizer {

    protected void renderDefault(IWrapperContainer container) throws Exception{
        Context                context  = container.getAssociatedContext();
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        ResultDocument         resdoc   = container.getAssociatedResultDocument();
        EditorProduct          eprod    = esess.getProduct();
        TargetGenerator        tgen     = eprod.getTargetGenerator();
        PageTargetTree         ptree    = tgen.getPageTargetTree();
        Navigation             navi     = eprod.getNavigation();
        PageInfo               currpage = esess.getCurrentPage();
        
        // Release a locks that may be held
        esess.releaseLock();

        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
        
        // Render all pages
        NavigationElement[] nelems = navi.getNavigationElements();
        Element root = resdoc.createNode("allpages");
        renderAllPages(ptree, nelems, resdoc, root);

        // Render detailed view of currently selected page
        if (currpage != null) {
            root = resdoc.createNode("currentpageinfo");
            root.setAttribute("name", currpage.getName());
            if (currpage.getVariant() != null) {
                root.setAttribute("variant", currpage.getVariant());
            }

            Target  target = ptree.getTargetForPageInfo(currpage);
            Element elem   = resdoc.createSubNode(root, "targetinfo");
            EditorHelper.renderSingleTarget(target, resdoc, elem);

            elem = resdoc.createSubNode(root, "includeinfo");
            EditorHelper.renderIncludesFlatRecursive(target, resdoc, elem);

            elem = resdoc.createSubNode(root, "imageinfo");
            EditorHelper.renderImagesFlatRecursive(target, resdoc, elem);
        }
    }

    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }

    private void renderAllPages(PageTargetTree ptree, NavigationElement[] pages, ResultDocument resdoc, Element root) throws Exception{
        for (int i = 0; i < pages.length; i++) {
            NavigationElement   page   = pages[i];
            String              name   = page.getName();

            TreeSet pinfos = ptree.getPageInfoForPageName(name);
            Element elem   = null;
            if (pinfos != null) {
                for (Iterator j = pinfos.iterator(); j.hasNext();) {
                    PageInfo pinfo = (PageInfo) j.next();
                    elem           = resdoc.createSubNode(root, "page");
                    elem.setAttribute("name", name);
                    elem.setAttribute("handler", page.getHandler());
                    if (pinfo.getVariant() != null) {
                        elem.setAttribute("variant", pinfo.getVariant());
                    }
                }
            }
            NavigationElement[] childs = page.getChildren();
            if (childs != null && childs.length > 0) {
                renderAllPages(ptree, childs, resdoc, elem);
            }
        }
    }
    
}// PagesFinalizer
