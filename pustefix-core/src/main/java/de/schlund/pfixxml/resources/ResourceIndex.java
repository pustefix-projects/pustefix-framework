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

package de.schlund.pfixxml.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixxml.config.GlobalConfig;

/**
 * The resource index of the application and all modules.
 * Can be queried for the existence of a resource by its URL.
 *  
 */
public class ResourceIndex {
	
	private static ResourceIndex instance = new ResourceIndex();
	
	private String docrootUrl;
	private Map<String, ResourceIndexMap> urlToIndex = new HashMap<String, ResourceIndexMap>();
	private ResourceIndexMap appIndex;
	
	private ResourceIndex() {
		try {
			if(GlobalConfig.getDocroot() != null) {
				docrootUrl = GlobalConfig.getDocrootAsURL().toExternalForm();
			}
            read();
        } catch(IOException x) {
            throw new PustefixRuntimeException(x);
        }
	}
	
	public static ResourceIndex getInstance() {
		return instance;
	}
	
    private void read() throws IOException {
        Enumeration<URL> urls = getClass().getClassLoader().getResources("META-INF/pustefix-resource.index");
        while(urls.hasMoreElements()) {
            URL url = urls.nextElement();
            read(url);
        }
    }
    
	private void read(URL url) throws IOException {
		InputStream in = url.openStream();
		ResourceIndexMap index = ResourceIndexMap.read(in);
		String urlStr = url.toExternalForm();
		if(url.getProtocol().equals("jar")) {
			int ind = urlStr.indexOf('!');
			if(ind > -1) {
				urlStr = urlStr.substring(0, ind);
			}
		} else if(url.getProtocol().equals("file")) {
			int ind = urlStr.indexOf("/WEB-INF");
			if(ind > -1) {
				urlStr = urlStr.substring(0, ind);
			}
			appIndex = index;
		}
		urlToIndex.put(urlStr, index);
		in.close();
	}
	
	public boolean exists(URL url) throws IOException {

		boolean exists = false;
		if(url.getProtocol().equals("jar")) {
			String urlStr = url.toString();
			int ind = urlStr.indexOf('!');
			String jarUrlStr = urlStr.substring(0, ind);
			String pathUrlStr = urlStr.substring(ind+2);
			ResourceIndexMap index = urlToIndex.get(jarUrlStr);
			if(index != null) {
				exists = index.exists(pathUrlStr);
			}
		} else if(url.getProtocol().equals("file")) {
			String urlStr = url.toString();
			if(urlStr.startsWith(docrootUrl)) {
				String pathUrlStr = urlStr.substring(docrootUrl.length());
				ResourceIndexMap index = urlToIndex.get(docrootUrl);
				if(index != null) {
					exists = index.exists(pathUrlStr);
				}
			}
		}
		return exists;
	}
	
	public boolean exists(String path) throws IOException {
		
		boolean exists = appIndex.exists(path);
		return exists;
	}
}
