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
import org.w3c.dom.*;

/**
 * ImagesFinalizer.java
 *
 *
 * Created: Fri Nov 30 14:00:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ImagesFinalizer extends ResdocSimpleFinalizer {

    protected void renderDefault(IWrapperContainer container) throws Exception{
        Context                context   = container.getAssociatedContext();
        ContextResourceManager crm       = context.getContextResourceManager();
        EditorSessionStatus    esess     = EditorRes.getEditorSessionStatus(crm);
        ResultDocument         resdoc    = container.getAssociatedResultDocument();
        EditorProduct          eprod     = esess.getProduct();
        TargetGenerator        tgen      = eprod.getTargetGenerator();
        AuxDependency          currimage = esess.getCurrentImage();
        
        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
        
        // Render all images
        TreeSet images = tgen.getDependencyRefCounter().getDependenciesOfType(DependencyType.IMAGE);
        Element root   = resdoc.createNode("allimages"); 
        renderAllImages(images, resdoc, root);
        
        // Render detailed view of currently selected target
        if (currimage != null) {
            boolean lock = false;
            boolean allowed = esess.getUser().getUserInfo().isImageEditAllowed(currimage.getPath());
            if(allowed) {
                lock = esess.getLock(currimage);
            } else {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("User is not allowed to edit this image. No lock required.");
                }
            }
            long    mod  = currimage.getModTime();
            Path    path = currimage.getPath();
            String  name = path.getName();
            root = resdoc.createNode("currentimageinfo");
            root.setAttribute("path", path.getRelative());
            root.setAttribute("name", name);
            root.setAttribute("modtime", "" + mod);
            root.setAttribute("havelock", "" + lock);
            
            // render all affected products for current image
            Element aff_prods = resdoc.createNode("affectedproducts");
            HashSet set = EditorHelper.getAffectedProductsForImage(esess.getCurrentImage().getPath());
            for(Iterator iter = set.iterator(); iter.hasNext(); ) {
                EditorProduct prod = (EditorProduct) iter.next();
                String na = prod.getName();
                Element pr = resdoc.createNode("product");
                pr.setAttribute("name", na);
                aff_prods.appendChild(pr);
            }
            root.appendChild(aff_prods);

            if (!lock) {
                try {
                    EditorSessionStatus foreign = EditorLockFactory.getInstance().getLockingEditorSessionStatus(currimage);
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
            EditorHelper.renderAffectedPages(esess, currimage, resdoc, elem);

            elem = resdoc.createSubNode(root, "backup");
            EditorHelper.renderBackupOptions(esess, currimage, resdoc, elem);
        }
    }

    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }
    
    private void renderAllImages(TreeSet images, ResultDocument resdoc, Element root) {
        String  olddir  = "";
        Element elem    = null;
        for (Iterator i = images.iterator(); i.hasNext(); ) {
            AuxDependency curr = (AuxDependency) i.next();
            Path path = curr.getPath();
            String dir  = path.getDir();
            String name = path.getName();
            if (!olddir.equals(dir) || olddir.equals("")) {
                elem   = resdoc.createSubNode(root, "directory");
                elem.setAttribute("name", dir);
                olddir = dir;
            }
            Element img = resdoc.createSubNode(elem, "image");
            img.setAttribute("path", path.getRelative());
            img.setAttribute("name", name);
            if (curr.getModTime() == 0) {
                img.setAttribute("missing", "true");
            }
        }
    }
    
  /*  private void checkAccess(EditorSessionStatus esess, Element root) throws Exception {
        StatusCodeFactory sfac  = new StatusCodeFactory("pfixcore.editor.imagesupload");
        EditorUser u = esess.getUser();
        EditorProduct          eprod       = esess.getProduct();
        ProjectPermissions perms = u.getUserInfo().getProjectPerms(eprod.getName());
        
        HashSet affected = getAffectedProductsForImage(esess.getCurrentImage().getPath());
        
        if(u.isAdmin()) {
            root.setAttribute("permisssion", "granted");
        } else if(perms == null) {
            root.setAttribute("permission", "denied");
            root.setAttribute("permission_info", "You don not have the permission to edit images of this product. No permissions found.");
        } else if(!perms.isEditImages()) {
            root.setAttribute("permission", "denied");
            root.setAttribute("permission_info", "You don not have the permission to edit images of this product");
        } else if(perms.isEditImages()) {
            // check if image is uses by another product
            Iterator iter = affected.iterator();
            StringBuffer sb = new StringBuffer();
            boolean denied = false;
            while(iter.hasNext()) {
                String name = ((EditorProduct)iter.next()).getName();
                ProjectPermissions p = u.getUserInfo().getProjectPerms(name);
                if(p == null || !p.isEditImages()) {
                    denied = true;
                    sb.append(name).append(" ");
                }   
            }
            if(denied) {
                root.setAttribute("permission", "denied");
                String scode1 = sfac.getStatusCode("NO_PERM_SHARED").getDefaultMessage();
                root.setAttribute("permission_info", scode1 + sb.toString() + ".");
            }  else 
                root.setAttribute("permission", "granted");
        } else {
            root.setAttribute("permission", "denied");
            root.setAttribute("permission_info", "Permission denied for unkown reason.");
        }    
    }
    */
   
    
    
}// ImagesFinalizer
