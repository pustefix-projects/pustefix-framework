package de.schlund.pfixxml;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixxml.resources.Resource;

public class IncludePartsInfoParser {

    private final static Logger LOG = Logger.getLogger(IncludePartsInfoParser.class);
    
    public static IncludePartsInfo parse(Resource resource) {
        Set<String> parts = null;
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            Handler handler = new Handler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            InputSource in = new InputSource();
            in.setByteStream(resource.getInputStream());
            in.setSystemId(resource.toURI().toASCIIString());
            xr.parse(in);
            parts = handler.getParts();
        } catch(Exception x) {
            LOG.warn("Error reading include parts: " + resource.toURI().toString(), x);
        }
        IncludePartsInfo info = new IncludePartsInfo();
        info.setLastMod(resource.lastModified());
        info.setParts(parts);
        return info;
    }
    
    static class Handler extends DefaultHandler {
        
        private int level;
        private boolean isIncludeParts;
        private Set<String> parts = new HashSet<String>();
        
        public Set<String> getParts() {
            return parts;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            level++;    
            if(level == 1) {
                if(localName.equals("include_parts")) {
                    isIncludeParts = true;
                }
            } else if(level == 2 && isIncludeParts) {
                if(localName.equals("part")) {
                    String part = attributes.getValue("name");
                    if(part != null) {
                        part = part.trim();
                        parts.add(part);
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
