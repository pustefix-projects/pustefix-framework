package de.schlund.pfixxml.testenv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Category;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.targets.TraxXSLTProcessor;


/**
 * Class RecordManager implements the singleton pattern. Its purpose is to log
 * user inputs encapsulated in a {@link PfixServletRequest} and the corresponding output 
 * encapsulated in a {@link ResultDocument} into files on the filesystem. 
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public final class RecordManager {

    //~ Instance/static variables ..................................................................

    private static Category      CAT                = Category.getInstance(RecordManager.class.getName());
    private static RecordManager theInstance        = new RecordManager();
    private static final String  DEFAULT_STYLESHEET = "default.xsl";

    //~ Constructors ...............................................................................

    private RecordManager() {
    }

    //~ Methods ....................................................................................

    /**
     * The getInstance method of this singleton.
     * @return the instance
     */
    public static final RecordManager getInstance() {
        return theInstance;
    }

    /**
     * Start recording. All requests and results will be logged to file.
     * @param count a number which will be appended to the filename used to
     * determine the order.
     * @param the URI of the PfixServletRequest.
     * @param logdir a directory where all logfiles are written to
     * @param pfix_servlet_request the PfixServletRequest containing all GET- and POST-
     * parameters.
     * @param result_document the {@link SPDocument} containing the current state of business data 
     * @param session_id the sessionid the above PfixServletRequest belongs to. 
     * @throws RecordManagerException on all non-recoverable errors
     */
    public final void doRecord(int count, String logdir, String uri, 
                               PfixServletRequest pfix_servlet_request, SPDocument result_document, 
                               String session_id) throws RecordManagerException {
        
        /*String[] req_param_names = pfix_servlet_request.getRequestParamNames();
		System.out.println("=================" + uri + "==================="); 
		System.out.println("URI: " + uri);
        System.out.println("Query string : " + pfix_servlet_request.getQueryString());
        System.out.println("Scheme       : " + pfix_servlet_request.getScheme());
        System.out.println("Server name  : " + pfix_servlet_request.getServerName());
        System.out.println("Server port  : " + pfix_servlet_request.getServerPort());
        System.out.println("Servlet path : " + pfix_servlet_request.getServletPath());
        for (int i = 0; i < req_param_names.length; i++) {
            RequestParam[] values = pfix_servlet_request.getAllRequestParams(req_param_names[i]);
            for (int j = 0; j < values.length; j++) {
                System.out.println(req_param_names[i] + " = " + values[j].getValue());
            }
        }*/
        File file = new File(logdir);
        if (file.exists()) {
            if (file.isFile()) {
                throw new RecordManagerException(logdir + " already exists, but its a file!", null);
            } else if (file.isDirectory() && ! file.canRead()) {
                throw new RecordManagerException(logdir + " alreay exists, but its not readable!", 
                                                 null);
            }
        } else {
            boolean ok = file.mkdirs();
            if (! ok) {
                throw new RecordManagerException("Unable to create directory: " + file.getName(), 
                                                 null);
            }
        }
        Node     input_node      = doPFServletRequesttoXML(uri, pfix_servlet_request, 
                                                            count == 0 ? true : false);
        Node     output_node     = doSPDocumenttoXML(result_document);
        Node     stylesheet_node = doDefaultStylesheettoXML();
        Document doc             = new DocumentImpl();
        Element  step            = doc.createElement("step");
        step.setAttribute("hostname", pfix_servlet_request.getServerName());
        step.setAttribute("hostport", "" + pfix_servlet_request.getServerPort());
        doc.appendChild(step);
        Node imp1 = doc.importNode(input_node, true);
        step.appendChild(imp1);
        Node imp2 = doc.importNode(output_node, true);
        step.appendChild(imp2);
        Node imp3 = doc.importNode(stylesheet_node, true);
        step.appendChild(imp3);
        writeDocument(doc, logdir + "/" + "testdata." + count);
    }

	/**
	 * Generate XML from the PfixServletRequest
	 * @param uri the URI belonging to the PfixServletRequest
	 * @param pfreq the PfixServletRequest
	 * @param first flag determining if this is the first call
	 * @return a Node containing the generated XML
	 */
    private Node doPFServletRequesttoXML(String uri, PfixServletRequest pfreq, boolean first) {
        Document doc     = new DocumentImpl();
        Element  ele     = doc.createElement("request");
        String   new_uri = null;
        if (first) {
            new_uri = uri.substring(0, uri.indexOf(';'));
        } else {
            new_uri = uri.substring(0, uri.indexOf('=') + 1) + "[SESSION_ID]";
        }
        ele.setAttribute("uri", new_uri);
        String[] req_param_names = pfreq.getRequestParamNames();
        for (int i = 0; i < req_param_names.length; i++) {
            // we don't want to send the record parameter
            if (req_param_names[i].toUpperCase().equals("__RECORDMODE"))
                continue;
            String         name   = req_param_names[i];
            RequestParam[] values = pfreq.getAllRequestParams(name);
            for (int j = 0; j < values.length; j++) {
                Element e    = doc.createElement("param");
                Text    text = doc.createTextNode(values[j].getValue());
                e.setAttribute("name", name);
                e.appendChild(text);
                ele.appendChild(e);
            }
        }
        return ele;
    }
	/**
	 * Generate XML from the ResultDocument.
	 * @param the SPDocument containing the ResultDocument
	 * @return a Node containing the generated XML
	 */
    private Node doSPDocumenttoXML(SPDocument doc) {
        return doc.getDocument().getFirstChild();
    }

	/**
	 * Create XML containing default stylesheet usage
	 * @return a Node containing the generated XML
	 */
    private Node doDefaultStylesheettoXML() {
        Document doc  = new DocumentImpl();
        Element  ele  = doc.createElement("stylesheet");
        Text     text = doc.createTextNode(DEFAULT_STYLESHEET);
        ele.appendChild(text);
        return ele;
    }
	
	/**
	 * not used
	 */
	/*
    private Node getHostnameNode(String hostname) {
        Document doc  = new DocumentImpl();
        Element  ele  = doc.createElement("host");
        Text     text = doc.createTextNode(hostname);
        ele.appendChild(text);
        return ele;
    }
	*/
	
	/**
	 * Writes a XML document to a specified filename. Does nothing if the 
	 * file alreay exists.
	 * @param the Document to write
	 * @param the filename
	 * @throws RecordManagerException on all non-recoverable errors.
	 */
    private void writeDocument(Document doc, String filename) throws RecordManagerException {
        File file = new File(filename);
        if (file.exists()) {
            CAT.error("Trying to overwrite an existing file: " + file.getName()+ ". Returning...");
            return;
        }
        OutputFormat  out_format = new OutputFormat("xml", "ISO-8859-1", true);
        XMLSerializer ser = new XMLSerializer();
        ser.setOutputFormat(out_format);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        try {
            ser.setOutputCharStream(new FileWriter(file));
        } catch (IOException e) {
            throw new RecordManagerException("IOException occured during serialization!", e);
        }
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            throw new RecordManagerException("IOException occured during serialization!", e);
        }
    }
}