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

/**
 * DocumentationHandler.java
 *
 *
 * Created: Wed Nov 06 03:05:24 2002
 *
 * @author <a href="mailto:zaich@schlund.de">Volker Zaich</a>
 *
 *
 */


public class DocumentationHandler implements IHandler {
	
	/**
	 * @see IHandler#handleSubmittedData(Context, IWrapper)
	 */
	public void handleSubmittedData(Context context, IWrapper wrapper)
		throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        Documentation          doc    =(Documentation) wrapper;
        EditorProduct          prod   = esess.getProduct();
        

        
        if (doc.getId() != null) {
			esess.setCurrentDocumentationId(CoreDocumentation.encode(doc.getId()));
		}
	}

	/**
	 * @see IHandler#retrieveCurrentStatus(Context, IWrapper)
	 */
	public void retrieveCurrentStatus(Context context, IWrapper wrapper)
		throws Exception {
		Documentation          doc    =(Documentation) wrapper;
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        EditorProduct          prod   = esess.getProduct();
		
		
		
		if (doc.getId() != null) {		
		//	esess.setCurrentDocumentationId(doc.getId());
		}
			
	}

	/**
	 * @see IHandler#prerequisitesMet(Context)
	 */
	public boolean prerequisitesMet(Context context) throws Exception {
		return true;
	}

	/**
	 * @see IHandler#isActive(Context)
	 */
	public boolean isActive(Context context) throws Exception {
		return true;
	}

	/**
	 * @see IHandler#needsData(Context)
	 */
	public boolean needsData(Context context) throws Exception {
		return true;
	}

}
