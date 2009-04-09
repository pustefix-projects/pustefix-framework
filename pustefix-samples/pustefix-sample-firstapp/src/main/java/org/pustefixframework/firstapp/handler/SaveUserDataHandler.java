/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.firstapp.handler;

import org.pustefixframework.firstapp.contextresources.ContextUser;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

public class SaveUserDataHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        System.out.println("Business logic to save data");
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return false;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        if (cUser.getUser() != null) {
            return true;
        }
        return false;
    }

    public void retrieveCurrentStatus(Context context, IWrapper arg1)
            throws Exception {
        // Nothing to be done here
    }
}
