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
 * SimpleRequestParam.java
 *
 *
 * Created: Wed May  8 02:55:19 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class SimpleRequestParam implements RequestParam {
    private String           value = null;
    private RequestParamType type  = RequestParamType.SIMPLE;
    private boolean          synthetic = false;

    public boolean isTrue() {
        if (value != null) {
            return (value.equals("true") || value.equals("1") || value.equals("yes"));
        }
        return false;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }
    
    public SimpleRequestParam(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return getValue();
    }

    public RequestParamType getType() {
        return type;
    }
    
}// SimpleRequestParam
