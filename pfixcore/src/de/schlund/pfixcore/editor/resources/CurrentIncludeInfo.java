/*
 * Created on 14.08.2003
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

    public HashSet getAfftectedProducts(EditorSessionStatus esess) throws Exception  {
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
