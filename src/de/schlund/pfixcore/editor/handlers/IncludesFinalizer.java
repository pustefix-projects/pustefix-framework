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
import de.schlund.pfixcore.editor.auth.ProjectPermissions;
import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.pfixcore.workflow.Navigation.*;
import de.schlund.pfixcore.workflow.app.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import java.util.*;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.*;
import java.io.*;

import javax.xml.transform.TransformerException;

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

    protected void renderDefault(IWrapperContainer container) throws Exception {
        Context                context     = container.getAssociatedContext();
        ContextResourceManager crm         = context.getContextResourceManager();
        EditorSessionStatus    esess       = EditorRes.getEditorSessionStatus(crm);
        EditorSearch           esearch     = EditorRes.getEditorSearch(crm);
        ResultDocument         resdoc      = container.getAssociatedResultDocument();
        
        TargetGenerator        tgen        = esess.getProduct().getTargetGenerator();
        AuxDependency          currinclude = esess.getCurrentInclude();
        PfixcoreNamespace[]    nspaces     = esess.getProduct().getPfixcoreNamespace();

        for (int i = 0; i < nspaces.length; i++) {
            PfixcoreNamespace nsp = nspaces[i];
            resdoc.addUsedNamespace(nsp.getPrefix(), nsp.getUri());
        }

        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
        
        // Render all includes
        TreeSet includes = tgen.getDependencyRefCounter().getDependenciesOfType(DependencyType.TEXT);
        Element root     = resdoc.createNode("allincludes"); 
        EditorHelper.renderAllIncludesForNavigation(includes, resdoc, root);

        TreeSet searchinc = esearch.getResultSet();
        if (searchinc != null && searchinc.size() > 0) {
            root = resdoc.createNode("currentsearchincludes");
            EditorHelper.renderAllIncludesForNavigation(searchinc, resdoc, root);
        }
        
        // Render detailed view of currently selected include
        if (currinclude != null) {
            boolean lock    = esess.getLock(currinclude); 
            String  dir     = currinclude.getDir();
            long    mod     = currinclude.getModTime();
            String  path    = currinclude.getPath();
            String  part    = currinclude.getPart();
            String  product = currinclude.getProduct();
            root = resdoc.createNode("currentincludeinfo");
            root.setAttribute("path",    path);
            root.setAttribute("part",    part);
            root.setAttribute("product", product);
            root.setAttribute("modtime", "" + mod);
            root.setAttribute("havelock", "" + lock);
            
            
            // render all affected products for current include
            Element aff_prods = resdoc.createNode("affectedproducts");
            HashSet set = EditorHelper.getAffectedProductsForInclude(esess, path, part);
            for(Iterator iter = set.iterator(); iter.hasNext(); ) {
                EditorProduct prod = (EditorProduct) iter.next();
                String name = prod.getName();
                Element pr = resdoc.createNode("product");
                pr.setAttribute("name", name);
                aff_prods.appendChild(pr);
            }
            root.appendChild(aff_prods);
            
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
    }

  
    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }
    
}// IncludesFinalizer
