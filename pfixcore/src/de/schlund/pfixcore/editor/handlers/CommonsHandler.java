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
import org.w3c.dom.*;

/**
 * CommonsHandler.java
 *
 *
 * Created: Tue Feb 05 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class CommonsHandler extends EditorStdHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        ContextResourceManager crm      = context.getContextResourceManager();
        EditorSessionStatus    esess    = EditorRes.getEditorSessionStatus(crm);
        Commons                includes = (Commons) wrapper;
        EditorProduct          prod     = esess.getProduct();
        TargetGenerator        tgen     = prod.getTargetGenerator();
        String                 path     = includes.getPath();
        String                 part     = includes.getPart();
        String                 realprod = prod.getName();
        TreeSet                allcoms  = EditorCommonsFactory.getInstance().getAllCommons();

        if (EditorCommonsFactory.getInstance().isPathAllowed(path)) {
            AuxDependency incdef  =
                AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, "default");
            AuxDependency incprod =
                AuxDependencyFactory.getInstance().getAuxDependency(DependencyType.TEXT, path, part, realprod);
            
            if (allcoms.contains(incprod)) {
                esess.getLock(incprod); 
                esess.setCurrentCommon(incprod);
            } else {
                esess.getLock(incdef); 
                esess.setCurrentCommon(incdef);
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
            esess.getLock(common);
        }
    }

    
}// IncludesHandler
