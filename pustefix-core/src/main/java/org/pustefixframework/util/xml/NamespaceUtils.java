package org.pustefixframework.util.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

/**
 * Utitliy methods for XML namespace handling
 * 
 * @author mleidig@schlund.de
 *
 */
public class NamespaceUtils {

    private static Templates setNamespaceTemplates;
   
    /**
     * Set the default namespace of a document.
     * 
     * @param doc - the original document
     * @param namespace - the default namespace URI
     * @param schema - an optional schema hint/location for the default namespace
     * @return new namespace-aware document
     * @throws TransformerException
     */
    public static Document setNamespace(Document doc, String namespace, String schema) throws TransformerException {
        Templates templates = getSetNamespacesTemplates();
        Transformer transformer = templates.newTransformer();
        if(namespace != null) transformer.setParameter("namespace", namespace);
        if(schema != null) transformer.setParameter("schema", schema);
       
        Source src = new DOMSource(doc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //Don't use DOMResult because namespace declaration gets lost
        StreamResult res = new StreamResult(out);
        transformer.transform(src, res);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder().parse(in);
        } catch(Exception x) {
            throw new TransformerException("Can't create new document", x);
        }
    }
    
    private static synchronized Templates getSetNamespacesTemplates() throws TransformerException {
        if(setNamespaceTemplates == null) {
            TransformerFactory tf = TransformerFactory.newInstance();
            InputStream in = NamespaceUtils.class.getResourceAsStream("set-namespace.xsl");
            Source src = new StreamSource(in);
            setNamespaceTemplates = tf.newTemplates(src);
        }
        return setNamespaceTemplates;
    }
    
}
