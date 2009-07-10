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

package de.schlund.pfixxml.targets;
import java.lang.reflect.Constructor;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.pustefixframework.resource.FileResource;
import org.pustefixframework.resource.Resource;

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

    public synchronized TargetRW getTarget(TargetType type, TargetGenerator gen, Resource targetRes, FileResource targetAuxRes, String targetkey, Themes themes) {
        String   key = createKey(type, gen, targetkey);
        TargetRW ret = (TargetRW) targetmap.get(key);
        if (ret == null) {
            ret = createTargetForType(type, gen, targetRes, targetAuxRes, targetkey, themes);
            targetmap.put(key, ret);
        }
        return ret;
    }

    private String createKey(TargetType type, TargetGenerator gen, String targetkey) {
        return(type.getTag() + "@" + gen.getName() + "@" + targetkey);
    }
    
    private TargetRW createTargetForType(TargetType type, TargetGenerator gen, Resource targetRes, FileResource targetAuxRes, String targetkey, Themes themes) {
        TargetRW target;
        LOG.debug("===> Creating target '" + targetkey + "' " + type + " [" + gen.getName() + "]");
        if (!(targetRes instanceof FileResource)) {
            if (type.equals(TargetType.XML_VIRTUAL) || type.equals(TargetType.XSL_VIRTUAL)) {
                throw new RuntimeException("error creating target '" + targetkey + "' " + type + " [" + gen.getName() + "]: Attempt to create virtual target from resource that is not implementing FileResource"); 
            }
        }
        try {
            Constructor<? extends TargetRW> constructor = type.getTargetClassConstructor();
            target = constructor.newInstance(new Object[]{type, gen, targetRes, targetAuxRes, targetkey, themes});
        } catch (Exception e) {
            throw new RuntimeException("error creating target '" + targetkey + "' " + type + " [" + gen.getName() + "]: " + e.toString(), e);
        }
        return target;
    }
    
    public void reset() {
        targetmap.clear();
    }
    

}// TargetFactory
