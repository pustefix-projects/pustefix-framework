package de.schlund.pfixxml.targets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Exception subclass which represents errors during generation of a target.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TargetGenerationException extends Exception {

    private String targetkey;
    private Throwable cause;

    public TargetGenerationException() {
        super();
    }

    public TargetGenerationException(String msg) {
        super(msg);
    }

    public TargetGenerationException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    public TargetGenerationException(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getNestedException() {
        return cause;
    }

    /**
     * @return
     */
    public String getTargetkey() {
        return targetkey;
    }

    /**
     * @param string
     */
    public void setTargetkey(String key) {
        targetkey = key;
    }

    public Document toXMLRepresentation() throws ParserConfigurationException, FactoryConfigurationError {
        return createErrorTree(this);
    }

    private Document createErrorTree(TargetGenerationException targetex)
        throws ParserConfigurationException {
        DocumentBuilder docbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docbuilder.newDocument();
        Element e0 = doc.createElement("error_message");
        doc.appendChild(e0);
        printEx(targetex, doc, e0);
        return doc;
    }

    private void insertErrInfo(Element error, String key, String value) {
        Element info = error.getOwnerDocument().createElement("info");
        info.setAttribute("key", key);
        info.setAttribute("value", value);
        error.appendChild(info);
    }

    private void printEx(Throwable e, Document doc, Node root) {
        if (e == null) {
            return;
        }

        Element error = doc.createElement("error");
        root.appendChild(error);
        error.setAttribute("type", e.getClass().getName());
        insertErrInfo(error, "Message", e.getMessage());

        if (e instanceof SAXParseException) {
            SAXParseException sex = (SAXParseException) e;
            insertErrInfo(error, "Id", sex.getSystemId());
            insertErrInfo(error, "Line", "" + sex.getLineNumber());
            insertErrInfo(error, "Column", "" + sex.getColumnNumber());
            printEx(sex.getException(), doc, root);
        } else if (e instanceof TargetGenerationException) {
            TargetGenerationException tagex = (TargetGenerationException) e;
            insertErrInfo(error, "Key", tagex.getTargetkey());
            printEx(tagex.getNestedException(), doc, root);
        } else if (e instanceof TransformerException) {
            TransformerException trex = (TransformerException) e;
            insertErrInfo(error, "Location", trex.getLocationAsString());
            printEx(trex.getCause(), doc, root);
        } else if (e instanceof SAXException) {
            SAXException saxex = (SAXException) e;
            printEx(saxex.getException(), doc, root);
        }
    }

    public String toStringRepresentation() {
        StringBuffer sb = new StringBuffer();
        printEx(this, sb, " ");
        return sb.toString();
    }

    private void printEx(Throwable e, StringBuffer buf, String indent) {
        String br = "\n";
        if (e == null) {
            return;
        }
        if (e instanceof SAXParseException) {
            SAXParseException sex = (SAXParseException) e;
            buf.append("|").append(br);
            buf.append("|").append(indent).append("Type: ").append(sex.getClass().getName()).append("\n");
            buf.append("|").append(indent).append("Message: ").append(sex.getMessage()).append("\n");
            buf.append("|").append(indent).append("Id: ").append(sex.getSystemId()).append("\n");
            buf.append("|").append(indent).append("Line: ").append(sex.getLineNumber()).append("\n");
            buf.append("|").append(indent).append("Column: ").append(sex.getColumnNumber()).append("\n");
        } else if (e instanceof TargetGenerationException) {
            TargetGenerationException tgex = (TargetGenerationException) e;
            buf.append("|").append(br);
            buf.append("|").append(indent).append("Type: ").append(tgex.getClass().getName()).append("\n");
            buf.append("|").append(indent).append("Message: ").append(tgex.getMessage()).append("\n");
            buf.append("|").append(indent).append("Target: ").append(tgex.getTargetkey()).append("\n");
            printEx(tgex.getNestedException(), buf, indent + " ");
        } else if (e instanceof TransformerException) {
            TransformerException trex = (TransformerException) e;
            buf.append("|").append(br);
            buf.append("|").append(indent).append("Type: ").append(trex.getClass().getName()).append("\n");
            buf.append("|").append(indent).append("Message: ").append(trex.getMessage()).append("\n");
            printEx(trex.getCause(), buf, indent + " ");
        } else {
            buf.append("|").append(br);
            buf.append("|").append(indent).append("Type: ").append(e.getClass().getName()).append("\n");
            buf.append("|").append(indent).append("Message: ").append(e.getMessage()).append("\n");
        }
        //printEx(e, buf, indent + " ");
    }

}
