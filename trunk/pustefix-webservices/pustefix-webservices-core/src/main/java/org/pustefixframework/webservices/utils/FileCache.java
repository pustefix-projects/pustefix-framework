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

package org.pustefixframework.webservices.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author mleidig@schlund.de
 */
public class FileCache {

    final static Logger LOG = Logger.getLogger(FileCache.class);
    private LinkedHashMap<String, FileCacheData> map;

    public FileCache(int size) {
        final int maxSize = size;
        map = new LinkedHashMap<String, FileCacheData>(maxSize, 0.75f, true) {
            private static final long serialVersionUID = 6172037110210448800L;

            @Override
            protected boolean removeEldestEntry(Map.Entry<String, FileCacheData> eldest) {
                boolean exceeded = size() > maxSize;
                if (exceeded) LOG.warn("Cache maximum size exceeded. Eldest entry to be removed.");
                return exceeded;
            }
        };
    }

    public synchronized void put(String name, FileCacheData data) {
        map.put(name, data);
    }

    public synchronized FileCacheData get(String name) {
        FileCacheData data = map.get(name);
        return data;
    }

    public static void main(String[] args) {
        FileCache cache = new FileCache(3);
        cache.put("a", new FileCacheData("aaa".getBytes()));
        cache.put("b", new FileCacheData("bbb".getBytes()));
        cache.put("c", new FileCacheData("ccc".getBytes()));
        cache.get("a");
        cache.get("b");
        cache.put("d", new FileCacheData("ddd".getBytes()));
        FileCacheData data = cache.get("b");
        String res = (data == null) ? "null" : new String(data.bytes) + " " + data.md5;
        System.out.println(res);
    }

}
