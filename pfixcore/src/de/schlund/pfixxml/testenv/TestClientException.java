package de.schlund.pfixxml.testenv;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Exception class for handling errors for {@link TestClient}
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestClientException extends Exception {

    //~ Instance/static variables ..................................................................

    private String    errorMessage = null;
    private Exception theCause = null;

    //~ Constructors ...............................................................................
    /**
     * Create a new TestClientException
     * @param error an error message
     * @param cause an exception which is the cause for this exception
     */
    public TestClientException(String error, Exception cause) {
        super(error);
        this.errorMessage = error;
        this.theCause     = cause;
    }

    //~ Methods ....................................................................................
    /**
     * Get the cause of this exception
     * @return the cause
     */
    public Exception getExceptionCause() {
        return theCause == null ? new Exception("unkown reason") : theCause;
    }

    /**
     * Get the error message
     * @return the message
     */
    public String getMessage() {
        return errorMessage == null ? "" : errorMessage;
    }
    
    public Document toXMLRepresentation() throws ParserConfigurationException {
        return createErrorTree(this);
    }
    
    private Document createErrorTree(TestClientException targetex)
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
            } else if (e instanceof TestClientException) {
                TestClientException tcex = (TestClientException) e;
                printEx(tcex.getExceptionCause(), doc, root);
            } else if (e instanceof TransformerException) {
                TransformerException trex = (TransformerException) e;
                insertErrInfo(error, "Location", trex.getLocationAsString());
                printEx(trex.getCause(), doc, root);
            } else if (e instanceof SAXException) {
                SAXException saxex = (SAXException) e;
                printEx(saxex.getException(), doc, root);
            }
        }

}