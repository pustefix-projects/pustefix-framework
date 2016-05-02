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
package de.schlund.pfixcore.auth;

import java.util.List;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public interface RoleProvider {

    /**
     * This method is called by the framework to add roles defined in the
     * context configuration.
     * 
     * @param role - Role to be added
     */
    public void addRole(Role role);

    /**
     * This method is called by the framework to check if a role with the
     * denoted name is defined. If an according role can be found the role is
     * returned, otherwise a RoleNotFoundException is thrown.
     * 
     * @param roleName - Role name
     * @return Found role
     * @throws RoleNotFoundException
     */
    public Role getRole(String roleName) throws RoleNotFoundException;

    /**
     * This method is called by the framework to check if roles are existing at
     * all and to find the roles, which have to be initially set. The method has
     * to return a list containing all available roles. If there are no roles,
     * it has to return an empty list.
     * 
     * @return List of available roles
     */
    public List<Role> getRoles();

}
