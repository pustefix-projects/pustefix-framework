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

import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageRequestConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.auth.AuthConstraintImpl;


public class AuthConstraintParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {

        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"id", "default", "ref", "authpage"});

        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);

        boolean topLevel = false;
        Element parentElement = (Element)element.getParentNode();
        if(parentElement.getLocalName().equals("context-xml-service-config")) {
            topLevel = true;
        }

        AuthConstraintImpl constraint = null;
        if (topLevel) {
            String constraintId = element.getAttribute("id").trim();
            if (constraintId.equals("")) throw new ParserException("Top-level element 'authconstraint' requires attribute 'id'");
            constraint = new AuthConstraintImpl(constraintId);
            context.getObjectTreeElement().addObject(constraint);
            config.getContextConfig().addAuthConstraint(constraintId, constraint);
            String defStr = element.getAttribute("default").trim();
            if (!defStr.equals("")) {
                boolean def = Boolean.valueOf(defStr);
                if (def) config.getContextConfig().setDefaultAuthConstraint(constraint);
            }
        } else {
            if (!config.getContextConfig().authConstraintRefsResolved()) {
                config.getContextConfig().resolveAuthConstraintRefs();
            }
            String constraintRef = element.getAttribute("ref").trim();
            if (!constraintRef.equals("")) {
                constraint = (AuthConstraintImpl) config.getContextConfig().getAuthConstraint(constraintRef);
                if (constraint == null) throw new ParserException("Referenced 'authconstraint' element can't be found: " + constraintRef);
            } else {
                constraint = new AuthConstraintImpl("anonymous");
            }
            context.getObjectTreeElement().addObject(constraint);
            PageRequestConfigImpl pageConfig = ParsingUtils.getSingleTopObject(PageRequestConfigImpl.class, context);
            pageConfig.setAuthConstraint(constraint);
        }
        String authPage = element.getAttribute("authpage").trim();
        if (!authPage.equals("")) constraint.setDefaultAuthPage(authPage);
    }

}
