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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.ImageGeometryData;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;


/**
 * ImageGeometry.java
 *
 *
 * Created: Tue Apr 16 23:43:02 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ImageGeometry {
    private static Map<String, ImageGeometryData> imageinfo = new HashMap<String, ImageGeometryData>();
    private final static Logger                         LOG       = Logger.getLogger(ImageGeometry.class); 
    
    
    public static int getHeight(String path) throws Exception {
    	try {
	        ImageGeometryData data = getImageGeometryData(path);
	        if (data == null) {
	            return -1;
	        } else {
	            return data.getHeight();
	        }
    	} catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            x.printStackTrace();
            throw x;
        }
    }
    
    public static int getWidth(String path) throws Exception {
    	try {
	        ImageGeometryData data = getImageGeometryData(path);
	        if (data == null) {
	            return -1;
	        } else {
	            return data.getWidth();
	        }
    	} catch (Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            x.printStackTrace();
            throw x;
        }
    }

    public static String getType(String path) throws Exception {
        try {
	        ImageGeometryData data = getImageGeometryData(path);
	        if (data == null) {
	            return null;
	        } else {
	            return data.getType();
	        }
		} catch (Exception x) {
	        ExtensionFunctionUtils.setExtensionFunctionError(x);
	        x.printStackTrace();
	        throw x;
	    }
    }
    
    public static String getStyleStringForImage(String path, String userStyle, String userWidth, String userHeight) throws Exception {
    	try {
        ImageGeometryData data = getImageGeometryData(path);
        int targetWidth=-1;
        int targetHeight=-1;
        String targetWidthUnit = "px";
        String targetHeightUnit = "px";

        if (userWidth != null && userWidth.length() > 0) {
            userWidth = userWidth.trim();
            if (userWidth.endsWith("%")) {
                targetWidthUnit = "%";
                userWidth = userWidth.substring(0, userWidth.length() - 1);
            }
            try {
                targetWidth = Integer.parseInt(userWidth);
            } catch (NumberFormatException e) {
                LOG.error("*** Image " + path + " supplied invalid data for width parameter: " + userWidth);
                targetWidth = -1;
            }
        } else {
            if (data != null) targetWidth = data.getWidth();
        }
        if (userHeight != null && userHeight.length() > 0) {
            userHeight = userHeight.trim();
            if (userHeight.endsWith("%")) {
                targetHeightUnit = "%";
                userHeight = userHeight.substring(0, userHeight.length() - 1);
            }
            try {
                targetHeight = Integer.parseInt(userHeight);
            } catch (NumberFormatException e) {
                LOG.error("*** Image " + path + " supplied invalid data for height parameter: " + userHeight);
                targetHeight = -1;
            }
        } else {
            if (data != null) targetHeight = data.getHeight();
        }
        
        boolean haveWidth = false, haveHeight = false;
        
        if (userStyle == null) {
            userStyle = "";
        }
        StringBuffer genStyle = new StringBuffer(userStyle.trim());
        
        StringTokenizer st = new StringTokenizer(userStyle, ";");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            String propName = token.substring(0, token.indexOf(':'));
            propName = propName.trim().toLowerCase();
            if (propName.equals("width")) {
                haveWidth = true;
            } else if (propName.equals("height")) {
                haveHeight = true;
            }
        }
        
        if (!haveWidth && targetWidth != -1) {
            if (genStyle.length() > 0 && genStyle.charAt(genStyle.length()-1) != ';') {
                genStyle.append(';');
            }
            genStyle.append("width:");
            genStyle.append(targetWidth);
            genStyle.append(targetWidthUnit + ";");
        }
        
        if (!haveHeight && targetHeight != -1) {
            if (genStyle.length() > 0 && genStyle.charAt(genStyle.length()-1) != ';') {
                genStyle.append(';');
            }
            genStyle.append("height:");
            genStyle.append(targetHeight);
            genStyle.append(targetHeightUnit + ";");
        }
        
        return genStyle.toString();
    	} catch (Exception x) {
    		x.printStackTrace();
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

    private static ImageGeometryData getImageGeometryData(String path) {
    	System.out.println("TEST:"+path);
        if(path.startsWith("http") || path.startsWith("//")) {
            return getRemoteImageGeometryData(path);
        }
        synchronized (imageinfo) {
            if(path.startsWith("modules/")) path = "module://" + path.substring(8);
            Resource img = ResourceUtil.getResource(path);
            if (img.exists() && img.canRead() && img.isFile()) {
                long              mtime = img.lastModified();
                ImageGeometryData tmp = imageinfo.get(path);
                if (tmp == null || mtime > tmp.lastModified()) {
                    // LOG.debug("Cache miss or outdated for: " + path);
                    try {
                        tmp = new ImageGeometryData(img);
                    } catch (IOException e) {
                        LOG.error("*** Couldn't get geometry for " + path, e);
                        return null;
                    }
                    if (!tmp.isOK()) {
                        LOG.error("*** Image data wasn't recognized for " + path);
                        return null;
                    }
                    imageinfo.put(path, tmp);
                } else {
                    // CAT.debug("Cache hit and uptodate for: " + path);
                }
                return tmp;
            }
            return null;
        }
    }

    private static ImageGeometryData getRemoteImageGeometryData(String path) {
        synchronized (imageinfo) {
            ImageGeometryData geom = imageinfo.get(path);
            if(geom != null) return geom; 
        }
        try {
            String urlStr = path;
            if (urlStr.startsWith("https")) {
            	urlStr = "http" + urlStr.substring(5);
            }
            else if(urlStr.startsWith("//")) {
            	urlStr = "http:" + urlStr;
            }
            URL url = new URL(urlStr);
            HttpURLConnection con=(HttpURLConnection)url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            ImageGeometryData geom = new ImageGeometryData(con);
            //TODO: notice HTTP caching headers
            synchronized (imageinfo) {
                imageinfo.put(path, geom);
            }
            return geom;
        } catch(Exception x) {
            LOG.error("Couldn't get image size for " + path, x);
            return null;
        }
    }

}// ImageGeometry
