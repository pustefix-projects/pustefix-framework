package de.schlund.pfixxml;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixxml.resources.Resource;

public class IncludePartsInfoParser {
 
    public static IncludePartsInfo parse(Resource resource) throws IncludePartsInfoParsingException {
        InputSource in = new InputSource();
        try {
            in.setByteStream(resource.getInputStream());
            in.setSystemId(resource.toURI().toASCIIString());
            IncludePartsInfo info = parse(resource);
            info.setLastMod(resource.lastModified());
            return info;
        } catch(IOException x) {
            throw new IncludePartsInfoParsingException(resource.toURI().toString(), x);
        }
    }
    
    public static IncludePartsInfo parse(InputSource source) throws IncludePartsInfoParsingException {
        Set<String> parts = null;
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            Handler handler = new Handler();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(source);
            parts = handler.getParts();
        } catch(IOException x) {
            throw new IncludePartsInfoParsingException(source.getSystemId(), x);
        } catch(SAXException x) {
            throw new IncludePartsInfoParsingException(source.getSystemId(), x);
        }
        IncludePartsInfo info = new IncludePartsInfo();
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
