package de.schlund.lucefix.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Class creating Lucenedocuments from a Pfx-includefile
 * <p>
 * Every PfxDocument has the following fields:
 * <ul>
 *  <li><code>path</code> - filepath/partname/productname</li>
 *  <li><code>lastTouch</code> - keyword field</li>
 *  <li><code>comments</code> - </li>
 *  <li><code>tags</code></li>
 *  <li><code>attribkeys</code></li>
 *  <li><code>attribvals</code></li>
 *  <li><code>contents</code> - the full content as a readerfield</li>
 * </ul>
 * </p>
 * @author schuppi
 * @date Jun 7, 2005
 */
public class PfxDocument {
    
   
//    public static Document[] getDocumentsFromFile(File f) throws FileNotFoundException, IOException, SAXException{
//        //saxparser auf das ding loslassen
//        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
//        IncludeFileHandler handler = new IncludeFileHandler(f.getAbsolutePath(),f.lastModified());
//        xmlreader.setContentHandler(handler);
//        xmlreader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
//        xmlreader.parse(new InputSource(new FileReader(f)));
//        return handler.getScannedDocuments();
//    }

    public static Collection getDocumentsFromFileAsCollection(File f) throws FileNotFoundException, IOException, SAXException{
        //saxparser auf das ding loslassen
        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        IncludeFileHandler handler = new IncludeFileHandler(f.getAbsolutePath(),f.lastModified());
        xmlreader.setContentHandler(handler);
        xmlreader.setDTDHandler(handler);
        xmlreader.setEntityResolver(handler);
        xmlreader.setErrorHandler(handler);
        xmlreader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        try{
            xmlreader.parse(new InputSource(new FileReader(f)));
        }catch(Exception e){
            System.out.println("bad xml: " + f);
//            e.printStackTrace();
            return new Vector();
        }
        return handler.getScannedDocumentsAsVector();
    }
    

    
}
