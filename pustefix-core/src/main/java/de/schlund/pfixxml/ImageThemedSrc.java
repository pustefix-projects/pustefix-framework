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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.support.NullResource;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.pustefixframework.xmlgenerator.targets.VirtualTarget;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XsltContext;
    
/**
 * Describe class ImageThemedSrc here.
 *
 *
 * Created: Wed Mar 23 17:15:43 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class ImageThemedSrc {
    private final static Logger LOG = Logger.getLogger(ImageThemedSrc.class);

    /** xslt extension */
    public static String getSrc(XsltContext context, String src, String themed_path, String themed_img,
                                String parent_part_in, String parent_theme_in,
                                TargetGenerator targetGen, String targetKey, String module, String search) throws Exception {
        
    	try {
    	
        boolean dynamic = false;
        if(search!=null && !search.trim().equals("")) {
            if(search.equals("dynamic")) dynamic = true;
            else throw new XMLException("Unsupported include search argument: " + search);
        }
        
        String parentSystemId = context.getSystemId();
        URI parentURI = new URI(parentSystemId);
        
        if(module != null) {
            module = module.trim();
            if(module.equals("")) module = null;
        }
        
        if(module == null && "bundle".equals(parentURI.getScheme())) module = parentURI.getAuthority();
        if(module == null) throw new IllegalArgumentException("Can't detect source bundle for requested image: " 
        		+ src + " " + themed_path + " " + themed_img);
        
        String[]        themes    = null;
          
        VirtualTarget target = null;
        if (!targetKey.equals("__NONE__")) {
            target = (VirtualTarget) targetGen.getTarget(targetKey);
            themes               = target.getThemes().getThemesArr();
        }
        if (themes == null) {
            themes = targetGen.getGlobalThemes().getThemesArr();
        }
        
        String parent_uri_str  = "";
        
        if (IncludeDocumentExtension.isIncludeDocument(context)) {
            parent_uri_str  = parentSystemId;
        }
        
        Resource parent_path = null;
        if(!parent_uri_str.equals("")) {
        	URI uri = new URI(parent_uri_str);
        	parent_path = targetGen.getResourceLoader().getResource(uri);
        }
        
        if (isSimpleSrc(src, themed_path, themed_img)) {
            
        	if (src.startsWith("/")) {
                src = src.substring(1);
            }
            String uriStr;
            if(dynamic) {
            	uriStr = "dynamic://" + module + "/PUSTEFIX-INF/" + src + "?application=" + targetGen.getApplicationBundle();
            } else {
            	uriStr = "bundle://" + module + "/PUSTEFIX-INF/" + src;
            }
            URI uri = new URI(uriStr);
            Resource res = targetGen.getResourceLoader().getResource(uri);
            if(res == null) res = new NullResource(uri);
            DependencyTracker.logTyped("image", res, "", "", parent_path, parent_part_in, parent_theme_in, target);
            //TODO: return request-path-prefix + path by looking up static resource extensions
            return res.getOriginalURI().toASCIIString();
            
        } else if (isThemedSrc(src, themed_path, themed_img)) {
        	
            if (themed_path.startsWith("/")) {
                themed_path = themed_path.substring(1);
            }
            
            if(dynamic) {
            	
                String themeParam = "&themes=";
                for (int i = 0; i < themes.length; i++) {
                    themeParam += themes[i];
                    if(i<themes.length-1) themeParam += ",";
                }
                String uriStr = "dynamic://" + module + "/PUSTEFIX-INF/" + themed_path +"/THEME/" + themed_img + "?application="+targetGen.getApplicationBundle();
                uriStr += themeParam;
                URI uri = new URI(uriStr);
                Resource res = targetGen.getResourceLoader().getResource(uri);
                if(res == null) res = new NullResource(uri);
                DependencyTracker.logTyped("image", res, "", "", parent_path, parent_part_in, parent_theme_in, target);
                //TODO: return request-path-prefix + path by looking up static resource extensions
                return res.getOriginalURI().toASCIIString();
                
            } else {
            
            	String uriStr = src;
            	for (int i = 0; i < themes.length; i++) {
            		String currtheme = themes[i];
            		uriStr = "bundle://" + module + "/PUSTEFIX-INF/" + themed_path + "/" + currtheme + "/" + themed_img;
            		URI uri = new URI(uriStr);
            		Resource res = targetGen.getResourceLoader().getResource(uri);
            		if(res != null) {
            			DependencyTracker.logTyped("image", res, "", "", parent_path, parent_part_in, parent_theme_in, target);
            			//TODO: return request-path-prefix + path by looking up static resource extensions
            			return res.getOriginalURI().toASCIIString();
            		} 
            		if (i < (themes.length - 1)) {
            			// FIXME: the next commented line should be used sometime so we can discriminate between
            			// "real" missing and "missing, but we found a better version" -- but make sure editor copes with it.
            			//DependencyTracker.logImage(context, testsrc, parent_part_in, parent_theme_in, targetGen, targetKey, "shadow");
            			DependencyTracker.logTyped("image", new NullResource(uri), "", "", parent_path, parent_part_in, parent_theme_in, target);
            			LOG.info("    -> Image src '" + uriStr + "' not found, trying next theme");
            		} else {
            			DependencyTracker.logTyped("image", new NullResource(uri), "", "", parent_path, parent_part_in, parent_theme_in, target);
            			LOG.warn("    -> No themed image found!");
            		}
            	}
            	return uriStr;
            
            }

        } else {
            throw new XMLException("Need to have one of 'src' XOR both 'themed-path' and 'themed-img' given!");
        }
        
    	} catch(Exception x) {
    		ExtensionFunctionUtils.setExtensionFunctionError(x);
    		throw x;
    	}
    }


    private static boolean isSimpleSrc(String src, String path, String img) {
        return (src != null && !src.equals("") && (path == null || path.equals("")) && (img == null || img.equals("")));
    }

    private static boolean isThemedSrc(String src, String path, String img) {
        return ((src == null || src.equals("")) && path != null && !path.equals("") && img != null && !img.equals(""));
    }

    /**
     * Transforms a URI (with the bundle scheme) to a path, that can be used
     * to access the resource over the web. This will only work for Pustefix 
     * bundles and only if the resource path has been explicitly exported by 
     * the bundle.
     * 
     * @param uri URI string
     * @return path relative to servlet context path (starting with a slash)
     */
    public static String uriToPath(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            // Invalid URI
            return uriString;
        }
        if (uri.getScheme().equals("bundle")) {
            String bundleSymbolicName = uri.getAuthority();
            String path = uri.getPath();
            if (path.startsWith("/PUSTEFIX-INF/")) {
                path = path.substring(13);
            }
            return "/bundle/" + bundleSymbolicName + path;
        }
        // URI cannot be transformed for unknown scheme,
        // thus the original string is returned
        return uriString;
    }

}
