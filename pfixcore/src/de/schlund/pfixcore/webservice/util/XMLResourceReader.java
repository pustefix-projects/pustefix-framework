package de.schlund.pfixcore.webservice.util;

import java.io.*;
import org.apache.log4j.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * This class acts as a helper class for reading in XML resources via SAX.
 * This is done using the classloading mechanism. External entities are resolved the same way.
 * The SAX event handling is done by a XMLHandler instance resp. a derivation's instance.
 * 
 * @author mleidig
 * 
 */
public class XMLResourceReader {
	
    protected static Category LOG=Category.getInstance(XMLResourceReader.class.getName());
	
    private String DEFAULT_PARSER="org.apache.xerces.parsers.SAXParser";
  
    InputStream inputStream;
    Reader reader;
    String parserName;  
    String systemId;
    XMLHandler handler;
    boolean validate;
    File file;
	
    /**
     * A XMLResourceReader instance is created by passing the resource name, the XMLHandler and an indication
     * if the XML document should be validated.
     */
    public XMLResourceReader(InputStream inputStream,String systemId,XMLHandler handler,boolean validate) {
        this.inputStream=inputStream;
        init(systemId,handler,validate);
    }
    
    public XMLResourceReader(Reader reader,String systemId,XMLHandler handler,boolean validate) {
        this.reader=reader;
        init(systemId,handler,validate);
    }
	
    private void init(String systemId,XMLHandler handler,boolean validate) {
        this.systemId=systemId;
        this.handler=handler;
        this.validate=validate;
        if(systemId==null) systemId="";
        String parser=System.getProperty("org.xml.sax.driver");
        if(parser!=null && !parser.equals("")) {
            parserName=parser;
        } else {
            parserName=DEFAULT_PARSER;
        }       
    }
    
    /**
     * This methods reads in the XML document (parsing, validating, handling SAX events).
     */
    public  void read() throws XMLProcessingException {
        try {
            InputSource src=null;
            if(inputStream!=null) src=new InputSource(inputStream);
            else if(reader!=null) src=new InputSource(reader);
            else {
                throw new XMLProcessingException("Can't get input");
            }
            XMLReader reader=XMLReaderFactory.createXMLReader(parserName);
            reader.setContentHandler(handler);
            reader.setEntityResolver(handler);
            reader.setErrorHandler(handler);
            if(validate) reader.setFeature("http://xml.org/sax/features/validation", true);
            handler.setSystemId(systemId);
            reader.parse(src);
        } catch(IOException x) {
            LOG.error("Error occurred while reading resource '"+systemId+"'.",x);
            throw new XMLProcessingException("Error occurred while reading resource '"+systemId+"'.",x);
        } catch(SAXException x) {
            Exception cause=x;
            if(x.getException()!=null) cause=x.getException();
            LOG.error("Error occurred while reading resource '"+systemId+"'.",cause);
            throw new XMLProcessingException("Error occurred while reading resource '"+systemId+"'.",cause);
        }
    }
	
}