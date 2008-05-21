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

import org.apache.log4j.Logger;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AuthenticationImpl implements Authentication {

    private final static Logger LOG = Logger.getLogger(AuthenticationImpl.class);

    private SortedMap<String, Role> roles;
    private RoleProvider roleProvider;

    public AuthenticationImpl(RoleProvider roleProvider) {
        this.roleProvider = roleProvider;
        roles = new TreeMap<String, Role>();
    }

    public synchronized Role[] getRoles() {
        Role[] rolesCopy = new Role[roles.size()];
        roles.values().toArray(rolesCopy);
        return rolesCopy;
    }

    public synchronized boolean hasRole(String roleName) {
        boolean hasRole = roles.containsKey(roleName);
        if (!hasRole) {
            try {
                roleProvider.getRole(roleName);
            } catch (RoleNotFoundException x) {
                LOG.warn("ROLE_NOT_FOUND|" + roleName);
            }
        }
        return hasRole;
    }

    public synchronized boolean addRole(String roleName) throws RoleNotFoundException {
        if (!roles.containsKey(roleName)) {
            Role role = roleProvider.getRole(roleName);
            if(role == null) throw new RoleNotFoundException(roleName);
            roles.put(roleName, role);
            return true;
        }
        return false;
    }

    public synchronized boolean revokeRole(String roleName) {
        return roles.remove(roleName) != null;
    }

}
