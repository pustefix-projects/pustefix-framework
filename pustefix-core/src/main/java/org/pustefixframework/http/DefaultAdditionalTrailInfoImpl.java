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
package org.pustefixframework.http;

import java.util.LinkedHashMap;


import de.schlund.pfixxml.PfixServletRequest;

public class DefaultAdditionalTrailInfoImpl implements AdditionalTrailInfo {

    public LinkedHashMap<String,Object> getData(PfixServletRequest preq) {
        Object pptime    = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.PREPROCTIME);
        Object doctime   = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.GETDOMTIME);
        Object trafotime = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.TRAFOTIME);
        Object rextime  = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.RENDEREXTTIME);
        Object functime = preq.getRequest().getAttribute(AbstractPustefixXMLRequestHandler.EXTFUNCTIME);
        
        LinkedHashMap<String,Object> retval = new LinkedHashMap<String,Object>();
        retval.put("GET_DOM", ""+doctime);
        retval.put("HDL_DOC", ""+trafotime);
        retval.put("REX_DOC", ""+rextime);
        retval.put("PRE_PROC", ""+pptime);
        if(functime != null) {
            retval.put("EXT_FUNC", ""+functime);
        }
        return retval;
    }
    
    public void reset() {
        // Nothing to do in default impl
    }
}
