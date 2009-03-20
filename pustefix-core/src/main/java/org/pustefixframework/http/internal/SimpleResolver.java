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
package org.pustefixframework.http.internal;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class SimpleResolver implements URIResolver {
    public static Transformer configure(TransformerFactory factory, String resource) throws TransformerConfigurationException {
        URIResolver resolver = new SimpleResolver(factory.getURIResolver()); 
        factory.setURIResolver(resolver);
        Transformer transformer = factory.newTransformer(new StreamSource(SimpleResolver.class.getResource(resource).toString()));
        transformer.setURIResolver(resolver);
        return transformer;
    }

    //--
    
    private URIResolver defaultResolver;
    
    public SimpleResolver(URIResolver defaultResolver) {
        this.defaultResolver = defaultResolver;
    }

    public Source resolve(String href, String base) throws TransformerException {
        if (href.contains(":")) {
            return defaultResolver.resolve(href, base);
        } else {
            System.out.println("href: " + href + ", base: " + base);
            return defaultResolver.resolve("jar:file:/home/mhm/Projects/pustefixframework/pustefix-samples/pustefix-sample1/target/pustefix-sample1-0.14.0-SNAPSHOT/WEB-INF/lib/pustefix-core-0.14.0-SNAPSHOT.jar!/build/" + href, base);
        }
    }
}