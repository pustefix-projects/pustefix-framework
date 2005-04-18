package de.schlund.pfixxml;

import java.util.StringTokenizer;
import java.util.ArrayList;

/**
 * Describe class Variant here.
 *
 *
 * Created: Sun Apr 10 17:41:28 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class Variant {

    String variant;
    String[] variant_arr;
        
    public Variant(String var) {
        variant = var;
        if (variant == null || variant.equals("")) {
            variant_arr = null;
        } else {
            StringTokenizer tokenizer = new StringTokenizer(variant, ":");
            ArrayList       arrlist   = new ArrayList();
            String          fallback  = "";
            while (tokenizer.hasMoreElements()) {
                String tok = tokenizer.nextToken();
                if (!fallback.equals("")) {
                    fallback += ":";
                }
                fallback += tok;
                arrlist.add(0,fallback);
            }
            variant_arr = (String[]) arrlist.toArray(new String[]{});
        }
    }

    public String getVariantId() {
        return variant;
    }

    public String[] getVariantFallbackArray() {
        return variant_arr;
    }
}
