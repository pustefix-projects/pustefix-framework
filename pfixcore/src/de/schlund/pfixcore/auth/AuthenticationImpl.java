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

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author mleidig@schlund.de
 */
public class AuthenticationImpl implements Authentication {

    private boolean authenticated;
    private SortedMap<String,Role> roles;
    
    public AuthenticationImpl(String[] roleNames, boolean authenticated) {
        roles=new TreeMap<String,Role>();
        for(String roleName:roleNames) {
            roles.put(roleName,new RoleImpl(roleName));
        }
        this.authenticated=authenticated;
    }
    
    public synchronized boolean isAuthenticated() {
        return authenticated;
    }
    
    public synchronized void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public Role[] getRoles() {
        Role[] rolesCopy=new Role[roles.size()];
        roles.values().toArray(rolesCopy);
        return rolesCopy;
    }
    
    public boolean hasRole(String roleName) {
        return roles.containsKey(roleName);
    }
    
}
