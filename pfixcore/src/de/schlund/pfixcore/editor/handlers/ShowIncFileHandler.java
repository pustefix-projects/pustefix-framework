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

import de.schlund.pfixcore.editor.interfaces.*;
import de.schlund.pfixcore.editor.resources.EditorRes;
import de.schlund.pfixcore.editor.resources.EditorSessionStatus;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

/**
 * Describe class ShowIncFileHandler here.
 *
 *
 * Created: Thu Jul  8 12:44:52 2004
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version $Id$
 */
public class ShowIncFileHandler implements IHandler {

    // Implementation of de.schlund.pfixcore.generator.IHandler

    /**
     * Describe <code>handleSubmittedData</code> method here.
     *
     * @param context a <code>Context</code> value
     * @param IWrapper an <code>IWrapper</code> value
     * @exception Exception if an error occurs
     */
    public final void handleSubmittedData(final Context context, final IWrapper wrapper) throws Exception {
        ContextResourceManager crm    = context.getContextResourceManager();
        EditorSessionStatus    esess  = EditorRes.getEditorSessionStatus(crm);
        ShowIncFile            sinc   = (ShowIncFile) wrapper;
        boolean                doshow = sinc.getDoShow().booleanValue();
        
        esess.showAdditionalIncfiles(doshow);
    }

    /**
     * Describe <code>retrieveCurrentStatus</code> method here.
     *
     * @param context a <code>Context</code> value
     * @param IWrapper an <code>IWrapper</code> value
     * @exception Exception if an error occurs
     */
    public final void retrieveCurrentStatus(final Context context, final IWrapper wrapper) throws Exception {
        // None
    }

    /**
     * Describe <code>isActive</code> method here.
     *
     * @param context a <code>Context</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public final boolean isActive(final Context context) throws Exception {
        return true;
    }

    /**
     * Describe <code>prerequisitesMet</code> method here.
     *
     * @param context a <code>Context</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public final boolean prerequisitesMet(final Context context) throws Exception {
        return true;
    }

    /**
     * Describe <code>needsData</code> method here.
     *
     * @param context a <code>Context</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public final boolean needsData(final Context context) throws Exception {
        return false;
    }

}
