package org.pustefixframework.container.spring.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.Properties;

import org.pustefixframework.config.generic.PropertyFileReader;
import org.springframework.util.DefaultPropertiesPersister;
import org.xml.sax.InputSource;

import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * PropertyPersister implementation, which can read the Pustefix-XML-Property
 * format instead of the Java-Standard-XML-Property format.
 * 
 * @author mleidig
 *
 */
public class PustefixPropertiesPersister extends DefaultPropertiesPersister {
    
    private URI uri;
    
    public void setLocation(URI uri) {
        this.uri = uri;
    }
    
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
            InputSource inputSource = new InputSource(in);
            if(uri != null) inputSource.setSystemId(uri.toASCIIString());
            PropertyFileReader.read(inputSource, properties);
        } catch(ParserException x) {
            String msg = x.getMessage();
            if(x.getCause() != null) msg += "[" + x.getCause().getMessage() +"]";
            //TODO: IOException doesn't support Throwable constructor argument in 1.5
            //we should throw a derived IOException here
            throw new IOException("Error reading XML properties: "+x.getMessage());
        }
    }

}
