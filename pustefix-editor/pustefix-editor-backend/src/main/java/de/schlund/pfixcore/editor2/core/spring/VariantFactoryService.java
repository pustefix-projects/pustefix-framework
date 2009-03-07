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

package de.schlund.pfixcore.editor2.core.spring;

import org.pustefixframework.editor.common.dom.Variant;

/**
 * Service generating Variant objects
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface VariantFactoryService {
    /**
     * Creates a variant with the specified name.
     * Components of the variant name are seperated by colons (e.g. foo:bar:baz).
     * 
     * @param variantName  name of the variant 
     * @return Variant object representing the specified variant
     */
    Variant getVariant(String variantName);
}
