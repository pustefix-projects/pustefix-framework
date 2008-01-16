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

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.AuthConstraintImpl;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.auth.conditions.And;
import de.schlund.pfixcore.auth.conditions.ConditionGroup;
import de.schlund.pfixcore.auth.conditions.HasRole;
import de.schlund.pfixcore.auth.conditions.Not;
import de.schlund.pfixcore.auth.conditions.Or;

/**
 * @author mleidig@schlund.de
 */
public class ConditionRule extends CheckedRule {

    ContextXMLServletConfigImpl config;

    public ConditionRule(ContextXMLServletConfigImpl config) {
        this.config = config;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        check(namespace, name, attributes);
        Condition condition = null;
        if (name.equals("or")) {
            condition = new Or();
        } else if (name.equals("and")) {
            condition = new And();
        } else if (name.equals("not")) {
            condition = new Not();
        } else if (name.equals("hasrole")) {
            String roleName = attributes.getValue("name");
            condition = new HasRole(roleName);
            Role role = config.getContextConfig().getRole(roleName);
            if (role == null) throw new Exception("Condition hasrole references unknown role: " + roleName);
        } else throw new Exception("Unsupported condition: " + name);
        Object obj = getDigester().peek();
        if (obj instanceof AuthConstraint) {
            ((AuthConstraintImpl) obj).setCondition(condition);
        } else if (obj instanceof ConditionGroup) {
            ((ConditionGroup) obj).add(condition);
        } else if (obj instanceof Not) {
            ((Not) obj).set(condition);
        } else throw new Exception("Illegal object: " + obj.getClass().getName());
        getDigester().push(condition);
    }

    public void end(String namespace, String name) throws Exception {
        getDigester().pop();
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        atts.put("name", false);
        return atts;
    }

}
