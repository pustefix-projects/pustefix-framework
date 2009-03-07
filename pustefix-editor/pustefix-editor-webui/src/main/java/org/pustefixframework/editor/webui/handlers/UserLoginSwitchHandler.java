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
 */

package org.pustefixframework.editor.webui.handlers;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.webui.resources.SessionResource;
import org.pustefixframework.editor.webui.wrappers.UserLoginSwitch;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles enable/disable of user logins
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class UserLoginSwitchHandler implements IHandler {

    private SessionResource sessionResource;

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        UserLoginSwitch input = (UserLoginSwitch) wrapper;
        if (input.getAllow() != null) {
            sessionResource.setUserLoginsAllowed(input.getAllow().booleanValue());
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        // Do not insert data
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // Always enable this wrapper
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        // Always enable this wrapper
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        // Never ask for input
        return false;
    }

    @Inject
    public void setSessionResource(SessionResource sessionResource) {
        this.sessionResource = sessionResource;
    }

}
