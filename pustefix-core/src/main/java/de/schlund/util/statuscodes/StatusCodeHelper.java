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
package de.schlund.util.statuscodes;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class StatusCodeHelper {

    private final static String DEFAULT_STATUSCODE_CLASS = "org.pustefixframework.generated.CoreStatusCodes";
    
    public static StatusCode getStatusCodeByName(String name) {
        return getStatusCodeByName(name, false);
    }
    
    public static StatusCode getStatusCodeByName(String name, boolean optional) {
        StatusCode statusCode;
        String scClassName, scName;
        int ind = name.indexOf('#');
        if(ind==-1) {
            scClassName = DEFAULT_STATUSCODE_CLASS;
            scName = name;
        } else {
            scClassName = name.substring(0,ind);
            scName = name.substring(ind+1);
        }
        try {
            Class<?> scClass = Class.forName(scClassName);
            Method scMeth = scClass.getDeclaredMethod("getStatusCodeByName",String.class);
            statusCode = (StatusCode)scMeth.invoke(null, scName);
            if(statusCode==null && optional==false) 
                throw new StatusCodeException("StatusCode " + name + " is not defined.");
        } catch(ClassNotFoundException x) {
            throw new StatusCodeException("StatusCode class not found: "+scClassName,x);
        } catch(NoSuchMethodException x) {
            throw new StatusCodeException("StatusCode class hasn't method 'getStatusCodeByName'",x);
        } catch(Exception x) {
            throw new StatusCodeException("Can't get StatusCode '"+scName+"' from class '"+scClassName+"'",x);
        }
        return statusCode;
    }
    
    
    /**
     * Rewrites statusmessage file URLs from local docroot to dynamic module search URLS
     * if file doesn't exist locally. Thus both, the old statusmessage merge and the new
     * language module override mechanism, can be supported in parallel.
     */
    public static URI[] update(final URI[] uris) {
        //only apply when docroot is set (workaround for unit tests which didn't set docroot)
        if(GlobalConfig.getDocroot() != null || GlobalConfig.getServletContext() != null) {
            for(int i=0; i<uris.length; i++) {
                URI uri = uris[i];
                Resource res = ResourceUtil.getResource(uri);
                if(!res.exists() && uri.getScheme().equals("docroot") && uri.getPath().startsWith("/modules-override/")) {
                    String path = uri.getPath().substring(18);
                    int ind = path.indexOf('/');
                    String module = path.substring(0, ind);
                    path = path.substring(ind + 1);
                    path = path.replaceAll("-merged", "");
                    try {
                        uris[i] = new URI("dynamic://" + module + "/" + path);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException("Error updating statusmessage URI: " + uris[i], e);
                    }
                }
            }
        }
        return uris;
    }
    
}
