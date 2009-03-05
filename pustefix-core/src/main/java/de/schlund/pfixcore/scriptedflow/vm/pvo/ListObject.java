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

package de.schlund.pfixcore.scriptedflow.vm.pvo;

import java.util.ArrayList;
import java.util.List;

import de.schlund.pfixcore.scriptedflow.vm.XPathResolver;

/**
 * Virtual parameter encapsulating one or more parameters and
 * conactenating there values at runtime.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ListObject implements ParamValueObject {
    private List<ParamValueObject> objects = new ArrayList<ParamValueObject>();
    
    public void addObject(ParamValueObject obj) {
        objects.add(obj);
    }

    public String resolve(XPathResolver resolver) {
        StringBuffer buffer = new StringBuffer();
        for (ParamValueObject pvo : objects) {
            buffer.append(pvo.resolve(resolver));
        }
        return buffer.toString();
    }
}
