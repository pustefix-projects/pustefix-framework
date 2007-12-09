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
package de.schlund.pfixcore.auth.conditions;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.Authentication;
import de.schlund.pfixcore.auth.Condition;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class Not implements Condition {

	private Condition condition;
	
	public Not() {
	}
	
	public Not(Condition condition) {
		this.condition=condition;
	}
	
	public void set(Condition condition) {
		this.condition=condition;
	}
	
	public boolean evaluate(Authentication auth) {
		return !condition.evaluate(auth);
	}
	
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("! ");
		sb.append(condition);
		return sb.toString();
	}
	
	public Element toXML(Document doc) {
		Element element=doc.createElement("not");
		if(condition!=null) element.appendChild(condition.toXML(doc));
		return element;
	}
	
}
