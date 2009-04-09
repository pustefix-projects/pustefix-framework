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
package de.schlund.pfixxml.perflogging;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class AdditionalTrailInfoFactory {
    private static AdditionalTrailInfoFactory instance = new AdditionalTrailInfoFactory();
    private final static Logger LOG = Logger.getLogger(AdditionalTrailInfoFactory.class);

    private AdditionalTrailInfoFactory() {} 
    
    private Map<String, AdditionalTrailInfo> implementors = new HashMap<String, AdditionalTrailInfo>();
        
    public static AdditionalTrailInfoFactory getInstance() {
        return instance;
    }
    
    public synchronized AdditionalTrailInfo getAdditionalTrailInfo(String implemenation_class) {
        
        if (implementors.get(implemenation_class) == null) {
            LOG.info("Creating object from class "+implemenation_class);
            try {
                Class<?>             clazz = Class.forName(implemenation_class);
                AdditionalTrailInfo info  = (AdditionalTrailInfo) clazz.newInstance();
                implementors.put(implemenation_class, info);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Didn't find class " + implemenation_class, e);
            } catch (InstantiationException e) {
                throw new IllegalStateException("Couldn't instantiate " + implemenation_class, e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return implementors.get(implemenation_class);
    }

}
