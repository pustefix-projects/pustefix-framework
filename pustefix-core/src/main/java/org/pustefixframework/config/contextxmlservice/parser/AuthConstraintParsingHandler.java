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
package org.pustefixframework.config.contextxmlservice.parser;

import org.pustefixframework.config.contextxmlservice.parser.internal.AuthConstraintRef;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.auth.AuthConstraint;
import de.schlund.pfixcore.auth.AuthConstraintImpl;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class AuthConstraintParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {

        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"id", "default", "ref", "authpage"});
        
        AuthConstraint constraint = null;
     
        String constraintRef = element.getAttribute("ref").trim();
        String constraintId = element.getAttribute("id").trim();
        if(constraintRef.length()>0) {
        	constraint = new AuthConstraintRef(constraintRef);
        } else {
        	if(constraintId.length()>0) {
        		constraint = new AuthConstraintImpl(constraintId);
        	} else {
        		constraint = new AuthConstraintImpl("anonymous");
        	}
        	String defStr = element.getAttribute("default").trim();
            if(defStr.length()>0) {
                boolean def = Boolean.valueOf(defStr);
                ((AuthConstraintImpl)constraint).setDefault(def);
            }
            String authPage = element.getAttribute("authpage").trim();
            if(authPage.length()>0) {
            	((AuthConstraintImpl)constraint).setAuthPage(authPage);
            }
        }
        
        context.getObjectTreeElement().addObject(constraint);
    }

}
