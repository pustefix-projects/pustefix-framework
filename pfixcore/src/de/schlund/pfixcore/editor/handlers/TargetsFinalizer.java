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
import org.w3c.dom.*;
import java.util.*;

/**
 * TargetsFinalizer.java
 *
 *
 * Created: Fri Nov 30 14:00:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class TargetsFinalizer extends ResdocSimpleFinalizer {

    protected void renderDefault(IWrapperContainer container) throws Exception {
        Context                context    = container.getAssociatedContext();
        ContextResourceManager crm        = context.getContextResourceManager();
        EditorSessionStatus    esess      = EditorRes.getEditorSessionStatus(crm);
        ResultDocument         resdoc     = container.getAssociatedResultDocument();
        EditorProduct          eprod      = esess.getProduct();
        TargetGenerator        tgen       = eprod.getTargetGenerator();
        Target                 currtarget = esess.getCurrentTarget();
        
        // Release a locks that may be held
        esess.releaseLock();

        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));
        
        // Render all targets
        TreeSet targets = tgen.getPageTargetTree().getToplevelTargets();
        Element root    = resdoc.createNode("alltargets"); 
        renderAllTargets(targets, resdoc, root);
        
        // Render detailed view of currently selected target
        if (currtarget != null) {
            root = resdoc.createNode("currenttargetinfo");
            root.setAttribute("name", currtarget.getTargetKey());
            if (currtarget.getXMLSource() != null) {
                root.setAttribute("xmlsrc", currtarget.getXMLSource().getTargetKey());
            }
            if (currtarget.getXSLSource() != null) {
                root.setAttribute("xslsrc", currtarget.getXSLSource().getTargetKey());
            }

            Element elem  = resdoc.createSubNode(root, "paraminfo");
            TreeMap param = currtarget.getParams();
            if (param != null) {
                int j = 0;
                for (Iterator i = param.keySet().iterator(); i.hasNext(); ) {
                    String  key = (String) i.next();
                    String  val = (String) param.get(key);
                    Element tmp = resdoc.createSubNode(elem, "param");
                    tmp.setAttribute("key", key);
                    tmp.setAttribute("value", val);
                    tmp.setAttribute("count", "" + j++);
                }
            }
            
            elem = resdoc.createSubNode(root, "auxfileinfo");
            EditorHelper.renderAuxfiles(currtarget, resdoc, elem);
             
            elem = resdoc.createSubNode(root, "includeinfo");
            EditorHelper.renderIncludes(currtarget, resdoc, elem);
             
            elem = resdoc.createSubNode(root, "imageinfo");
            EditorHelper.renderImages(currtarget, resdoc, elem);

            elem = resdoc.createSubNode(root, "content");
            EditorHelper.renderTargetContent(currtarget, resdoc, elem);

            elem = resdoc.createSubNode(root, "affectedpages");
            EditorHelper.renderAffectedPages(esess, currtarget, resdoc, elem);
        }
    }

    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }
    
    private void renderAllTargets(TreeSet targets, ResultDocument resdoc, Element root)  throws Exception{
        for (Iterator i = targets.iterator(); i.hasNext(); ) {
            Target curr = (Target) i.next();
            EditorHelper.renderSingleTarget(curr, resdoc, root);
        }
    }
    
}// TargetsFinalizer
