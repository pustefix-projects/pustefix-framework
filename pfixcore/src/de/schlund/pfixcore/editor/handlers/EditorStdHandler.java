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

/**
 * EditorStdHandler.java
 *
 *
 * Created: Fri Nov 30 13:53:21 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public abstract class EditorStdHandler implements IHandler {

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // Never
    }
        
    public boolean prerequisitesMet(Context context) {
        ContextResourceManager crm   = context.getContextResourceManager();
        EditorSessionStatus    esess = EditorRes.getEditorSessionStatus(crm);
        if (esess.getProduct() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    public boolean isActive(Context context) {
        return true;
    }
    
    public boolean needsData(Context context) {
        // We always want to stop here.
        return true;
    }

}// EditorStdHandler
