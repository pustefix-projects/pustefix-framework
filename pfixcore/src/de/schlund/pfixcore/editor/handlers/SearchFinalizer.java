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
import de.schlund.util.statuscodes.*;
import org.w3c.dom.*;

/**
 * SearchFinalizer.java
 *
 *
 * Created: Tue Dec 04 23:55:33 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class SearchFinalizer extends ResdocSimpleFinalizer {

    public void onSuccess(IWrapperContainer container) throws Exception{
        renderDefault(container);
    }
    
    protected void renderDefault(IWrapperContainer container) throws Exception{
        Context                context = container.getAssociatedContext();
        ContextResourceManager crm     = context.getContextResourceManager();
        EditorSessionStatus    esess   = EditorRes.getEditorSessionStatus(crm);
        EditorSearch           es      = EditorRes.getEditorSearch(crm);
        ResultDocument         resdoc  = container.getAssociatedResultDocument();

        // Release a locks that may be held
        esess.releaseLock();
        
        // Render the current status of the editor session
        esess.insertStatus(resdoc, resdoc.createNode("cr_editorsession"));

        StatusCode status = es.getStatus();
        if (status == EditorSearch.SCODE_OK) {
            Element root = resdoc.createNode("matchingincludes"); 
            EditorHelper.renderAllPatternMatchingIncludes(es, resdoc, root, EditorSearch.INCLUDE);
            root = resdoc.createNode("matchingcommons"); 
            EditorHelper.renderAllPatternMatchingIncludes(es, resdoc, root, EditorSearch.COMMON);
        }
    }
    
}// SearchFinalizer
