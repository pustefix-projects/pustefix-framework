package de.schlund.pfixxml;

public class FilterHelper {
    
    public static String getFilter(String tenant, String language) {
        StringBuilder sb = new StringBuilder();
        int condNo = 0;
        if(language != null && language.length() > 0) {
            sb.append("(lang=" + language + ")");
            condNo++;
            int ind = language.indexOf('_');
            if(ind > -1) {
                String langOnly = language.substring(0, ind);
                sb.append("(lang=" + langOnly +")");
                condNo++;
            }
        }
        if(tenant != null && tenant.length() > 0) {
            sb.append("(tenant=" + tenant + ")");
            condNo++;
        }
        if(condNo > 0) {
            if(condNo == 1) {
                return sb.toString();
            } else {
                sb.insert(0, "(|");
                sb.append(")");
                return sb.toString();
            }
        }
        return null;
    }

}
