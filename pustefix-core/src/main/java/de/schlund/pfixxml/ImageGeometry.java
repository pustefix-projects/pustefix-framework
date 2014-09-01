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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;


/**
 * XSLT extension functions to get image size.
 */
public class ImageGeometry {

    private final static Logger LOG = Logger.getLogger(ImageGeometry.class); 
    
    private static ConcurrentHashMap<String, ImageGeometryData> imageinfo = new ConcurrentHashMap<String, ImageGeometryData>();
    
  
    public static int getHeight(String path) {
    
        ImageGeometryData data = getImageGeometryData(path);
        if (data != null && data.isOK()) {
            return data.getHeight();
        } else {
            return -1;
        }
    }
    
    public static int getWidth(String path) {
    
        ImageGeometryData data = getImageGeometryData(path);
        if (data != null && data.isOK()) {
            return data.getWidth();
        } else {
            return -1;
        }
    }

    public static String getType(String path) {
    
        ImageGeometryData data = getImageGeometryData(path);
        if (data != null && data.isOK()) {
            return data.getType();
        } else {
            return null;
        }
    }
    
    public static String getStyleStringForImage(String path, String userStyle, String userWidth, String userHeight) {
        
        ImageGeometryData data = getImageGeometryData(path);
	    if(data != null && data.isOK()) {
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
        } else {
        	return userStyle;
    	}
    }

    private static ImageGeometryData getImageGeometryData(String path) {
        
    	if(path.startsWith("http") || path.startsWith("//")) {
    		return getRemoteImageGeometryData(path);
    	}
    	
    	if(path.startsWith("modules/")) path = "module://" + path.substring(8);
    	Resource img = ResourceUtil.getResource(path);
    	ImageGeometryData tmp = imageinfo.get(path);
    	if (tmp == null || (img.exists() && img.lastModified() > tmp.lastModified())) {
    	    tmp = ImageGeometryData.create(img);
    	    imageinfo.put(path, tmp);
    	}
    	return tmp;
    }
    
    private static ImageGeometryData getRemoteImageGeometryData(String path) {
    	
        ImageGeometryData geom = imageinfo.get(path);
        if(geom == null) {
            String urlStr = path;
            if(urlStr.startsWith("https")) urlStr = "http" + urlStr.substring(5);
            else if(urlStr.startsWith("//")) urlStr = "http:" + urlStr;
            URL url;
            try {
                url = new URL(urlStr);
            } catch(MalformedURLException e) {
                LOG.warn("Can't get image information: " + path + " [" + e + "]");
                return null;
            }
            geom = ImageGeometryData.create(url);
            imageinfo.put(path, geom);
    	}
    	return geom;
    }
    
}
