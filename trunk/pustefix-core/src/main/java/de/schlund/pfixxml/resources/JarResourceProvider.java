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

import java.net.URI;

public class JarResourceProvider implements ResourceProvider {

    private final static String JAR_SCHEME = "jar";
    
    private String[] supportedSchemes = {JAR_SCHEME};
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
    public Resource getResource(URI uri) throws ResourceProviderException {
        if (uri.getScheme() == null)
            throw new ResourceProviderException("Missing URI scheme: " + uri);
        if (!uri.getScheme().equals(JAR_SCHEME))
            throw new ResourceProviderException("URI scheme not supported: " + uri);
        return new JarResource(uri);
    }

    public static void main(String[] args) throws Exception {
    	URI uri = new URI("jar:file:/data/checkouts/pustefix.svn.sourceforge.net/pustefix-zero/pustefix-samples/pustefix-i18n/pustefix-i18n-webapp/target/pustefix-i18n-webapp-0.18.38-SNAPSHOT/WEB-INF/lib/log4j-1.2.17.jar!/META-INF/maven/log4j/log4j/pom.xml");
    	System.out.println(uri.getRawSchemeSpecificPart());
    }
    
}
