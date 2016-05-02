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
package de.schlund.pfixcore.example.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.auth.RoleImpl;
import de.schlund.pfixcore.auth.RoleNotFoundException;
import de.schlund.pfixcore.auth.RoleProvider;

public class BankRoleProvider implements RoleProvider {

    private Map<String, Role> roles;

    public BankRoleProvider() {
        roles = new HashMap<String, Role>();
        Role role = new RoleImpl("TEST", false);
        roles.put(role.getName(), role);
        role = new RoleImpl("ADMIN", false);
        roles.put(role.getName(), role);
    }

    public void addRole(Role role) {
        roles.put(role.getName(),role);
    }
    
    public Role getRole(String roleName) throws RoleNotFoundException {
        Role role = roles.get(roleName);
        if(role == null) throw new RoleNotFoundException(roleName);
        return role;
    }

    public List<Role> getRoles() {
        return new ArrayList<Role>(roles.values());
    }
    
}
