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

package de.schlund.pfixcore.util;

import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * Insert the type's description here.
 * Creation date: (05/10/00 18:21:36)
 * @author Michael Salzmann
 */

public class PropertiesUtils {
    /** Logging. */
    protected static Logger LOG = Logger.getLogger(PropertiesUtils.class);

    /** Default constructor. */
    protected PropertiesUtils() {
    	super();
    }
    
    /**
     * find all properties whose name starts with the given prefix,
     * strip the prefix from the name, and put them into a hashmap with 
     * their new, shortened name. Example: prefix is "foo", property name
     * is "foo.bar" -&gt; will result in new property name "bar". 
     *
     * Creation date: (05/09/00 17:53:25)
     * @return HashMap
     */
    public static HashMap selectProperties(Properties props,String prefix) {
    	String      p;
    	Enumeration enm;
    	HashMap     result = new HashMap();

    	prefix += '.';
    	enm    = props.propertyNames();
    	while (enm.hasMoreElements()) {
            p = (String) enm.nextElement();
            if (p.startsWith(prefix)) {
                String suffix = p.substring(prefix.length(),p.length());
                result.put(suffix,props.getProperty(p));
            }
    	}
        
    	return result;
    }

    /**
     * Select the Properties with the given prefix and return them in a
     * TreeMap, using the natural Ordering.
     * @return com.sun.java.util.TreeMap. If prefix is null or 0 &lt;
     *     prefix.length() return all Properties sorted. If props is null
     *     return an empty TreeMap
     */
    public static TreeMap selectPropertiesSorted(Properties props, String prefix) {
        return selectPropertiesSorted((Comparator)null, props, prefix);
    }
    
    /**
     * Select the Properties with the given prefix and return them in a
     * TreeMap, using the given Comparator for Ordering.
     * If the Comparator is null, the natural Ordering is used.
     * @return com.sun.java.util.TreeMap. If prefix is null or 0 &lt;
     *     prefix.length() return all Properties sorted. If props is null
     *     return an empty TreeMap
     */
    public static TreeMap selectPropertiesSorted(Comparator comp, Properties props, String prefix) {
        TreeMap rc = null;
        if (comp != null) {
            rc = new TreeMap(comp);
        } else {
            rc = new TreeMap();
        }
        if (props != null) {
            if (prefix != null && 0 < prefix.length()) {
                String dottedPrefix = prefix + '.';
                Enumeration enm = props.propertyNames();
                String pKey = null;
                String newKey = null;
                while (enm.hasMoreElements()) {
                    pKey = (String) enm.nextElement();
                    if (pKey.startsWith(dottedPrefix)) {
                        try {
                            newKey = pKey.substring(dottedPrefix.length(), pKey.length());
                            rc.put(newKey, props.getProperty(pKey));
                        } catch (IndexOutOfBoundsException e) { }
                    }
                }
            } else {
                rc.putAll(props);
            }
        }
        return rc;
    }
    
    public static int getInteger(Properties props, String key) throws IOException {
        String value;
        
        value = getString(props, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IOException("number expected for property '" + key + "': " + value);
        }
    }

    public static String getString(Properties props, String key) throws IOException {
        String value;
        
        value = props.getProperty(key);
        if (value == null) {
            throw new IOException("property not found: " + key);
        }
        return value;
    }
}
