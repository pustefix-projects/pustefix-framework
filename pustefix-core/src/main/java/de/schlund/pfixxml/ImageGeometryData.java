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
package de.schlund.pfixxml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.Resource;

/**
 * Provides basic information about an image resource, like size and type.
 */
public class ImageGeometryData {
   
    private static Logger LOG = Logger.getLogger(ImageGeometryData.class);
    
    private final boolean ok;
    private final int width;
    private final int height;
    private long mtime;
    private String type;
    
    private ImageGeometryData(boolean ok, int width, int height, long mtime, String type) {
        
        this.ok = ok;
        this.width = width;
        this.height = height;
        this.mtime = mtime;
        this.type = type;
    }
    
    public boolean isOK() {
        return ok;
    }

    public String getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public long lastModified() {
        return mtime;
    }

    public static ImageGeometryData create(Resource resource) {
       
        boolean ok = false;
        int width = 0;
        int height = 0;
        long mtime = 0;
        String type = null;
        
        if(resource.exists() && resource.canRead() && resource.isFile()) {
            InputStream in = null;
            try {
                in = resource.getInputStream();
                ImageInfo info = new ImageInfo();
                info.setInput(in);
                if(info.check()) {
                    ok = true;
                    mtime = resource.lastModified();
                    type = info.getFormatName();
                    width = info.getWidth();
                    height = info.getHeight(); 
                } else {
                    LOG.warn("Can't get image information: " + resource);
                }
            } catch(IOException x) {
                LOG.warn("Error reading image geometry: " + resource + " [" + x + "]");
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch(IOException x) {
                        //ignore exception while trying to close
                    }
                }
            }
        }
        
        return new ImageGeometryData(ok, width, height, mtime, type);
    }
    
    public static ImageGeometryData create(URL url) {
        
        boolean ok = false;
        int width = 0;
        int height = 0;
        long mtime = 0;
        String type = null;
        
        InputStream in = null;
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(500);
            connection.setReadTimeout(2000);
            in = connection.getInputStream();
            ImageInfo info = new ImageInfo();
            info.setInput(in);
            if(info.check()) {
                ok = true;
                mtime = connection.getLastModified();
                type = info.getFormatName();
                width = info.getWidth();
                height = info.getHeight(); 
            } else {
                LOG.warn("Can't get image information: " + url);
            }
        } catch(IOException x) {
            LOG.warn("Error reading image geometry: " + url + " [" + x + "]");
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException x) {
                    //ignore exception while trying to close
                }
            }
        }
        
        return new ImageGeometryData(ok, width, height, mtime, type);
    }

}