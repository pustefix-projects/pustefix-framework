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
package de.schlund.pfixcore.auth.conditions;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class HasRole implements Condition {

    private String roleName;

    public HasRole() {

    }

    public HasRole(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean evaluate(Context context) {
        Authentication auth = context.getAuthentication();
        if (auth != null) {
            Role[] roles = auth.getRoles();
            if (roles != null) {
                for (Role role : roles) {
                    if (roleName.equals(role.getName()))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("role==");
        sb.append(roleName);
        return sb.toString();
    }

}
