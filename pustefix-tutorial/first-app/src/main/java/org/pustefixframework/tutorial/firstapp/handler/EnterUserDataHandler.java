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
package org.pustefixframework.tutorial.firstapp.handler;

import org.pustefixframework.tutorial.firstapp.User;
import org.pustefixframework.tutorial.firstapp.wrapper.EnterUserDataWrapper;
import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;


public class EnterUserDataHandler implements InputHandler<EnterUserDataWrapper> {

    @Autowired
    User user;

    public void handleSubmittedData(EnterUserDataWrapper wrapper) {
        user.setSex(wrapper.getSex());
        user.setName(wrapper.getName());
        user.setEmail(wrapper.getEmail());
        user.setHomepage(wrapper.getHomepage());
        user.setBirthday(wrapper.getBirthday());
        user.setAdmin(wrapper.getAdmin());
    }

    public boolean isActive() {
        return true;
    }

    public boolean needsData() {
        return user.getName() == null;
    }

    public boolean prerequisitesMet() {
        return true;
    }

    public void retrieveCurrentStatus(EnterUserDataWrapper wrapper) {
        if (user.getName() == null) {
            return;
        }
        wrapper.setSex(user.getSex());
        wrapper.setName(user.getName());
        wrapper.setEmail(user.getEmail());
        wrapper.setHomepage(user.getHomepage());
        wrapper.setBirthday(user.getBirthday());
        wrapper.setAdmin(user.getAdmin());
    }

}