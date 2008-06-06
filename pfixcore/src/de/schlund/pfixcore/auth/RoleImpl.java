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

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class RoleImpl implements Role {

    private String  name;
    private boolean initial;

    public RoleImpl(String name) {
        if (name == null)
            throw new IllegalArgumentException("Name must not be null!");
        this.name = name;
    }

    public RoleImpl(String name, boolean initial) {
        this(name);
        this.initial = initial;
    }

    public String getName() {
        return name;
    }

    public boolean isInitial() {
        return initial;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RoleImpl) {
            Role role = (Role) obj;
            if (role.getName().equals(name))
                return true;
        }
        return false;
    }

}
