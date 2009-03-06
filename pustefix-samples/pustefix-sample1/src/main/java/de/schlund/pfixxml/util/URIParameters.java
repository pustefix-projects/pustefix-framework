/*
 * Created on 19.02.2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixxml.util;

import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author mleidig@schlund.de
 * 
 */
public class URIParameters {

    private Map<String, String[]> params;

    public URIParameters(String queryStr, String encoding) throws Exception {
        params = parse(queryStr, encoding);
    }

    public static Map<String, String[]> parse(String queryStr, String encoding) throws Exception {
        HashMap<String, String[]> map = new HashMap<String, String[]>();
        if (queryStr != null) {
            StringTokenizer st = new StringTokenizer(queryStr, "&");
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                int ind = pair.indexOf('=');
                if (ind > 0) {
                    String key = pair.substring(0, ind);
                    String val = pair.substring(ind + 1);
                    String decKey = URLDecoder.decode(key, encoding);
                    String decVal = URLDecoder.decode(val, encoding);
                    String[] vals = (String[]) map.get(decKey);
                    if (vals == null) vals = new String[] { decVal };
                    else {
                        String[] tmp = new String[vals.length + 1];
                        for (int i = 0; i < vals.length; i++)
                            tmp[i] = vals[i];
                        tmp[vals.length] = decVal;
                        vals = tmp;
                    }
                    map.put(decKey, vals);
                }
            }
        }
        return map;
    }

    public String getParameter(String name) {
        String[] vals = (String[]) params.get(name);
        if (vals != null && vals.length > 0) return vals[0];
        return null;
    }

    public Enumeration<String> getParameterNames() {
        Vector<String> v = new Vector<String>();
        if (!params.isEmpty()) {
            Iterator<String> it = params.keySet().iterator();
            while (it.hasNext())
                v.add(it.next());
        }
        return v.elements();
    }

    public String[] getParameterValues(String name) {
        return params.get(name);
    }

    public Map<String, String[]> getParameterMap() {
        return params;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("URIParameters:\n");
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String name = it.next();
            sb.append("   ");
            sb.append(name);
            sb.append(" = ");
            String[] values = (String[]) params.get(name);
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i]);
                if (i < values.length - 1) sb.append(" | ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
