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

package org.pustefixframework.config.customization;

import com.marsching.flexiparse.objecttree.DisableParsingFlag;
import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Handles the &lt;otherwise&gt; element within a &lt;choose&gt; element. Adds a 
 * {@link CustomizationIgnoreBranchFlag} object to current object tree element
 * if the branch below the current &lt;otherwise&gt; element should not be parsed.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class CustomizationOtherwiseParsingHandler extends CustomizationAwareParsingHandler {
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        ObjectTreeElement current = context.getObjectTreeElement();
        
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
    }

}
