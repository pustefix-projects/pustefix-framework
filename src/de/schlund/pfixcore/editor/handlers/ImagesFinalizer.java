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
            boolean lock = esess.getLock(currimage); 
            String  dir  = currimage.getDir();
            long    mod  = currimage.getModTime();
            String  path = currimage.getPath();
            String  name = path.substring(path.lastIndexOf("/") + 1);
            root = resdoc.createNode("currentimageinfo");
            root.setAttribute("path", path);
            root.setAttribute("name", name);
            root.setAttribute("modtime", "" + mod);
            root.setAttribute("havelock", "" + lock);

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
            String dir  = curr.getDir();
            String path = curr.getPath();
            String name = path.substring(path.lastIndexOf("/") + 1);
            if (!olddir.equals(dir) || olddir.equals("")) {
                elem   = resdoc.createSubNode(root, "directory");
                elem.setAttribute("name", dir);
                olddir = dir;
            }
            Element img = resdoc.createSubNode(elem, "image");
            img.setAttribute("path", path);
            img.setAttribute("name", name);
            if (curr.getModTime() == 0) {
                img.setAttribute("missing", "true");
            }
        }
    }
    
}// ImagesFinalizer
