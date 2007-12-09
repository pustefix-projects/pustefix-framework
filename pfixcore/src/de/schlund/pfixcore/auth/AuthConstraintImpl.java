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

/**
 * 
 * @author mleidig
 *
 */
public class AuthConstraintImpl implements AuthConstraint {

	private Condition condition;
	private String authPage;
	
	public AuthConstraintImpl() {
	}
	
	public AuthConstraintImpl(Condition condition) {
		this.condition=condition;
	}
	
	public void setCondition(Condition condition) {
		this.condition=condition;
	}
	
	public Condition getCondition() {
		return condition;
	}
	
	public void setAuthPage(String authPage) {
		this.authPage=authPage;
	}
	
	public String getAuthPage() {
		return authPage;
	}
	
	public boolean isAuthorized(Authentication auth) {
		if(condition!=null) {
			return condition.evaluate(auth);
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append(getClass().getName());
		sb.append(" ");
		sb.append("{");
		sb.append("condition: ");
		sb.append(condition);
		sb.append("}");
		return sb.toString();
	}
	
	public Element toXML(Document doc) {
		Element element=doc.createElement("authconstraint");
		if(condition!=null) element.appendChild(condition.toXML(doc));
		return element;
	}
	
}
