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

package de.schlund.pfixcore.beans.metadata;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author mleidig@schlund.de
 */
public class DefaultLocator implements Locator {

    private final static String DEFAULT_FILE="beanmetadata.xml";
    private final static String DEFAULT_LOCATION="META-INF";
    
    List<URL> resources;
    
    public DefaultLocator() {
        resources=getResourcesFromClasspath();
    }
    
    public DefaultLocator(URL additionalResource) {
        resources=getResourcesFromClasspath();
        resources.add(additionalResource);
    }
    
    public DefaultLocator(List<URL> additionalResources) {
        resources=getResourcesFromClasspath();
        resources.addAll(additionalResources);
    }
    
    public List<URL> getMetadataResources() {
        return resources;
    }
    
    private List<URL> getResourcesFromClasspath() {
        try {
            Enumeration<URL> urls=getClass().getClassLoader().getResources(DEFAULT_LOCATION+"/"+DEFAULT_FILE);
            List<URL> urlList=new ArrayList<URL>();
            while(urls.hasMoreElements()) urlList.add(urls.nextElement());
            return urlList;
        } catch(IOException x) {
            throw new RuntimeException("Can't get metadata resources from classpath.",x);
        }
    }
    
}
