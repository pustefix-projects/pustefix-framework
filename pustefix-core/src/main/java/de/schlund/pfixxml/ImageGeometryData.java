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

import de.schlund.pfixxml.resources.FileResource;


/**
 * ImageGeometryData.java
 *
 *
 * Created: Tue Apr 16 23:55:52 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ImageGeometryData {
    private boolean ok = false;
    private int     width;
    private int     height;
    private long    mtime;
    private String  type;
    
    public ImageGeometryData(FileResource img) throws IOException {
        ImageInfo       info       = new ImageInfo();
        InputStream     img_stream = img.getInputStream();
        info.setInput(img_stream);
        if (info.check()) {
            ok     = true;
            mtime  = img.lastModified();
            type   = info.getFormatName();
            width  = info.getWidth();
            height = info.getHeight(); 
        }
        img_stream.close();
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

}// ImageGeometryData
