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
import de.schlund.util.statuscodes.*;
import de.schlund.pfixxml.targets.*;
import java.util.*;

import org.apache.log4j.Category;

/**
 * CommonsHandler.java
 *
 *  Handler responsible for selecting commons.
 *
 * Created: Tue Feb 05 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class CommonsHandler extends EditorStdHandler {
    private static Category CAT = Category.getInstance(CommonsHandler.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        Commons                includes = (Commons) wrapper;
        EditorProduct          prod     = esess.getProduct();
        Path                   path     = Path.create(prod.getTargetGenerator().getDocroot(), includes.getPath());
        String                 part     = includes.getPart();
        String                 realprod = prod.getName();
        TreeSet                allcoms  = EditorCommonsFactory.getInstance().getAllCommons();

        if (EditorCommonsFactory.getInstance().isPathAllowed(path)) {
            AuxDependency incdef  =
                AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, "default");
            AuxDependency incprod =
                AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, realprod);
            
            if (allcoms.contains(incprod)) {
                esess.setCurrentCommon(incprod);
                String editor_product = esess.getProduct().getName();
                String incl_product = esess.getCurrentCommon().getProduct();
                boolean allowed = esess.getUser().getUserInfo().isDynIncludeEditAllowed(editor_product, incl_product);
                if(allowed) {
                    esess.getLock(incprod); 
                } else {
                    if(CAT.isDebugEnabled()) 
                        CAT.debug("User is not allowed to edit this dyninclude. No lock required!");
                }
                
            } else {
                esess.setCurrentCommon(incdef);
                String editor_product = esess.getProduct().getName();
                String incl_product = esess.getCurrentCommon().getProduct();
                boolean allowed = esess.getUser().getUserInfo().isDynIncludeEditAllowed(editor_product, incl_product);
                if(allowed) 
                    esess.getLock(incdef);
                else {
                    if(CAT.isDebugEnabled())
                        CAT.debug("User is not allowed to edit this dyninclude. No lock required!");
                }
            }
        } else {
            StatusCodeFactory sfac  = new StatusCodeFactory("pfixcore.editor.commons");
            StatusCode        scode = sfac.getStatusCode("COMMONPATH_UNDEF");
            includes.addSCodePath(scode);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        AuxDependency          common = esess.getCurrentCommon();
        
        if (common != null) {
            boolean allowed = esess.getUser().getUserInfo().isDynIncludeEditAllowed(esess.getProduct().getName(), common.getProduct());
            if(allowed)
                esess.getLock(common);
            else {
                if(CAT.isDebugEnabled())
                    CAT.debug("User is not allowed to edit thsi dyninclude. No lock required.");
            }
        }
    }

    
}// IncludesHandler
