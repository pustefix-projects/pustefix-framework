package de.schlund.pfixxml.util;

import java.util.Properties;

/**
 * Describe class PfxProperties here.
 *
 *
 * Created: Mon Feb 21 14:49:37 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class PfxProperties extends Properties {
    Object obj = new Object();
    
    public PfxProperties(Properties props) {
        super(props);
    }
    
    public int hashCode() {
        return obj.hashCode();
    }
}
