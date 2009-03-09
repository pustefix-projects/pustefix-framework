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
 *
 */

package de.schlund.pfixxml;

/**
 * RequestParamType.java
 *
 *
 * Created: Tue May 14 20:47:08 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class RequestParamType {
    private String tag;

    public final static RequestParamType SIMPLE    = new RequestParamType("SIMPLE");
    public final static RequestParamType FIELDDATA = new RequestParamType("MULTIPART/FIELD");
    public final static RequestParamType FILEDATA  = new RequestParamType("MULTIPART/FILE");
    
    private RequestParamType() {}
    
    private RequestParamType(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "[RequestParamType " + tag + "]";
    }
}// RequestParamType
