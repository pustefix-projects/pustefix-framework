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

import com.marsching.flexiparse.objecttree.DisableParsingFlag;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Abstract base type for parsing handlers that want to be aware of 
 * customization blocks. This handler will take care of not parsing
 * data that is in an inactive branch. A branch is marked as inactive
 * by adding an object of the type {@link CustomizationIgnoreBranchFlag}
 * to the corresponding object tree element.
 * Types derived from this type should implement the 
 * {@link #handleNodeIfActive(HandlerContext)} instead of the 
 * {@link #handleNode(HandlerContext)} method.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class CustomizationAwareParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        // Should be handled by the parser, this is just for safety and 
        // compatibility with old code
        if (context.getObjectTreeElement().getObjectsOfTypeFromTopTree(DisableParsingFlag.class).isEmpty()) {
            handleNodeIfActive(context);
        }
    }
    
    /**
     * Called by the handler if and only if parsing is active for the current
     * branch. Should be implemented by subtypes to perform the actual
     * parsing.
     * 
     * @param context provides context information for the current node
     * @throws ParserException if any error occurs during the parsing
     */
    protected abstract void handleNodeIfActive(HandlerContext context) throws ParserException;
    
}
