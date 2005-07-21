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
package de.schlund.pfixcore.editor.resources;

import java.util.HashSet;

import de.schlund.pfixcore.editor.EditorHelper;
import de.schlund.pfixxml.targets.AuxDependency;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */
class CurrentIncludeInfo {

    private AuxDependency currentInclude;
    private HashSet affectedProducts;

    public HashSet getAffectedProducts(EditorSessionStatus esess) throws Exception  {
        if(affectedProducts == null) {
            affectedProducts = EditorHelper.getAffectedProductsForInclude(currentInclude);
        }
        return affectedProducts;
    }

    public AuxDependency getCurrentInclude() {
        return currentInclude;
    }


    /**
     * @param dependency
     */
    public void setCurrentInclude(AuxDependency dependency) {
        if(dependency == null) {
            throw new IllegalArgumentException("A NP as dependency is not valid here!");
        }
        currentInclude = dependency;
        if(currentInclude != null) {
            resetAffectedProducts();    
        }
    }

    public void resetAffectedProducts() {
        affectedProducts = null;
    }

    public void resetAll() {
        currentInclude = null;
        resetAffectedProducts();
    }
}
