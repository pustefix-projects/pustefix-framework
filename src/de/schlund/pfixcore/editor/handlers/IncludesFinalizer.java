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
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;
import org.apache.log4j.Category;
import org.w3c.dom.*;

/**
 * IncludesFinalizer.java
 *
 *
 * Created: Wed Dec 13 13:00:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IncludesFinalizer extends ResdocSimpleFinalizer {
    private static String EDITOR_PERF = "EDITOR_PERF";
    private static Category PERF_LOGGER = Category.getInstance(EDITOR_PERF);

    protected void renderDefault(IWrapperContainer container) throws Exception {
        long start_time = 0;
        if(PERF_LOGGER.isInfoEnabled()) {
            start_time = System.currentTimeMillis();
            PERF_LOGGER.info(this.getClass().getName()+"#renderDefault starting ");
        }
        
        Context                context     = container.getAssociatedContext();
        ContextResourceManager crm         = context.getContextResourceManager();
        EditorSessionStatus    esess       = EditorRes.getEditorSessionStatus(crm);
        EditorSearch           esearch     = EditorRes.getEditorSearch(crm);
        ResultDocument         resdoc      = container.getAssociatedResultDocument();
        
        TargetGenerator     tgen         = esess.getProduct().getTargetGenerator();
        AuxDependency       currinclude  = esess.getCurrentInclude();
        PfixcoreNamespace[] nspaces      = esess.getProduct().getPfixcoreNamespace();
        boolean             doshow       = esess.getShowAdditionalIncfiles();
        // esess.showAdditionalIncfiles(false);
        
        for (int i = 0; i < nspaces.length; i++) {
            PfixcoreNamespace nsp = nspaces[i];
            resdoc.addUsedNamespace(nsp.getPrefix(), nsp.getUri());
        }

        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
        
        // Render all includes
        TreeSet includes = tgen.getDependencyRefCounter().getDependenciesOfType(DependencyType.TEXT);
        Element root     = resdoc.createNode("allincludes"); 
        root.setAttribute("allshown", "" + doshow);
        EditorHelper.renderAllIncludesForNavigation(includes, resdoc, root, currinclude, doshow);

        TreeSet searchinc = esearch.getResultSet();
        if (searchinc != null && searchinc.size() > 0) {
            root = resdoc.createNode("currentsearchincludes");
            EditorHelper.renderAllIncludesForNavigation(searchinc, resdoc, root, null, true);
        }
        
        if(PERF_LOGGER.isInfoEnabled()) {
            long length = System.currentTimeMillis() - start_time;
            PERF_LOGGER.info(this.getClass().getName()+"#renderDefault#1: "+length);
        }
        
        // Render detailed view of currently selected include
        if (currinclude != null) {
            boolean lock    = false;
            
            
            long    mod     = currinclude.getModTime();
            Path    path    = currinclude.getPath();
            String  part    = currinclude.getPart();
            String  product = currinclude.getProduct();
            root = resdoc.createNode("currentincludeinfo");
            root.setAttribute("path",    path.getRelative());
            root.setAttribute("part",    part);
            root.setAttribute("product", product);
            root.setAttribute("modtime", "" + mod);
            root.setAttribute("havelock", "" + lock);
            
            HashSet affected_products = esess.getAffectedProductsForCurrentInclude();
            //HashSet affected_products = EditorHelper.getAffectedProductsForInclude(esess, path, part);
            boolean allowed = esess.getUser().getUserInfo().isIncludeEditAllowed(esess, affected_products);
            if(allowed) {
                lock = esess.getLock(currinclude);
            } else {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("User is not allowed to edit this include. No lock required.");
                }
            }
                       
            
            if(PERF_LOGGER.isInfoEnabled()) {
                long length = System.currentTimeMillis() - start_time;
                PERF_LOGGER.info(this.getClass().getName()+"#renderDefault#2: "+length);
            }
            // <comment>
            // Here we must handle the case that an editoruser references a
            // new include. When he selects it from the list, it is
            // not written yet. So we must NOT call EditorHelper.getAffectedProductsForInclude!!!
            // look if part exists
            Element ele = EditorHelper.getIncludePart(esess.getProduct().getTargetGenerator(), 
                                                        AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT,
                                                        path, part,esess.getProduct().getName()));   
            
            // if ele==null a new include has been referenced, but was not written yet                                           
            if(ele != null ) {
                // render all affected products for current include
                Element aff_prods = resdoc.createNode("affectedproducts");
                
                for(Iterator iter = affected_products.iterator(); iter.hasNext(); ) {
                    EditorProduct prod = (EditorProduct) iter.next();
                    String name = prod.getName();
                    Element pr = resdoc.createNode("product");
                    pr.setAttribute("name", name);
                    aff_prods.appendChild(pr);
                }
                root.appendChild(aff_prods);
            }
            //</comment>
            if(PERF_LOGGER.isInfoEnabled()) {
                long length = System.currentTimeMillis() - start_time;
                PERF_LOGGER.info(this.getClass().getName()+"#renderDefault#3: "+length);
            }
            
            
            if (!lock) {
                try {
                    EditorSessionStatus foreign = EditorLockFactory.getInstance().getLockingEditorSessionStatus(currinclude);
                    if (foreign != null) {
                        EditorUser user = foreign.getUser();
                        Element    elem = resdoc.createSubNode(root, "lockinguser");
                        user.insertStatus(resdoc, elem);
                    }
                } catch (IllegalStateException e) {
                    CAT.warn("*** Eeeeek, seems the EditorSessionStatus was suddenly GC'ed...");
                }
            }
            
            Element elem = resdoc.createSubNode(root, "affectedpages");
            EditorHelper.renderAffectedPages(esess, currinclude, resdoc, elem);

            elem = resdoc.createSubNode(root, "branchoptions");
            EditorHelper.renderBranchOptions(esess, currinclude, resdoc, elem);

            elem = resdoc.createSubNode(root, "backup");
            EditorHelper.renderBackupOptions(esess, currinclude, resdoc, elem);

            elem = resdoc.createSubNode(root, "content");
            EditorHelper.renderIncludeContent(tgen, currinclude, resdoc, elem);

            elem = resdoc.createSubNode(root, "includeinfo");
            EditorHelper.renderIncludes(currinclude, resdoc, elem);

            elem = resdoc.createSubNode(root, "imageinfo");
            EditorHelper.renderImagesFlatRecursive(currinclude, resdoc, elem);
        }
        if(PERF_LOGGER.isInfoEnabled()) {
            long length = System.currentTimeMillis() - start_time;
            PERF_LOGGER.info(this.getClass().getName()+"#renderDefault ended: "+length); 
        }
    }

  
    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }
    
}// IncludesFinalizer
