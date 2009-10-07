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
package org.pustefixframework.config.contextxmlservice.parser.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.Condition;
import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * Helper class for late resolving of authconstraint references.
 * 
 * @author mleidig@schlund.de
 *
 */
public class AuthConstraintRef implements AuthConstraint {
    
    private String ref;
   
    public AuthConstraintRef(String ref) {
        this.ref = ref;
    }
    
    public String getId() {
    	throw new RuntimeException("Method not implemented");
    }
    
    public String getRef() {
        return ref;
    }
    
    public boolean isDefault() {
    	throw new RuntimeException("Method not implemented");
    }
    
    public boolean evaluate(Context context) {
        throw new RuntimeException("Method not implemented");
    }
    
    public boolean isAuthorized(Context context) {
    	throw new RuntimeException("Method not implemented");
    }
    
    public void setCondition(Condition condition) {
    	throw new RuntimeException("Method not implemented");	
    }
    
    public Condition getCondition() {
    	throw new RuntimeException("Method not implemented");
    }
    
    public String getAuthPage() {
    	throw new RuntimeException("Method not implemented");
    }
    
    public Element toXML(Document doc) {
        throw new RuntimeException("Method not implemented");
    }
    
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append("authconstraintref");
        sb.append("{ref="+ref+"}");
        return sb.toString();
    }
    
}
