package org.pustefixframework.util.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class NamespaceUtils {

    public static String getNamespace(InputStream in) throws IOException, SAXException {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setValidating(false);
        parserFactory.setNamespaceAware(true);
        SAXParser parser;
        try {
            parser = parserFactory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Can't get SAX parser", e);
        }
        XMLReader reader = parser.getXMLReader();
        StopContentHandler contentHandler = new StopContentHandler();
        reader.setContentHandler(contentHandler);
        InputSource src = new InputSource(in);
        try {
            reader.parse(src);
        } catch(StopException x) {}
        in.close();
        return contentHandler.getNamespace();
    }
    
    private static class StopContentHandler implements ContentHandler {

        private String namespace;
        
        public String getNamespace() {
            return namespace;
        }
        
        public void characters(char[] ch, int start, int length) throws SAXException {}
        public void endDocument() throws SAXException {}
        public void endElement(String uri, String localName, String qName) throws SAXException {}
        public void endPrefixMapping(String prefix) throws SAXException {}
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
        public void processingInstruction(String target, String data) throws SAXException {}
        public void setDocumentLocator(Locator locator) {}
        public void skippedEntity(String name) throws SAXException {}
        public void startDocument() throws SAXException {}
        public void startPrefixMapping(String prefix, String uri) throws SAXException {}
        
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            namespace = uri;
            throw new StopException();
        }
        
    }
    
    private static class StopException extends RuntimeException {

        private static final long serialVersionUID = 2009602618486377774L;        
        
    }
    
}
