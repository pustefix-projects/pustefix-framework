package org.pustefixframework.xml.tools;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixxml.resources.Resource;


public class XSLInfoParser {
 
    private final static String XMLNS_XSL = "http://www.w3.org/1999/XSL/Transform";
    
    public static XSLInfo parse(Resource resource) throws XSLInfoParsingException {
        InputSource in = new InputSource();
        try {
            in.setByteStream(resource.getInputStream());
            in.setSystemId(resource.toURI().toASCIIString());
            XSLInfo info = parse(in);
            info.setLastModified(resource.lastModified());
            return info;
        } catch(IOException x) {
            throw new XSLInfoParsingException(resource.toURI().toString(), x);
        }
    }
    
    public static XSLInfo parse(InputSource source) throws XSLInfoParsingException {
        Handler handler;
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            handler = new Handler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(source);
            return handler.getInfo();
        } catch(IOException x) {
            throw new XSLInfoParsingException(source.getSystemId(), x);
        } catch(SAXException x) {
            throw new XSLInfoParsingException(source.getSystemId(), x);
        }
    }
    
    static class Handler extends DefaultHandler {
        
        private int level;
        private XSLInfo xslInfo;
        
        public XSLInfo getInfo() {
            return xslInfo;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            level++;    
            if(level == 1) {
                if(uri.equals(XMLNS_XSL) && localName.equals("stylesheet")) {
                    xslInfo = new XSLInfo();
                }
            } else if(level == 2 && xslInfo != null) {
                if(uri.equals(XMLNS_XSL)) {
                    if(localName.equals("template")) {
                        String match = attributes.getValue("match");
                        String name = attributes.getValue("name");
                        if(name != null || match != null) {
                            XSLTemplateInfo info = new XSLTemplateInfo(match, name);
                            xslInfo.addTemplate(info);
                        }
                    } else if(localName.equals("include")) {
                        String href = attributes.getValue("href");
                        xslInfo.addInclude(href);
                    } else if(localName.equals("import")) {
                        String href = attributes.getValue("href");
                        xslInfo.addImport(href);
                    }
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            level--;
        }
        
    }
    
}