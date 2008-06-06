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

import de.schlund.pfixcore.auth.AuthConstraintImpl;

/**
 * @author mleidig@schlund.de
 */
public class AuthConstraintRule extends CheckedRule {

    private ContextXMLServletConfigImpl config;
    private boolean                     topLevel;

    public AuthConstraintRule(ContextXMLServletConfigImpl config, boolean topLevel) {
        this.config = config;
        this.topLevel = topLevel;
    }

    public void begin(String namespace, String name, Attributes attributes) throws Exception {
        try {
            check(namespace, name, attributes);
            AuthConstraintImpl constraint = null;
            if (topLevel) {
                String constraintId = attributes.getValue("id");
                if (constraintId == null) throw new Exception("Top-level element 'authconstraint' requires attribute 'id'");
                constraint = new AuthConstraintImpl();
                getDigester().push(constraint);
                config.getContextConfig().addAuthConstraint(constraintId, constraint);
                String defStr = attributes.getValue("default");
                if (defStr != null) {
                    boolean def = Boolean.valueOf(defStr);
                    if (def) config.getContextConfig().setDefaultAuthConstraint(constraint);
                }
            } else {
                Object obj = getDigester().peek();
                String constraintRef = attributes.getValue("ref");
                if (constraintRef != null) {
                    constraint = (AuthConstraintImpl) config.getContextConfig().getAuthConstraint(constraintRef);
                    if (constraint == null) throw new Exception("Referenced 'authconstraint' element can't be found: " + constraintRef);
                } else {
                    constraint = new AuthConstraintImpl();
                }
                getDigester().push(constraint);
                if (obj instanceof PageRequestConfigImpl) {
                    ((PageRequestConfigImpl) obj).setAuthConstraint(constraint);
                } else
                    throw new Exception("Element 'authconstraint' isn't 'pagerequest' child: " + obj.getClass().getName());
            }
            String authPage = attributes.getValue("authpage");
            if (authPage != null) constraint.setAuthPage(authPage);
        } catch (Exception x) {
            x.printStackTrace();
            throw x;
        }
    }

    public void end(String namespace, String name) throws Exception {
        getDigester().pop();
    }

    protected Map<String, Boolean> wantsAttributes() {
        HashMap<String, Boolean> atts = new HashMap<String, Boolean>();
        if (topLevel) {
            atts.put("id", true);
            atts.put("default", false);
        } else
            atts.put("ref", false);
        atts.put("authpage", false);
        return atts;
    }

}
