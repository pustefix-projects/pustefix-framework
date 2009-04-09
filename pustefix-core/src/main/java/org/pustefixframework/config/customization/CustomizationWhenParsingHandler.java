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
package org.pustefixframework.config.customization;

import java.util.Collection;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;

import com.marsching.flexiparse.objecttree.DisableParsingFlag;
import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handles the &lt;when&gt; elements within a &lt;choose&gt; element. Adds a 
 * {@link CustomizationIgnoreBranchFlag} object to current object tree element
 * if the branch below the current &lt;when&gt; element should not be parsed.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class CustomizationWhenParsingHandler extends CustomizationAwareParsingHandler {
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        ObjectTreeElement current = context.getObjectTreeElement();
        Element whenElement = (Element) context.getNode();
        
        // Look for an active branch preceding the current branch
        for (ObjectTreeElement child : current.getParent().getChildren()) {
            if (child.equals(current)) {
                break;
            }
            if (child.getObjectsOfType(DisableParsingFlag.class).isEmpty()) {
                current.addObject(new DisableParsingFlag());
                return;
            }
        }
        
        // Evaluate expression of matched element
        String expression = whenElement.getAttribute("test");
        if (expression == null) {
            throw new ParserException("Mandatory attribute \"test\" missing on element <when>");
        }
        
        Collection<CustomizationInfo> infoCollection = current.getObjectsOfTypeFromTopTree(CustomizationInfo.class);
        if (infoCollection.isEmpty()) {
            throw new ParserException("Could not find instance of CustomizationInfo");
        }
        CustomizationInfo info = infoCollection.iterator().next();
        
        boolean test;
        try {
            test = (Boolean) info.evaluateXPathExpression(expression, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            throw new ParserException("Error while evaluating XPath expression \"" + expression + "\": " + e.getMessage(), e);
        } 
        
        if (!test) {
            current.addObject(new DisableParsingFlag());
        }
    }

}
