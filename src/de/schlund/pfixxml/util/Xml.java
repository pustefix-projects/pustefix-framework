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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Category;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.output.SaxonOutputKeys;
import com.icl.saxon.tinytree.TinyDocumentImpl;

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
    
    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder result;
        try {
            result = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("createDocumentBuilder failed", e);
        }
        result.setErrorHandler(ERROR_HANDLER);
        return result;
    }

    public static Document createDocument() {
        return createDocumentBuilder().newDocument();       
    }

    //-- parse immutable
    
    public static Document parseString(String str) throws TransformerException {
        SAXSource src = new SAXSource(createXMLReader(), new InputSource(new StringReader(str)));
        return parse(src);
    }

    /**
     * Convert the document implementation which is used for write-access 
     * by {@link SPDocument} to the document implementation which is used 
     * by the XSLTProcessor. Note: Currently we convert here from a mutable
     * DOM implementation to an imutable TinyTree(saxon).
     * @param doc the document as source for conversion(mostly a Node implementation
     * when using xerces)
     * @return a document as result of conversion(currently saxons TinyDocumentImpl)
     * @throws Exception on all errors 
     */
    public static Document parse(Document doc) {
        if (doc instanceof TinyDocumentImpl) {
            return doc;
        } else {
            DOMSource domsrc = new DOMSource(doc);
            try {
                return parse(domsrc);
            } catch (TransformerException e) {
                throw new RuntimeException("a dom tree is always well-formed xml", e);
            }
        }
    }

    /**
     * Create a document from a sourcefile in the filesystem. 
     * @param path the path to the source file in the filesystem
     * @return the created document(currenly saxons TinyDocumentImpl)
     * @throws TransformerException on errors
     */
    public static Document parse(File file) throws TransformerException {
        SAXSource src = new SAXSource(createXMLReader(), new InputSource(toUri(file)));
        return parse(src);
    }

    private static String toUri(File file) {
        // TODO: file.toURI returns single-slash.uri ...
        return "file://" + file.getAbsolutePath();
    }

    public static Document parse(Source input) throws  TransformerException, TransformerConfigurationException {
        try {
            Transformer trans  = Xslt.createIdentityTransformer();
            DOMResult   result = new DOMResult();
            trans.transform(input, result);
            return (TinyDocumentImpl) result.getNode();
        } catch (XPathException e) {
            StringBuffer sb = new StringBuffer();
            sb.append("TransformerException in xmlObjectFromDisc!\n");
            sb.append("Path: ").append(input.getSystemId()).append("\n");
            sb.append("Message and Location: ").append(e.getMessage()).append("\n");
            Throwable cause = e.getException();
            sb.append("Cause: ").append((cause != null) ? cause.getMessage() : "none").append("\n");
            CAT.error(sb.toString());
            throw e;
        }
    }

    //-- parse mutable
    
    public static Document parseStringMutable(String text) throws SAXException {
        try {
            return parseMutable(new InputSource(new StringReader(text)));        
        } catch (IOException e) {
            throw new RuntimeException("unexpected ioexception while reading from memory", e);
        }
    }
    
    public static Document parseMutable(File file) throws IOException, SAXException {
        if (file.isDirectory()) { 
            // otherwise, I get obscure content-not-allowed-here exceptions 
            throw new IOException("expected file, got directory: " + file);
        }
        return parseMutable(new InputSource(toUri(file)));
    }
    
    public static Document parseMutable(String filename) throws IOException, SAXException {
        return parseMutable(new File(filename));
    }
    
    public static Document parseMutable(InputStream src) throws IOException, SAXException {
        return parseMutable(new InputSource(src));
    }
    
    public static Document parseMutable(InputSource src) throws IOException, SAXException {
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

    public static void serialize(Node node, OutputStream dest, boolean pp, boolean decl) throws IOException {
        doSerialize(node, dest, pp, decl);
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
    
    private static final String ENCODING = "ISO-8859-1";
    
    /**
     * @param pp pretty print
     */
    private static void doSerialize(Node node, Object dest, boolean pp, boolean decl) throws IOException {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'null' is not allowed here! "
                                               + "Can't serialize a null node!");
        }
        Transformer t;
        Result result;
        Throwable cause;
        DOMSource src;
        
        // TODO: remove special cases
        if (node instanceof Text) {
            write(((Text) node).getData(), dest);
            return; 
        } else if (node instanceof Comment) {
            write("<!--" + ((Comment) node).getData() + "-->", dest);
            return;
        }

        if (!(node instanceof Document) && !(node instanceof Element)) {
            throw new IllegalArgumentException("unsupported node type: " + node.getClass());
        }
        if (pp) {
            t = Xslt.createPrettyPrinter();
        } else {
            t = Xslt.createIdentityTransformer();
        }
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, decl? "no" : "yes");
        if (decl) {
            t.setOutputProperty(OutputKeys.ENCODING, ENCODING);
        } else {
            // don't set encoding, I'd force an xml decl by setting it.
        }
        t.setOutputProperty(OutputKeys.INDENT, pp? "yes" : "no");
        t.setOutputProperty(SaxonOutputKeys.INDENT_SPACES, "2");
        src = new DOMSource(wrap(node));
        if (dest instanceof Writer) {
            result = new StreamResult((Writer) dest);
        } else if (dest instanceof OutputStream) {
            result = new StreamResult((OutputStream) dest);
        } else {
            throw new RuntimeException("Only Writer or OutputStreams allowed: " + dest.getClass());
        }
        try {
            t.transform(src, result);
        } catch (TransformerException e) {
            cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new RuntimeException("unexpected problem with identity transformer", e);
            }
        }
    }
    
    private static void write(String str, Object dest) throws IOException {
        if (dest instanceof Writer) {
            ((Writer) dest).write(str);
        } else {
            ((OutputStream) dest).write(str.getBytes(ENCODING));
        }
    }
    
    private static Document wrap(Node node) {
        // ugly hack to work-around saxon limitation: 6.5.3 cannot run xslt on sub-trees:
        // solved in 7.7: http://saxon.sourceforge.net/saxon7.7/changes.html (see 'jaxp changes')
        
        // TODO: implicit namespace attributes in tiny-tree nodes might vanish
        Document doc;
        
        if (node instanceof Document) {
            doc = (Document) node;
        } else {
            doc = Xml.createDocument();
            doc.appendChild(doc.importNode(node, true));
        }
        return doc;
    }
    
    public static String stripElement(String ele) {
        int start;
        int end;
        
        if (ele.startsWith("<?")) {
            throw new IllegalArgumentException(ele);
        }
        if (!ele.startsWith("<")) {
            throw new IllegalArgumentException(ele);
        }
        if (!ele.endsWith(">")) {
            throw new IllegalArgumentException(ele);
        }
        if (ele.endsWith("/>")) {
            return "";
        }
        start = ele.indexOf('>');
        if (start == -1) {
            throw new IllegalArgumentException(ele);
        }
        end = ele.lastIndexOf('<');
        if (end == -1) {
            throw new IllegalArgumentException(ele);
        }
        return ele.substring(start + 1, end);
    }
}
