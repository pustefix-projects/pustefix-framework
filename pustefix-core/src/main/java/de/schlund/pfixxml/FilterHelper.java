package de.schlund.pfixxml;

public class FilterHelper {
    
    public static String getFilter(String targetKey) {
        String filter = null;
        int ind = targetKey.indexOf("::");
        if(ind > -1) {
            String variant = targetKey.substring(ind+2);
            ind = variant.indexOf(".");
            if(ind > -1) {
                variant = variant.substring(0, ind);
                System.out.println("VVVVVVVVVVVVV: "+variant);
                String[] s = variant.split("_");
                filter = "(|(lang=" + s[0] + ")(country=" + s[1] + "))";
                System.out.println("FFFFFFFFFFFFFF: "+filter);
            }
        }
        if(filter == null) filter = "(|(lang=de)(country=DE))";
        return filter;
    }

}
