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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AuthConstraintImpl implements AuthConstraint {

    private Condition condition;
    private String authPage;
    private String id;

    public AuthConstraintImpl(String id) {
        this.id = id;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }

    public String getId() {
        return id;
    }

    public void setAuthPage(String authPage) {
        this.authPage = authPage;
    }

    public String getAuthPage() {
        return authPage;
    }

    public boolean isAuthorized(Context context) {
        return evaluate(context);
    }

    public boolean evaluate(Context context) {
        if (condition != null) {
            return condition.evaluate(context);
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("authconstraint");
        sb.append("{");
        sb.append("id=" + id);
        sb.append(",authpage=" + authPage);
        sb.append("}");
        sb.append("[");
        sb.append(condition);
        sb.append("]");
        return sb.toString();
    }

    public Element toXML(Document doc) {
        Element element = doc.createElement("authconstraint");
        if (condition != null) element.appendChild(condition.toXML(doc));
        return element;
    }

}
