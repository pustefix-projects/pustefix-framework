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
package de.schlund.pfixxml;

import com.icl.saxon.Context;
import com.icl.saxon.expr.StaticContext;
import com.icl.saxon.om.NodeInfo;

/**
 * XSLT extension function which returns the current context of a XSL
 * transformation (systemId and line number of the current context and
 * and stylesheet nodes).
 *
 */
public class LocationInfo {

    public static String getLocation(Context context) {

        StringBuilder sb = new StringBuilder();

        StaticContext staticContext = context.getStaticContext();
        if(staticContext != null) {
            String systemId = staticContext.getSystemId();
            if(systemId != null && systemId.length() > 0) {
                sb.append(systemId).append(":").append(staticContext.getLineNumber());
            }
        }

        NodeInfo current = context.getCurrentNodeInfo();
        if(current != null) {
            String systemId = current.getSystemId();
            if(systemId != null && systemId.length() > 0) {
                if(sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(systemId).append(":").append(current.getLineNumber());
            }
        }

        return sb.toString();
    }

}
