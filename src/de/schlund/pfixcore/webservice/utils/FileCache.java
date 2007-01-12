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

package de.schlund.pfixcore.webservice.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author mleidig@schlund.de
 */
public class FileCache {

    private static Logger LOG=Logger.getLogger(FileCache.class);
    
    private File dir;
    private LinkedHashMap<String,FileCacheData> map;
    
    public FileCache(int size) {
        final int maxSize=size;
        map=new LinkedHashMap<String,FileCacheData>(maxSize,0.75f,true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size()>maxSize;
             }
        };
    }
    
    public FileCache(int size,File dir) {
        this(size);
        this.dir=dir;
    }
    
    public void put(String name,byte[] bytes) {
        map.put(name,new FileCacheData(bytes));
    }
    
    public synchronized void put(String name,FileCacheData data) {
        map.put(name,data);
    }
    
    public synchronized FileCacheData get(String name) {
        FileCacheData data=map.get(name);
        if(dir!=null) {
            File file=new File(dir,name);
            if(file.exists()) {
                try {
                    byte[] bytes=read(file);
                    data=new FileCacheData(bytes);
                    map.put(name,data);
                } catch(IOException x) {
                    LOG.warn("Can't read data from file: "+file.getAbsolutePath(),x);
                }
            }
        }
        return data;
    }
    
    private byte[] read(File file) throws IOException {
        FileInputStream in=new FileInputStream(file);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[4096];
        int no=0;
        try {
            while((no=in.read(buffer))!=-1) out.write(buffer,0,no);
        } finally {
            in.close();
            out.close();
        }
        return out.toByteArray();
    }
    

    
    public static void main(String[] args) {
        FileCache cache=new FileCache(3);
        cache.put("a","aaa".getBytes());
        cache.put("b","bbb".getBytes());
        cache.put("c","ccc".getBytes());
        cache.get("a");
        cache.get("b");
        cache.put("d","ddd".getBytes());
        FileCacheData data=cache.get("b");
        String res=(data==null)?"null":new String(data.bytes)+" "+data.md5;
        System.out.println(res);
    }
    
}
