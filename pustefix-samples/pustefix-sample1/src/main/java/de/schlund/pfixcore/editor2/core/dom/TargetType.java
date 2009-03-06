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

package de.schlund.pfixcore.editor2.core.dom;

/**
 * Enum class to represent the different types a Target can be of.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.core.dom.Target#getType()
 */
public final class TargetType {
    /**
     * Target type for XML targets. XML targets are the XML document in further
     * processing.
     */
    public final static TargetType TARGET_XML = new TargetType(2, "XML");
    
    /**
     * Target type for XSL targets. XSL targets are the XSLT stylesheet in
     * further processing.
     */
    public final static TargetType TARGET_XSL = new TargetType(4, "XSL");
    
    /**
     * Target type for auxilliary targets. These targets are used to signal
     * a dependency on a file, which is not visible in the build chain.
     */
    public final static TargetType TARGET_AUX = new TargetType(8, "AUX");
    
    private int id;
    private String info;
    
    private TargetType(int id, String info) {
        this.id = id;
        this.info = info;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof TargetType) {
            TargetType tt = (TargetType) obj;
            return (this.id == tt.id);
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return ("TARGETTYPE: " + this.id + ", " + this.toString()).hashCode();
    }
    
    public String toString() {
        return "TARGET_" + this.info;
    }
}
