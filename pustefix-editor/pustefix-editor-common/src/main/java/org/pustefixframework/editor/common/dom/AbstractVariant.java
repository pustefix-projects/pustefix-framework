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

package org.pustefixframework.editor.common.dom;

/**
 * Provides functionality common to all classes implementing Variant
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class AbstractVariant implements Variant {
    private String name;
    
    /**
     * Creates a Variant object with the specified name
     * 
     * @param name Name of the variant to create
     */
    public AbstractVariant(String name) {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see de.schlund.pfixcore.editor2.core.dom.Variant#getName()
     */
    public String getName() {
        return this.name;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Variant#isChildOf(de.schlund.pfixcore.editor2.core.dom.Variant)
     */
    public boolean isChildOf(Variant variant) {
        return this.getName().startsWith(variant.getName() + ":")
                && !this.equals(variant);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.schlund.pfixcore.editor2.core.dom.Variant#isParentOf(de.schlund.pfixcore.editor2.core.dom.Variant)
     */
    public boolean isParentOf(Variant variant) {
        return variant.getName().startsWith(this.getName() + ":")
                && !this.equals(variant);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variant) {
            Variant variant = (Variant) obj;
            return this.getName().equals(variant.getName());
        } else {
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ("VARIANT: " + this.toString()).hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getName();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Variant variant) {
        return this.getName().compareTo(variant.getName());
    }
}
