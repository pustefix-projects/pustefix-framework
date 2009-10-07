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

import java.util.Iterator;

import org.pustefixframework.config.contextxmlservice.parser.internal.AuthConstraintRef;
import org.pustefixframework.config.contextxmlservice.parser.internal.PustefixContextXMLRequestHandlerConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

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
 * 
 * @author mleidig
 *
 */
public class ConditionParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {

        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, null, new String[] {"name", "class", "ref", "id"});
        
        PustefixContextXMLRequestHandlerConfigImpl config = ParsingUtils.getSingleTopObject(PustefixContextXMLRequestHandlerConfigImpl.class, context);
        
        Iterator<Condition> parentConditions = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(Condition.class).iterator();
        Condition parentCondition = null;
        if(parentConditions.hasNext()) parentCondition = parentConditions.next();
        boolean inCondition = ( parentCondition != null);
        boolean inPageRequest = isInPageRequest(element);
        
        String name = element.getNodeName();
        Condition condition = null;
        if (name.equals("or")) {
            condition = new Or();
        } else if (name.equals("and")) {
            condition = new And();
        } else if (name.equals("not")) {
            condition = new Not();
        } else if (name.equals("hasrole")) {
            String roleName = element.getAttribute("name").trim();
            if(roleName.equals("")) throw new ParserException("Element 'hasrole' requires 'name' attribute value.");
            condition = new HasRole(roleName);
        } else if (name.equals("condition")) {
            if (!inCondition) {
                String id = element.getAttribute("id").trim();
                condition = createCondition(element);
                config.getContextConfig().addCondition(id, condition);
            } else {
                String ref = element.getAttribute("ref").trim();
                if (ref != null) {
                    condition = config.getContextConfig().getCondition(ref);
                    if (condition == null) throw new ParserException("Condition reference not found: " + ref);
                } else {
                    condition = createCondition(element);
                }
            }
        } else if (name.equals("authconstraint")) {
            String ref = element.getAttribute("ref").trim();
            if(ref.equals("")) throw new ParserException("Nested authconstraint requires 'ref' attribute.");
            if(inPageRequest) {
                AuthConstraint constraint = config.getContextConfig().getAuthConstraint(ref);
                if(constraint == null) throw new ParserException("Referenced authconstraint not found: "+ref);
                condition = constraint.getCondition();
            } else {
                condition = new AuthConstraintRef(ref);
            }
        } else throw new ParserException("Unsupported condition: " + name);
      
        if(inCondition) {
            if (parentCondition instanceof AuthConstraint) {
                ((AuthConstraintImpl) parentCondition).setCondition(condition);
            } else if (parentCondition instanceof ConditionGroup) {
                ((ConditionGroup) parentCondition).add(condition);
            } else if (parentCondition instanceof Not) {
                ((Not) parentCondition).set(condition);
            } else throw new ParserException("Illegal object: " + parentCondition.getClass().getName());
        }
        
        PropertyParsingUtils.setProperties(condition, element);
        
        context.getObjectTreeElement().addObject(condition);
    }
    
    private Condition createCondition(Element element) throws ParserException {
        String className = element.getAttribute("class").trim();
        if (className.equals("")) throw new ParserException("Condition needs class attribute.");
        try {
            Class<?> clazz = Class.forName(className);
            return (Condition) clazz.newInstance();
        } catch (ClassNotFoundException x) {
            throw new ParserException("Condition class not found: " + className);
        } catch (Exception x) {
            throw new ParserException("Condition class can't be instantiated: " + className, x);
        }
    }
    
    private boolean isInPageRequest(Element element) {
        Node parent = element.getParentNode();
        if(parent!=null && parent.getNodeType()==Node.ELEMENT_NODE) {
            Element parentElem = (Element)parent;
            if(parentElem.getNodeName().equals("pagerequest")) return true;
            else return isInPageRequest(parentElem);
        }
        return false;
    }

}
