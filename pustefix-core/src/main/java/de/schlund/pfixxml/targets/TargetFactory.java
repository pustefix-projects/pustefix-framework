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
 *
 */

package de.schlund.pfixxml.targets;
import java.lang.reflect.Constructor;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * TargetFactory.java
 *
 *
 * Created: Mon Jul 23 15:38:41 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class TargetFactory {
    private final static Logger  LOG      = Logger.getLogger(TargetFactory.class); 
    private static TargetFactory instance = new TargetFactory();

    private TreeMap<String, TargetRW> targetmap = new TreeMap<String, TargetRW>();
    
    private TargetFactory (){}

    public static TargetFactory getInstance() {
        return instance;
    }

    public boolean exists(TargetType type, TargetGenerator gen, String targetkey) {
        String key = createKey(type, gen, targetkey);
        Target ret = (Target) targetmap.get(key);
        if (ret != null) {
            return true;
        }
        return false;
    }

    public synchronized TargetRW getTarget(TargetType type, TargetGenerator gen, String targetkey, Themes themes) {
        String   key = createKey(type, gen, targetkey);
        TargetRW ret = (TargetRW) targetmap.get(key);
        if (ret == null) {
            ret = createTargetForType(type, gen, targetkey, themes);
            targetmap.put(key, ret);
        }
        return ret;
    }

    private String createKey(TargetType type, TargetGenerator gen, String targetkey) {
        return(type.getTag() + "@" + gen.getName() + "@" + targetkey);
    }
    
    private TargetRW createTargetForType(TargetType type, TargetGenerator gen, String targetkey, Themes themes) {
        TargetRW target;
        LOG.debug("===> Creating target '" + targetkey + "' " + type + " [" + gen.getName() + "]");
        try {
            Class<? extends TargetRW>       theclass    = type.getTargetClass();
            Constructor<? extends TargetRW> constructor = theclass.getConstructor(new Class[]{type.getClass(), gen.getClass(), targetkey.getClass(), Themes.class});
            target = constructor.newInstance(new Object[]{type, gen, targetkey, themes});
        } catch (Exception e) {
            throw new RuntimeException("error creating target '" + targetkey + "' " + type + " [" + gen.getName() + "]: " + e.toString(), e);
        }
        return target;
    }
    
    public void reset() {
        targetmap.clear();
    }
    

}// TargetFactory
