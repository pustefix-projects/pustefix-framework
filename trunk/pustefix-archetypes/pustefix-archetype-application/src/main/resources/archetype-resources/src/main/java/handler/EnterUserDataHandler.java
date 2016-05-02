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
#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.handler;

import ${package}.User;
import ${package}.contextresources.ContextUser;
import ${package}.wrapper.EnterUserDataWrapper;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;

public class EnterUserDataHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);

        EnterUserDataWrapper euWrapper = (EnterUserDataWrapper)wrapper;
        User user = new User();
        user.setSex(euWrapper.getSex());
        user.setName(euWrapper.getName());
        if (euWrapper.getEmail() != null) {
            user.setEmail(euWrapper.getEmail());
        }
        if (euWrapper.getHomepage() != null) {
            user.setHomepage(euWrapper.getHomepage());
        }
        if (euWrapper.getBirthdate() != null) {
            user.setBirthday(euWrapper.getBirthdate());
        }
        euWrapper.setAdmin(euWrapper.getAdmin());
        cUser.setUser(user);
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        if (cUser.getUser() == null) {
            return true;
        }
        return false;
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        ContextResourceManager manager = context.getContextResourceManager();
        ContextUser cUser = manager.getResource(ContextUser.class);
        User user = cUser.getUser();
        if (user == null) {
            return;
        }
        EnterUserDataWrapper euWrapper = (EnterUserDataWrapper)wrapper;

        euWrapper.setSex(user.getSex());
        euWrapper.setName(user.getName());
        euWrapper.setEmail(user.getEmail());
        euWrapper.setHomepage(user.getHomepage());
        euWrapper.setBirthdate(user.getBirthday());
        euWrapper.setAdmin(user.getAdmin());
    }
}
