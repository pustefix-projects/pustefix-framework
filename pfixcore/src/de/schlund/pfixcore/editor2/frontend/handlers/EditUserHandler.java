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

package de.schlund.pfixcore.editor2.frontend.handlers;

import java.util.Iterator;

import org.pustefixframework.CoreStatusCodes;
import org.pustefixframework.editor.EditorStatusCodes;

import de.schlund.pfixcore.editor2.core.dom.Project;
import de.schlund.pfixcore.editor2.core.vo.EditorGlobalPermissions;
import de.schlund.pfixcore.editor2.core.vo.EditorProjectPermissions;
import de.schlund.pfixcore.editor2.core.vo.EditorUser;
import de.schlund.pfixcore.editor2.frontend.util.EditorResourceLocator;
import de.schlund.pfixcore.editor2.frontend.util.SpringBeanLocator;
import de.schlund.pfixcore.editor2.frontend.wrappers.EditUser;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.util.UnixCrypt;
import de.schlund.pfixcore.workflow.Context;

/**
 * Handles user edit
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditUserHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper)
            throws Exception {
        EditUser input = (EditUser) wrapper;
        String name = null;
        String section = null;
        String phone = null;
        name = input.getName();
        if (name == null) {
            input.addSCodeName(CoreStatusCodes.MISSING_PARAM);
        }
        section = input.getSection();
        if (section == null) {
            input.addSCodeSection(CoreStatusCodes.MISSING_PARAM);
        }
        phone = input.getPhone();
        if (phone == null) {
            input.addSCodePhone(CoreStatusCodes.MISSING_PARAM);
        }
        EditorUser user = EditorResourceLocator.getUsersResource(context)
                .getSelectedUser();
        if (name != null && section != null && phone != null) {
            String pwd = input.getPassword();
            if (pwd != null) {
                if (pwd.equals(input.getPasswordRepeat())) {
                    user.setCryptedPassword(UnixCrypt.crypt(pwd));
                } else {
                    input
                            .addSCodePassword(EditorStatusCodes.USERDATA_PWD_NO_MATCH);
                    return;
                }
            } else if (input.getPasswordRepeat() != null) {
                input
                        .addSCodePassword(EditorStatusCodes.USERDATA_PWD_NO_MATCH);
                return;
            }
            
            // Make sure user is always created with a password
            if (!EditorResourceLocator.getUsersResource(context).existsSelectedUser()
                    && pwd == null && input.getPasswordRepeat() == null) {
                input.addSCodePassword(CoreStatusCodes.MISSING_PARAM);
                return;
            }
            
            user.setFullname(name);
            user.setSectionName(section);
            user.setPhoneNumber(phone);

            EditorGlobalPermissions gPermissions = user.getGlobalPermissions();
            boolean isAdmin = false;
            if (input.getAdminPrivilege() != null
                    && input.getAdminPrivilege().booleanValue()) {
                isAdmin = true;
            }
            gPermissions.setAdmin(isAdmin);
            boolean isEditDynIncludes = false;
            if (input.getEditDynIncludesPrivilege() != null
                    && input.getEditDynIncludesPrivilege().booleanValue()) {
                isEditDynIncludes = true;
            }
            gPermissions.setEditDynIncludes(isEditDynIncludes);
            user.setGlobalPermissions(gPermissions);

            for (Iterator<Project> i = SpringBeanLocator.getProjectFactoryService()
                    .getProjects().iterator(); i.hasNext();) {
                Project project = i.next();
                boolean isEditImages = false;
                if (input.getEditImagesPrivilege(project.getName()) != null
                        && input.getEditImagesPrivilege(project.getName())
                                .booleanValue()) {
                    isEditImages = true;
                }
                boolean isEditIncludes = false;
                if (input.getEditIncludesPrivilege(project.getName()) != null
                        && input.getEditIncludesPrivilege(project.getName())
                                .booleanValue()) {
                    isEditIncludes = true;
                }
                EditorProjectPermissions permissions = new EditorProjectPermissions();
                permissions.setEditImages(isEditImages);
                permissions.setEditIncludes(isEditIncludes);
                user.setProjectPermissions(project, permissions);
            }

            EditorResourceLocator.getUsersResource(context).updateSelectedUser();
            context.addPageMessage(EditorStatusCodes.USERDATA_CHANGES_SAVED, null, null);
        }

    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        EditUser input = (EditUser) wrapper;
        EditorUser user = EditorResourceLocator.getUsersResource(context)
                .getSelectedUser();
        input.setName(user.getFullname());
        input.setSection(user.getSectionName());
        input.setPhone(user.getPhoneNumber());
        if (user.getGlobalPermissions().isAdmin()) {
            input.setAdminPrivilege(new Boolean(true));
        } else {
            input.setAdminPrivilege(new Boolean(false));
        }
        if (user.getGlobalPermissions().isEditDynIncludes()) {
            input.setEditDynIncludesPrivilege(new Boolean(true));
        } else {
            input.setEditDynIncludesPrivilege(new Boolean(false));
        }
        for (Iterator<Project> i = SpringBeanLocator.getProjectFactoryService()
                .getProjects().iterator(); i.hasNext();) {
            Project project = i.next();
            EditorProjectPermissions permissions = user
                    .getProjectPermissions(project);
            if (permissions.isEditImages()) {
                input.setEditImagesPrivilege(new Boolean(true), project
                        .getName());
            } else {
                input.setEditImagesPrivilege(new Boolean(false), project
                        .getName());
            }
            if (permissions.isEditIncludes()) {
                input.setEditIncludesPrivilege(new Boolean(true), project
                        .getName());
            } else {
                input.setEditIncludesPrivilege(new Boolean(false), project
                        .getName());
            }
        }
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // User has to be selected
        return (EditorResourceLocator.getUsersResource(context)
                .getSelectedUser() != null);
    }

    public boolean isActive(Context context) throws Exception {
        // Always await input
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask for selection
        return true;
    }

}
