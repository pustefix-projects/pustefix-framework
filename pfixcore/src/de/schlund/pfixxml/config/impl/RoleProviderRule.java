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

package de.schlund.pfixxml.config.impl;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

import de.schlund.pfixcore.auth.RoleProvider;

/**
 * @author mleidig@schlund.de
 */
public class RoleProviderRule extends CheckedRule {
    private ContextXMLServletConfigImpl config;

    public RoleProviderRule(ContextXMLServletConfigImpl config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        String className = attributes.getValue("class");
        if (className == null || className.trim().equals("")) throw new Exception("Element 'roleprovider' requires 'class' attribute.");
        try {
            Class<?> clazz = Class.forName(className);
            if (!RoleProvider.class.isAssignableFrom(clazz))
                throw new Exception("Class '" + className + "' doesn't implement the RoleProvider interface.");
            RoleProvider roleProvider = (RoleProvider) clazz.newInstance();
            config.getContextConfig().addCustomRoleProvider(roleProvider);
            getDigester().push(roleProvider);
        } catch (ClassNotFoundException x) {
            throw new Exception("RoleProvider class not found: " + className);
        } catch (InstantiationException x) {
            throw new Exception("RoleProvider class can't be instantiated: " + className, x);
        } catch (IllegalAccessException x) {
            throw new Exception("RoleProvider class can't be instantiated: " + className, x);
        }
    }

    public void end(String namespace, String name) throws Exception {
        getDigester().pop();
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("class", true);
        return atts;
    }

}
