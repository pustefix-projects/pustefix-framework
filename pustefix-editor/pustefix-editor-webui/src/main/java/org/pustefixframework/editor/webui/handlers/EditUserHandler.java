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

import java.util.LinkedHashSet;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.common.dom.Project;
import org.pustefixframework.editor.generated.EditorStatusCodes;
import org.pustefixframework.editor.webui.resources.UsersResource;
import org.pustefixframework.editor.webui.wrappers.EditUser;
import org.pustefixframework.generated.CoreStatusCodes;

import de.schlund.pfixcore.editor2.core.spring.ProjectPool;
import de.schlund.pfixcore.editor2.core.vo.EditorGlobalPermissions;
import de.schlund.pfixcore.editor2.core.vo.EditorProjectPermissions;
import de.schlund.pfixcore.editor2.core.vo.EditorUser;
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
    private ProjectPool projectPool;
    
    private UsersResource usersResource;

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
        EditorUser user = usersResource.getSelectedUser();
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
            if (!usersResource.existsSelectedUser()
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
            user.setGlobalPermissions(gPermissions);
            
            for (String projectName : getProjectNames(user)) {
                boolean isEditImages = false;
                if (input.getEditImagesPrivilege(projectName) != null
                        && input.getEditImagesPrivilege(projectName)
                                .booleanValue()) {
                    isEditImages = true;
                }
                boolean isEditIncludes = false;
                if (input.getEditIncludesPrivilege(projectName) != null
                        && input.getEditIncludesPrivilege(projectName)
                                .booleanValue()) {
                    isEditIncludes = true;
                }
                EditorProjectPermissions permissions = new EditorProjectPermissions();
                permissions.setEditImages(isEditImages);
                permissions.setEditIncludes(isEditIncludes);
                user.setProjectPermissions(projectName, permissions);
            }

            usersResource.updateSelectedUser();
            context.addPageMessage(EditorStatusCodes.USERDATA_CHANGES_SAVED, null, null);
        }

    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper)
            throws Exception {
        EditUser input = (EditUser) wrapper;
        EditorUser user = usersResource.getSelectedUser();
        input.setName(user.getFullname());
        input.setSection(user.getSectionName());
        input.setPhone(user.getPhoneNumber());
        if (user.getGlobalPermissions().isAdmin()) {
            input.setAdminPrivilege(new Boolean(true));
        } else {
            input.setAdminPrivilege(new Boolean(false));
        }
        for (String projectName : getProjectNames(user)) {
            EditorProjectPermissions permissions = user
                    .getProjectPermissions(projectName);
            if (permissions.isEditImages()) {
                input.setEditImagesPrivilege(new Boolean(true), projectName);
            } else {
                input.setEditImagesPrivilege(new Boolean(false), projectName);
            }
            if (permissions.isEditIncludes()) {
                input.setEditIncludesPrivilege(new Boolean(true), projectName);
            } else {
                input.setEditIncludesPrivilege(new Boolean(false), projectName);
            }
        }
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        // User has to be selected
        return (usersResource.getSelectedUser() != null);
    }

    public boolean isActive(Context context) throws Exception {
        // Always await input
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        // Always ask for selection
        return true;
    }
    
    private LinkedHashSet<String> getProjectNames(EditorUser user) {
        LinkedHashSet<String> projectNames = new LinkedHashSet<String>();
        projectNames.addAll(user.getProjectsWithPermissions());
        for (Project project : projectPool.getProjects()) {
            projectNames.add(project.getName());
        }
        return projectNames;
    }

    @Inject
    public void setProjectPool(ProjectPool projectPool) {
        this.projectPool = projectPool;
    }

    @Inject
    public void setUsersResource(UsersResource usersResource) {
        this.usersResource = usersResource;
    }

}
