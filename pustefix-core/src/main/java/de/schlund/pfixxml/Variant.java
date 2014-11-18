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
package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Holds variant value and provides methods for working
 * with variants, like getting the variant fallback array
 * or matching it against variant patterns.
 */
public class Variant {

    String variant;
    String[] variant_arr;
    String[] variantComponents;
        
    public Variant(String var) {
        variant = var;
        if (variant == null || variant.equals("")) {
            variant_arr = null;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(variant, ":");
            ArrayList<String> arrlist = new ArrayList<String>();
            ArrayList<String> compList = new ArrayList<String>();
            String          fallback  = "";
            while (tokenizer.hasMoreElements()) {
                String tok = tokenizer.nextToken();
                if (!fallback.equals("")) {
                    fallback += ":";
                }
                fallback += tok;
                arrlist.add(0,fallback);
                compList.add(tok);
            }
            variant_arr = arrlist.toArray(new String[]{});
            variantComponents = compList.toArray(new String[compList.size()]);
        }
    }

    /**
     * Get the full variant value, including all subvariants, e.g. "foo:bar:baz".
     * 
     */
    public String getVariantId() {
        return variant;
    }

    /**
     * Get all sub-variants with highest precedence first.
     * E.g. the variant "foo:bar:baz" will return:
     * 
     * <ul>
     *   <li>foo:bar:baz</li>
     *   <li>foo:bar</li>
     *   <li>foo</li>
     * </ul>
     *
     */
    public String[] getVariantFallbackArray() {
        return variant_arr;
    }
    
    /**
     * Checks if variant matches a variant pattern.
     * Similar to ant-style patterns the operators "*" and "**" are supported
     * to match a single variant part or a sequence of variant parts.    
     * E.g. the variant "foo:bar:baz" will be matched by the following patterns:
     *   
     * <ul>
     *   <li>foo:bar:baz</li>
     *   <li>foo:bar</li>
     *   <li>foo</li>
     *   <li>*:bar:baz</li>
     *   <li>foo:*:baz</li>
     *   <li>**:baz</li>
     *   <li>**:bar</li>
     * </ul>
     * 
     */
    public boolean matches(String variantPattern) {
        
        String[] patternComponents = variantPattern.split(":");
        boolean inSkip = false;
        int patternIndex = 0;
        int variantIndex = 0;
        while(patternIndex < patternComponents.length && variantIndex < variantComponents.length) { 
            if(patternComponents[patternIndex].equals("*")) {
                patternIndex++;
                inSkip = false;
            } else if(patternComponents[patternIndex].equals("**")) {
                patternIndex++;
                inSkip = true;
            } else if(patternComponents[patternIndex].equals(variantComponents[variantIndex])) {
                patternIndex++;
                inSkip = false;
            } else if(!inSkip) {
                return false;
            }
            variantIndex++;
        }
        if(patternIndex < patternComponents.length) {
            return false;
        }
        return true;
    }

}
