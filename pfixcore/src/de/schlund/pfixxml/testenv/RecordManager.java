package de.schlund.pfixxml.testenv;

import de.schlund.pfixxml.*;
import de.schlund.pfixxml.serverutil.*;

import java.io.*;

import javax.servlet.http.*;

import javax.xml.parsers.*;

import org.apache.log4j.*;

import org.apache.xerces.dom.*;

import org.apache.xml.serialize.*;

import org.w3c.dom.*;


/**
 * The purpose of this class is to log
 * user inputs encapsulated in a {@link PfixServletRequest} and the corresponding output 
 * encapsulated in a {@link ResultDocument} into files on the filesystem. 
 * This class should not instantiated directly, use the {@link RecordManagerFactory} 
 * instead.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public final class RecordManager {

    //~ Instance/static variables ..................................................................

    private static Category               CAT                       = Category.getInstance(RecordManager.class.getName());
    private static DocumentBuilderFactory dbfac                     = DocumentBuilderFactory.newInstance();
    private static final String           SESS_RECORDMODE_DIR       = "__RECORDMODE__";
    private static final String           SESS_RECORD_COUNTER       = "__RECORDCOUNT__";
    private static final String           ATTR_RECORDDIR            = "record_dir";
    private static final String           REQ_PARAM_KEY_RECMODE     = "__recordmode";
    private static final String           REQ_PARAM_VAL_RECMODE_OFF = "0";
    private static final String           XML_PARAM_VAL_RECORDALLOW = "true";
    private static final String           DEFAULT_STYLESHEET        = "default.xsl";

    /** store the basic directory for recording here */
    private String recordBaseDir = null;

    /** store the flag if recording is allowed here */
    private boolean recordAllowed = false;

    //~ Initializers ...............................................................................

    static {
        dbfac.setNamespaceAware(true);
        dbfac.setValidating(false);
    }

    //~ Constructors ...............................................................................

    /**
     * Create a new RecordManager
     * @param path to file with configuration data. Mostly this
     * is the path to the dependeny configuration file in your project.
     */
    RecordManager(String depxml) throws Exception {
        if (CAT.isDebugEnabled()) {
            CAT.debug(this.getClass().getName() + " initializing");
        }
        DocumentBuilder db  = dbfac.newDocumentBuilder();
        Document        doc = db.parse(depxml);
        getConfigFromXML(doc);
        debug("RecordManager constructor end");
    }

    //~ Methods ....................................................................................

    /**
     * This method is called for every request. If the prerequisites met this
     * request and its corresponding output will be written to file. 
     * @param preq the current servlet request object
     * @param resp the current servlet response object
     * @param resdoc the output corresponding to the servlet request
     * @param session the current session for storing and reading data stored in the session
     * @param cUtil util needed for accessing sessiondata.
     */
    public final void tryRecord(PfixServletRequest preq, HttpServletResponse resp, 
                                SPDocument resdoc, HttpSession session, ContainerUtil cutil)
                         throws Exception {
        boolean recording_enabled = false;
        if (! recordAllowed) {
            if (CAT.isDebugEnabled()) {
                CAT.debug("Recordmode is not allowed for you! Go away!");
            }
            return;
        } else {
            debug("Start tryRecord");
            debugSession(session, cutil, "Start tryRecord");
            RequestParam param = preq.getRequestParam(REQ_PARAM_KEY_RECMODE);
            if (CAT.isDebugEnabled()) {
                CAT.debug("param: " + ((param == null) ? "null" : param.getValue().toString()));
            }
            if (param != null && param.getValue() != null) {
                if (param.getValue().equals(REQ_PARAM_VAL_RECMODE_OFF)
                    || param.getValue().equals("")) {
                    // users wants to turn recordmode off
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("Turning record mode OFF");
                    }
                    cutil.setSessionValue(session, SESS_RECORDMODE_DIR, null);
                    cutil.setSessionValue(session, SESS_RECORD_COUNTER, null);
                } else {
                    // users wants to turm recordmode on
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("Turning record mode ON");
                    }
                    String record_testcase_name = param.getValue();
                    String newname = tryFindNewDir(record_testcase_name, recordBaseDir);
                    cutil.setSessionValue(session, SESS_RECORDMODE_DIR, newname);
                    cutil.setSessionValue(session, SESS_RECORD_COUNTER, new Integer(0));
                }
            } else {
                // param == null-> We are in record mode or not
                if (CAT.isDebugEnabled()) {
                    CAT.debug("No parameter found");
                }
                // Look in the session if user is in record mode.
                String dir = (String) cutil.getSessionValue(session, SESS_RECORDMODE_DIR);
                recording_enabled = (dir == null || dir.equals(REQ_PARAM_VAL_RECMODE_OFF)
                                    || dir.equals("")) ? false : true;
                if (recording_enabled) {
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("We are in record mode");
                    }
                    int count = ((Integer) cutil.getSessionValue(session, SESS_RECORD_COUNTER)).intValue();
                    count++;
                    cutil.setSessionValue(session, SESS_RECORD_COUNTER, new Integer(count));
                    doRecord(count, recordBaseDir, dir, preq.getRequestURI(resp), preq, resdoc);
                } else {
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("Request has nothing to do with recording");
                    }
                }
            }
        }
        debug("End tryRecord");
        debugSession(session, cutil, "End tryRecord");
    }

    /**
     * Determine if recordmode is allowed
     * @return true if recordmode allowed, else false
     */
    public final boolean isRecordmodeAllowed() {
        return recordAllowed;
    }

    /**
     * Get the base directory for recorded files
     * @return the directory name 
     */
    public final String getRecordmodeBaseDir() {
        return recordBaseDir;
    }

    /**
     * Get the name of the current recorded testcase
     * @param session containing the name of the current testcase
     * @param cutil util for accessing session data
     * @return the name of the testcase
     */
    public final String getTestcaseName(HttpSession session, ContainerUtil cutil) {
        String name = (String) cutil.getSessionValue(session, SESS_RECORDMODE_DIR);
        return name;
    }

    private String tryFindNewDir(String testcasename, String basedir) throws RecordManagerException {
        File file = new File(basedir + "/" + testcasename);
        if (! file.exists()) {
            boolean ok = file.mkdirs();
            if (! ok) {
                throw new RecordManagerException("Unable to create dir:" + basedir + "/"
                                                 + testcasename, null);
            }
        } else {
            if (CAT.isDebugEnabled()) {
                CAT.debug(basedir + "/" + testcasename + " exists!!!!!!");
            }
            int    i       = 0;
            String newname = null;
            while (file.exists()) {
                String suffix = "_" + i++;
                if (CAT.isDebugEnabled()) {
                    CAT.debug(file.getName() + " exists. Trying " + basedir + "/" + testcasename
                              + suffix);
                }
                newname = testcasename + suffix;
                file    = new File(basedir + "/" + testcasename + suffix);
            }
            boolean ok = file.mkdirs();
            if (! ok) {
                throw new RecordManagerException("Unable to create dir:" + basedir + "/"
                                                 + testcasename, null);
            }
            return newname;
        }
        return testcasename;
    }

    /** analyze configuration */
    private void getConfigFromXML(Document doc) throws Exception {
        Element root = doc.getDocumentElement();
        String  dir = root.getAttribute(ATTR_RECORDDIR);
        if (dir == null || dir.equals("")) {
            CAT.warn("Unable to find recording directory! Setting record mode allowed to false!");
            recordBaseDir = null;
            recordAllowed = false;
            return;
        } else {
            recordBaseDir = dir;
            recordAllowed = true;
        }
    }

    private void debug(String msg) {
        if (CAT.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            if (msg == null) {
                msg = "";
            }
            sb.append("\n").append(this.getClass().getName() + " status: ").append("(").append(msg).append(
                    ")").append("\n");
            sb.append("  recordDir          = " + recordBaseDir).append("\n");
            sb.append("  recordAllowed      = " + recordAllowed).append("\n");
            CAT.debug(sb.toString());
        }
    }

    private void debugSession(HttpSession session, ContainerUtil cutil, String msg) {
        if (CAT.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("\n").append(this.getClass().getName() + " session status: ").append("(").append(
                    msg).append(")").append("\n");
            sb.append("  SESS_RECORD_DIR   = "
                      + cutil.getSessionValue(session, SESS_RECORDMODE_DIR)).append("\n");
            Integer integer = (Integer) cutil.getSessionValue(session, SESS_RECORD_COUNTER);
            sb.append("  SESS_RECORD_COUNT = "
                      + ((integer == null) ? "null" : ("" + integer.intValue()))).append("\n");
            CAT.debug(sb.toString());
        }
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
     * @throws RecordManagerException on all non-recoverable errors
     */
    private final void doRecord(int count, String basedir, String logdir, String uri, 
                                PfixServletRequest pfix_servlet_request, SPDocument result_document)
                         throws RecordManagerException {
        if (CAT.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer(512);
            sb.append("\nRecordManager#doRecord current PfixServletRequest: \n");
            sb.append("URI: " + uri).append("\n");
            sb.append("Query string : " + pfix_servlet_request.getQueryString()).append("\n");
            sb.append("Scheme       : " + pfix_servlet_request.getScheme()).append("\n");
            sb.append("Server name  : " + pfix_servlet_request.getServerName()).append("\n");
            sb.append("Server port  : " + pfix_servlet_request.getServerPort()).append("\n");
            sb.append("Servlet path : " + pfix_servlet_request.getServletPath()).append("\n");
            String[] req_param_names = pfix_servlet_request.getRequestParamNames();
            for (int i = 0; i < req_param_names.length; i++) {
                RequestParam[] values = pfix_servlet_request.getAllRequestParams(req_param_names[i]);
                for (int j = 0; j < values.length; j++) {
                    sb.append(req_param_names[i] + " = " + values[j].getValue()).append("\n");
                }
            }
            CAT.debug(sb.toString());
        }
        Node     input_node      = doPFServletRequesttoXML(uri, pfix_servlet_request);
        Node     output_node     = doSPDocumenttoXML(result_document);
        Node     stylesheet_node = doDefaultStylesheettoXML();
        Document doc             = new DocumentImpl();
        Element  step            = doc.createElement("step");
        doc.appendChild(step);
        Node imp1 = doc.importNode(input_node, true);
        step.appendChild(imp1);
        Node imp2 = doc.importNode(output_node, true);
        step.appendChild(imp2);
        Node imp3 = doc.importNode(stylesheet_node, true);
        step.appendChild(imp3);
        writeDocument(doc, basedir + "/" + logdir + "/" + "testdata." + count);
    }

    /**
     * Generate XML from the PfixServletRequest
     * @param uri the URI belonging to the PfixServletRequest
     * @param pfreq the PfixServletRequest
     * @return a Node containing the generated XML
     */
    private Node doPFServletRequesttoXML(String uri, PfixServletRequest pfreq) {
        Document doc      = new DocumentImpl();
        Element  ele      = doc.createElement("request");
        String   new_uri  = null;
        new_uri = uri.substring(0, uri.indexOf(';'));
        Element ele_uri  = doc.createElement("uri");
        Text    text_uri = doc.createTextNode(new_uri);
        ele_uri.appendChild(text_uri);
        ele.appendChild(ele_uri);
        Element ele_hostname  = doc.createElement("hostname");
        Text    text_hostname = doc.createTextNode(pfreq.getServerName());
        ele_hostname.appendChild(text_hostname);
        ele.appendChild(ele_hostname);
        Element ele_port  = doc.createElement("port");
        Text    text_port = doc.createTextNode("" + pfreq.getServerPort());
        ele_port.appendChild(text_port);
        ele.appendChild(ele_port);
        Element ele_proto  = doc.createElement("proto");
        Text    text_proto = doc.createTextNode(pfreq.getScheme());
        ele_proto.appendChild(text_proto);
        ele.appendChild(ele_proto);
        Element  ele_params      = doc.createElement("params");
        String[] req_param_names = pfreq.getRequestParamNames();
        for (int i = 0; i < req_param_names.length; i++) {
            // we don't want to send the record parameter
            if (req_param_names[i].toUpperCase().equals(REQ_PARAM_KEY_RECMODE))
                continue;
            String         name   = req_param_names[i];
            RequestParam[] values = pfreq.getAllRequestParams(name);
            for (int j = 0; j < values.length; j++) {
                Element e    = doc.createElement("param");
                Text    text = doc.createTextNode(values[j].getValue());
                e.setAttribute("name", name);
                e.appendChild(text);
                ele_params.appendChild(e);
            }
        }
        ele.appendChild(ele_params);
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
     * Writes a XML document to a specified filename. Does nothing if the 
     * file alreay exists.
     * @param the Document to write
     * @param the target filename
     * @throws RecordManagerException on all non-recoverable errors.
     */
    private void writeDocument(Document doc, String filename) throws RecordManagerException {
        if (CAT.isDebugEnabled()) {
            CAT.debug("Writing file: " + filename);
        }
        File file = new File(filename);
        int  i = 0;
        while (file.exists()) {
            String new_filename = filename + "_" + i++;
            if (CAT.isInfoEnabled()) {
                CAT.info(file.getName() + " exists. Trying " + new_filename);
            }
            file = new File(new_filename);
        }
        OutputFormat out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        FileOutputStream out_stream;
        try {
            out_stream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RecordManagerException("IOException occured during serialization!: " + file, 
                                             e);
        }
        XMLSerializer ser = new XMLSerializer(out_stream, out_format);
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            throw new RecordManagerException("IOException occured during serialization!: " + file, 
                                             e);
        }
    }
}