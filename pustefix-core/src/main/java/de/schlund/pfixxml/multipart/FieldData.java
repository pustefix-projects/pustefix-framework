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

package de.schlund.pfixxml.multipart;
import de.schlund.pfixxml.RequestParamType;

/**
 *
 *
 */   

public class FieldData extends PartData {

    private   String characterset     = null;
    private   String transferEncoding = null;

    /**
     * Constructor for FieldData.
     */
    public FieldData() {
        setType(RequestParamType.FIELDDATA);
    }

    /**
     * Gets the characterset.
     * @return Returns a String
     */
    public String getCharacterset() {
        return characterset;
    }

    /**
     * Sets the characterset.
     * @param characterset The characterset to set
     */
    public void setCharacterset(String characterset) {
        this.characterset = characterset;
    }

    /**
     * Gets the transferEncoding.
     * @return Returns a String
     */
    public String getTransferEncoding() {
        return transferEncoding;
    }

    /**
     * Sets the transferEncoding.
     * @param transferEncoding The transferEncoding to set
     */
    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }
}
