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
import de.schlund.util.*;
import de.schlund.util.statuscodes.*;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.*;
import de.schlund.util.statuscodes.*;
import java.util.*;

import org.apache.log4j.Category;
import org.w3c.dom.*;

/**
 * IncludesHandler.java
 *
 *
 * Created: Wed Dec 13 12:34:24 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IncludesHandler extends EditorStdHandler {
    private static Category CAT = Category.getInstance(IncludesHandler.class.getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        Includes               includes = (Includes) wrapper;
        EditorProduct          prod     = esess.getProduct();
        TargetGenerator        tgen     = prod.getTargetGenerator();
        TreeSet                allinc   = tgen.getDependencyRefCounter().getDependenciesOfType(DependencyType.TEXT);
        String                 path     = includes.getPath();
        String                 part     = includes.getPart();
        String                 realprod = prod.getName();
        
        AuxDependency incdef  = AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT,
                                                                                    path, part, "default");
        AuxDependency incprod = AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT,
                                                                                    path, part, realprod);

        if (allinc.contains(incdef) && !allinc.contains(incprod)) {
            esess.setCurrentInclude(incdef);
            boolean allowed = esess.getUser().getUserInfo().isIncludeEditAllowed(esess);
            if(allowed)
                esess.getLock(incdef);
            else {
                if(CAT.isDebugEnabled()) 
                    CAT.debug("User is not allowed to edit this include. No lock required."); 
            }
        } else if (!allinc.contains(incdef) && allinc.contains(incprod)) {
            esess.setCurrentInclude(incprod);
            boolean allowed = esess.getUser().getUserInfo().isIncludeEditAllowed(esess);
            if(allowed)
                esess.getLock(incprod);
            else 
                CAT.warn("User is not allowed to edit this include. No lock required.");
            
        } else if (allinc.contains(incdef) && allinc.contains(incprod)) {
            // This can be the case when a prod.spec. branch has just been created/deleted but not
            // all targets have been updated yet. We need to look into the part to make sure.
            Object LOCK = FileLockFactory.getInstance().getLockObj(incprod.getPath());
            synchronized (LOCK) {
                Node partnode = EditorHelper.getIncludePart(tgen, incprod);
                esess.setCurrentInclude(incprod);
                boolean allowed = esess.getUser().getUserInfo().isIncludeEditAllowed(esess);
                if (partnode == null) {
                    if(allowed)
                        esess.getLock(incdef);
                    else
                        CAT.warn("User is not allowed to edit this include. No lock required."); 
                    esess.setCurrentInclude(incdef);
                } else {
                    if(allowed)
                        esess.getLock(incprod);
                    else
                        CAT.warn("User is not allowed to edit this include. No lock required."); 
                    
                }
            }
        } else {
            StatusCodeFactory sfac  = new StatusCodeFactory("pfixcore.editor.includes");
            StatusCode        scode = sfac.getStatusCode("INCLUDE_UNDEF");
            includes.addSCodePath(scode);
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        AuxDependency          currinc  = esess.getCurrentInclude();
        if (currinc != null) {
            esess.getLock(currinc);
        }
    }

    
}// IncludesHandler
