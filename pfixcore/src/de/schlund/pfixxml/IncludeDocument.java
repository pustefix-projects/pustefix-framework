package de.schlund.pfixxml;

import de.schlund.pfixxml.targets.TraxXSLTProcessor;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXParseException;


/**
 * IncludeDocument.java
 * 
 * 
 * Created: 20021031
 * 
 * @author <a href="mailto:haecker@schlund.de">Joerg Haecker</a>
 * 
 * 
 * This class encapsulates an include-module of the PUSTEFIX-system.
 * A IncludeDocument stores a Document created from a file. Currently
 * there are two types of Documents: mutable and immutable. The user
 * of this class must know which type he wants. 
 * Anymore various administrative data like modification time
 * of the file from which it is created from and more are stored.  
 */
public class IncludeDocument {

    //~ Instance/static variables ..................................................................

    private Document doc;
    // NOTE: here we want a XERCES-DocumentBuilderFactory
    private DocumentBuilderFactoryImpl docBuilderFactory;
    private long                       modTime = 0;
    private static String              INCPATH = "incpath";
    private static Category            CAT     = Category.getInstance(IncludeDocument.class.getName());

    //~ Constructors ...............................................................................

    /**
     * Constructor
     */
    public IncludeDocument() {
        docBuilderFactory = new DocumentBuilderFactoryImpl();
        if (! docBuilderFactory.isNamespaceAware())
            docBuilderFactory.setNamespaceAware(true);
        if (docBuilderFactory.isValidating())
            docBuilderFactory.setValidating(false);
    }

    //~ Methods ....................................................................................

    /**
     * Create the internal document.
     * @param path the path in the filesystem to create the document from.
     * @param mutable determine if the document is mutable or not. Any attempts
     * to modify an immutable document will cause an exception.
     */
    public void createDocument(String path, boolean mutable) throws Exception {
        File tmp = new File(path);
        modTime = tmp.lastModified();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        try {
            doc = docBuilder.parse(path);
        } catch (Exception ex) {
            if (ex instanceof SAXParseException) {
                SAXParseException saxpex = (SAXParseException) ex;
                StringBuffer      buf = new StringBuffer(100);
                buf.append("Caught SAXParseException!\n");
                buf.append("  Message  : ").append(saxpex.getMessage()).append("\n");
                buf.append("  SystemID : ").append(saxpex.getSystemId()).append("\n");
                buf.append("  Line     : ").append(saxpex.getLineNumber()).append("\n");
                buf.append("  Column   : ").append(saxpex.getColumnNumber()).append("\n");
                System.out.println(buf.toString());
                CAT.error(buf.toString());
            }
            throw ex;
        }
        Element rootElement = doc.getDocumentElement();
        rootElement.setAttribute(INCPATH, path);
        if (! mutable)
            doc = TraxXSLTProcessor.getInstance().xmlObjectFromDocument(doc);
    }

    public Document getDocument() {
        return doc;
    }

    public long getModTime() {
        return modTime;
    }

    public void resetModTime() {
        modTime -= 1l;
    }
}