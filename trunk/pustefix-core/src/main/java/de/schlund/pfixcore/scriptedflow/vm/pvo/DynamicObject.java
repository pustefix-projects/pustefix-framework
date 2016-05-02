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

package de.schlund.pfixcore.scriptedflow.vm.pvo;

import de.schlund.pfixcore.scriptedflow.vm.XPathResolver;

/**
 * Parameter calculating its value at runtime using a supplied
 * XPath expression  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class DynamicObject implements ParamValueObject {
    private String expression;

    public DynamicObject(String expression) {
        this.expression = expression;
    }

    public String resolve(XPathResolver resolver) {
        return resolver.evalXPathString(expression);
    }
}
