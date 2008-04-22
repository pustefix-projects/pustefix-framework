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
package de.schlund.pfixcore.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class RoleProviderImpl implements RoleProvider {
    
    private List<Role> roles;
    private Map<String,Role> roleMap;
   
    public RoleProviderImpl() {
        roles = new ArrayList<Role>();
        roleMap = new HashMap<String,Role>();
    }
    
    public void addRole(Role role) {
        if(roleMap.containsKey(role.getName())) 
            throw new IllegalArgumentException("Duplicate role: "+role.getName());
        roles.add(role);
        roleMap.put(role.getName(), role);
    }
    
    public Role getRole(String roleName) throws RoleNotFoundException {
        return roleMap.get(roleName);
    }
    
    public List<Role> getRoles() {
        return roles;
    }
    
    public void addRoles(RoleProvider roleProvider) {
        List<Role> roleList = roleProvider.getRoles();
        if (roleList != null) {
            for (Role role : roleList) addRole(role);
        }
    }
    
    public void setReadOnly() {
        roles = Collections.unmodifiableList(roles);
        roleMap = Collections.unmodifiableMap(roleMap);
    }

}
