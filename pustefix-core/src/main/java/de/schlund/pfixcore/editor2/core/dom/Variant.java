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
 * Identifies a variant of a page. Variants are used to programatically
 * influence pageflows and rendering of pages.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface Variant extends Comparable<Variant> {
    /**
     * Returns the full name of this variant. Parts of the name are seperated by
     * a colon.
     * 
     * @return Name of this variant
     */
    String getName();

    /**
     * Returns true if this variant is a parent of the specified variant, false
     * otherwise. 
     * Example: foo and foo:bar are parents of foo:bar:baz.
     * If this variant and the specified variant are equal, false is returned.
     * 
     * @param variant
     *            Variant object to check
     * @return boolean value indicating wheter this is a parent variant of the
     *         specified variant
     */
    boolean isParentOf(Variant variant);

    /**
     * Returns true if this variant is a child of the specified variant, false
     * otherwise. 
     * Example: foo:bar:bum and foo:bar:baz are childs of foo:bar and foo. 
     * If this variant and the specified variant are equal, false is returned.
     * 
     * @param variant
     *         Variant object to check
     * @return boolean value indicating wheter this is a child variant of the
     *         specified variant
     */
    boolean isChildOf(Variant variant);
}
