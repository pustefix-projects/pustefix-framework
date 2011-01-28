package org.pustefixframework.agent;

import java.io.File;
import java.io.FileInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class POMParser {
    
    public POMInfo parse(File file) throws Exception {
        FileInputStream fis = null;
        ParsingHandler handler = new ParsingHandler();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
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
    
    public static void main(String[] args) throws Exception {
        File file = new File("/data/checkouts/pustefix.svn.sourceforge.net/pustefix-0.16.x/pom.xml");
        POMParser parser = new POMParser();
        long t1 = System.currentTimeMillis();
        for(int i = 0; i<10000; i++) {
            parser.parse(file);
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);
    }
    
}
