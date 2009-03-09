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

package de.schlund.pfixxml.multipart;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.RequestParamType;

/**
 *
 *
 */

public abstract class PartData implements RequestParam {

    protected String primaryType = null;
    protected String subType     = null;
    protected String fieldname   = null;
    protected String value       = null;
    protected RequestParamType type;
    private   boolean synthetic = false;

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
    
    /**
     * Constructor for PartData.
     */
    public PartData() {
    }

    /**
     * Gets the fieldname.
     * @return Returns a String
     */
    public String getFieldname() {
        return fieldname;
    }

    /**
     * Sets the fieldname.
     * @param fieldname The fieldname to set
     */
    protected void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    /**
     * Gets the type.
     * @return Returns a int
     */
    public RequestParamType getType() {
        return type;
    }

    protected void setType(RequestParamType type) {
        this.type = type;
    }
    /**
     * Gets the baseType.
     * @return Returns a String
     */
    public String getPrimaryType() {
        return primaryType;
    }

    /**
     * Sets the baseType.
     * @param baseType The baseType to set
     */
    protected void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    /**
     * Gets the subType.
     * @return Returns a String
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets the subType.
     * @param subType The subType to set
     */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * Gets the value.
     * @return Returns a String
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
