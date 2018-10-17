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
package org.pustefixframework.agent;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Fast SAX-based parser to get groupId, artifactId and version from POM.
 */
public class POMParser {
    
    public POMInfo parse(File file) throws Exception {
        
        FileInputStream fis = null;
        ParsingHandler handler = new ParsingHandler();
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            XMLReader xr = spf.newSAXParser().getXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            InputSource in = new InputSource();
            fis = new FileInputStream(file);
            in.setByteStream(fis);
            in.setSystemId(file.toURI().toASCIIString());
            xr.parse(in);
            
        } catch(AbortParsingException x) {
            //do nothing because parsing was regularly aborted
        } finally {
            if(fis != null) fis.close();
        } 
        if(handler.getPOMInfo().isComplete()) return handler.getPOMInfo();
        return null;
    }
    
    class ParsingHandler extends DefaultHandler {
        
        private int level;
        private StringBuilder content = new StringBuilder();
        private POMInfo pomInfo = new POMInfo();
        
        public POMInfo getPOMInfo() {
            return pomInfo;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
           level++;
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if(level == 2) {
                if(content == null) content = new StringBuilder();
                content.append(ch, start, length);
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(level == 2) {
                String str = content.toString().trim();
                if(localName.equals("groupId")) {
                    pomInfo.setGroupId(str);
                    if(pomInfo.isComplete()) throw new AbortParsingException();
                } else if(localName.equals("artifactId")) {
                    pomInfo.setArtifactId(str);
                    if(pomInfo.isComplete()) throw new AbortParsingException();
                } else if(localName.equals("version")) {
                    pomInfo.setVersion(str);
                    if(pomInfo.isComplete()) throw new AbortParsingException();
                }
                content = null;
            }
            level--;
        }
        
    }
    
    class AbortParsingException extends SAXException {

        private static final long serialVersionUID = 8137076865306267682L;
        
    }
    
}