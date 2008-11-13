package org.pustefixframework.container.spring.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.pustefixframework.config.generic.PropertyFileReader;
import org.springframework.util.DefaultPropertiesPersister;

import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * PropertyPersister implementation, which can read the Pustefix-XML-Property
 * format instead of the Java-Standard-XML-Property format.
 * 
 * @author mleidig
 *
 */
public class PustefixPropertiesPersister extends DefaultPropertiesPersister {

    @Override
    public void load(Properties props, InputStream in) throws IOException {
        super.load(props, in);
    }
    
    @Override
    public void load(Properties props, Reader reader) throws IOException {
        super.load(props, reader);
    }
    
    @Override
    public void loadFromXml(Properties properties, InputStream in) throws IOException { 
        try {
            PropertyFileReader.read(in, properties);
        } catch(ParserException x) {
            throw new IOException("Error reading XML properties", x);
        }
    }
    
}
