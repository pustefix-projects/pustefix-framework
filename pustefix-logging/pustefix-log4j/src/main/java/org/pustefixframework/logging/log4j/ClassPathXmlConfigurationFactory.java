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
package org.pustefixframework.logging.log4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Extends Log4j's {@link XmlConfigurationFactory} to add support for resolving
 * entities from the classpath using "classpath:" URIs.
 */
@Plugin(name = "ClassPathXmlConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(10)
public class ClassPathXmlConfigurationFactory extends XmlConfigurationFactory {

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
        ConfigurationSource resolvedSource = source;
        try {
            InputSource in = new InputSource(source.getInputStream());
            in.setSystemId(source.getLocation());
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setXIncludeAware(true);
            XMLReader reader = spf.newSAXParser().getXMLReader();
            ClassPathResolver resolver = new ClassPathResolver();
            reader.setEntityResolver(resolver);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new SAXSource(reader, in), new StreamResult(bout));
            bout.close();
            if(resolver.hasResolved()) {
                ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
                resolvedSource = new ConfigurationSource(bin);
            } else {
                resolvedSource = source.resetInputStream();
            }
        } catch(Exception x) {
            LOGGER.error("Error preprocessing Log4j configuration", x);
        }
        return new XmlConfiguration(loggerContext, resolvedSource);
    }


    private class ClassPathResolver implements EntityResolver {

        private boolean resolved;

        public boolean hasResolved() {
            return resolved;
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if(systemId != null && systemId.startsWith("classpath:")) {
                String path = systemId.substring(10);
                InputStream in = getClass().getClassLoader().getResourceAsStream(path);
                if(in != null) {
                    InputSource src = new InputSource(in);
                    src.setSystemId(systemId);
                    resolved = true;
                    return src;
                }
            }
            return null;
        }
    }

}
