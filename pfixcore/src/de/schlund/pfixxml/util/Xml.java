/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/
package de.schlund.pfixxml.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Category;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public final class Xml {
    private static final Category               CAT     = Category.getInstance(Xml.class.getName());
    private static final DocumentBuilderFactory factory = createDocumentBuilderFactory();
    
    //-- this is where you configure the xml parser:
    
    public static XMLReader createXMLReader() {
        XMLReader reader;
        reader = new SAXParser();
        reader.setErrorHandler(ERROR_HANDLER);
        return reader;
    }
    
    public static Document createDocument() {
        return createDocumentBuilder().newDocument();       
    }
    
    public static Document parseString(String text) throws SAXException {
        try {
            return parse(new InputSource(new StringReader(text)));        
        } catch (IOException e) {
            throw new RuntimeException("unexpected ioexception while reading from memory", e);
        }
    }
    
    public static Document parse(File file) throws IOException, SAXException {
        return parse(file.getPath());
    }
    
    public static Document parse(String filename) throws IOException, SAXException {
        return parse(new InputSource(filename));
    }
    
    public static Document parse(InputStream src) throws IOException, SAXException {
        return parse(new InputSource(src));
    }
    
    public static Document parse(InputSource src) throws IOException, SAXException {
        try {
            return createDocumentBuilder().parse(src);
        } catch (SAXParseException e) {
            StringBuffer buf = new StringBuffer(100);
            buf.append("Caught SAXParseException!\n");
            buf.append("  Message  : ").append(e.getMessage()).append("\n");
            buf.append("  SystemID : ").append(e.getSystemId()).append("\n");
            buf.append("  Line     : ").append(e.getLineNumber()).append("\n");
            buf.append("  Column   : ").append(e.getColumnNumber()).append("\n");
            CAT.error(buf.toString(), e);
            throw e;
        } catch (SAXException e) {
            StringBuffer buf = new StringBuffer(100);
            buf.append("Caught SAXException!\n");
            buf.append("  Message  : ").append(e.getMessage()).append("\n");
            buf.append("  SystemID : ").append(src.getSystemId()).append("\n");
            CAT.error(buf.toString(), e);
            throw e;
        } catch (IOException e) {
            StringBuffer buf = new StringBuffer(100);
            buf.append("Caught IOException!\n");
            buf.append("  Message  : ").append(e.getMessage()).append("\n");
            buf.append("  SystemID : ").append(src.getSystemId()).append("\n");
            CAT.error(buf.toString(), e);
            throw e;
        }
    }

    
    
    //-- serialization
    
    /**
     * @param pp pretty print
     */
    public static String serialize(Node node, boolean pp, boolean decl) {
        StringWriter dest;
        
        dest = new StringWriter();
        try {
            doSerialize(node, dest, pp, decl);
        } catch (IOException e) {
            throw new RuntimeException("unexpected IOException while writing to memory", e);
        }
        return dest.getBuffer().toString();        
    }
    
    /**
     * @param pp pretty print
     */
    public static void serialize(Node node, File file, boolean pp, boolean decl) throws IOException {
        serialize(node, file.getPath(), pp, decl);
    }

    /**
     * @param pp pretty print
     */
    public static void serialize(Node node, String filename, boolean pp, boolean decl) throws IOException {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'null' is not allowed here! "
                                               + "Can't serialize a null node to a file!");
        }
        if (filename == null || filename.equals("")) {
            throw new IllegalArgumentException("The parameter 'null' or '\"\"' is not allowed here! "
                                               + "Can't serialize a document to " + filename + "!");
        }
        doSerialize(node, new FileOutputStream(filename), pp, true);
    }


    

    // PRIVATE

    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory fact = new DocumentBuilderFactoryImpl();
        if (!fact.isNamespaceAware()) {
            fact.setNamespaceAware(true);
        }
        if (fact.isValidating()) {
            fact.setValidating(false);
        }
        return fact;
    }

    private static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder result;
        try {
            result = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("createDocumentBuilder failed", e);
        }
        result.setErrorHandler(ERROR_HANDLER);
        return result;
    }

    // make sure that output is not polluted by prinlns:
    private static final ErrorHandler ERROR_HANDLER = new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                report(exception);
            }
            
            public void fatalError(SAXParseException exception) throws SAXException {
                report(exception);
            }
            
            public void warning(SAXParseException exception) throws SAXException {
                report(exception);
            }
            
            private void report(SAXParseException exception) throws SAXException {
                CAT.error(exception.getSystemId() + ":" + exception.getLineNumber() + ":" 
                          + exception.getColumnNumber() + ":" + exception.getMessage());
                throw exception;
            }
        };
    
    /**
     * @param pp pretty print
     */
    private static void doSerialize(Node node, Object dest, boolean pp, boolean decl) throws IOException {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'null' is not allowed here! "
                                               + "Can't serialize a null node!");
        }
        
        OutputFormat format;
        XMLSerializer ser;
    
        format = new OutputFormat("xml", "ISO-8859-1", true);
        format.setOmitXMLDeclaration(!decl);
        format.setLineWidth(0); // no line-wrap
        if (pp) {
            format.setPreserveSpace(false);
            format.setIndent(2);
        } else {
            format.setPreserveSpace(true);
        }
        if (dest instanceof StringWriter) { // No FileWriter because encoding must be set.
                                            // Use FileOutputstreams instead 
            ser = new XMLSerializer((Writer) dest, format);
        } else if (dest instanceof FileOutputStream) {
            ser = new XMLSerializer((OutputStream) dest, format);
        } else {
            throw new RuntimeException("Only StringWriter and FileOutputStreams allowed! " +
                                       "(" + dest.getClass() +")");
        }

        if (node instanceof Document) {
            ser.serialize((Document) node);
        } else if (node instanceof DocumentFragment) {
            ser.serialize((DocumentFragment) node);
        } else if (node instanceof Element) {
            ser.serialize((Element) node);
        } else {
            throw new IllegalArgumentException("" + node);
        }
    }
}
