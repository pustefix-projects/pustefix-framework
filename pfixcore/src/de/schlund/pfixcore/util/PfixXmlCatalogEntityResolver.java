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
package de.schlund.pfixcore.util;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.readers.OASISXMLCatalogReader;
import org.apache.xml.resolver.readers.SAXCatalogReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author adam
 */
public class PfixXmlCatalogEntityResolver implements EntityResolver {
    
    private static String DEFAULT_SAXPARSERFACTORY = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl";
    
    Catalog catalog = new Catalog();
    
    public PfixXmlCatalogEntityResolver(String catalogfile) throws MalformedURLException, IOException {
        // get rid of ugly message during build (" [pfx-iwrp] Cannot find CatalogManager.properties")
        CatalogManager.getStaticManager().setIgnoreMissingProperties(true);
        // see org.apache.xerces.util.XMLCatalogResolver.attachReaderToCatalog(Catalog)
        SAXParserFactory spf = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if(cl == null) cl = getClass().getClassLoader();
            Class<?> clazz = Class.forName(DEFAULT_SAXPARSERFACTORY, true, cl);
            spf = (SAXParserFactory)clazz.newInstance();
        } catch(Exception x) {
            x.printStackTrace();
            //ignore and try to get SAXParserFactory via factory finder in next step
        }
        if(spf == null) {
            try {
                spf = SAXParserFactory.newInstance();
            } catch(FactoryConfigurationError x) {
                throw new RuntimeException("Can't get SAXParserFactory",x);
            }
        }
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXCatalogReader saxReader = new SAXCatalogReader(spf);
        saxReader.setCatalogParser(OASISXMLCatalogReader.namespaceName, "catalog", 
            "org.apache.xml.resolver.readers.OASISXMLCatalogReader");
        catalog.addReader("application/xml", saxReader);
        catalog.parseCatalog(catalogfile);
    }
        
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        String resolvedId = null;
        if (publicId != null && systemId != null) {
            resolvedId = catalog.resolvePublic(publicId, systemId);
        }
        else if (systemId != null) {
            resolvedId = catalog.resolveSystem(systemId);
        }
        
        if (resolvedId != null) {
            InputSource source = new InputSource(resolvedId);
            source.setPublicId(publicId);
            return source;
        }
        // DEBUG
        //System.out.println("PfixXmlCatalogEntityResolver.resolveEntity(publicId="+publicId+", systemId="+systemId+")="+resolvedId);
        return null;
    }
    
}
