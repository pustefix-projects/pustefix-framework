package de.schlund.pfixxml.testenv;

import com.icl.saxon.Controller;
import com.icl.saxon.expr.NodeSetValue;
import com.icl.saxon.om.NodeEnumeration;

import de.schlund.pfixxml.targets.TraxXSLTProcessor;
import de.schlund.pfixxml.xpath.PFXPathEvaluator;

import gnu.getopt.Getopt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import org.apache.xerces.dom.DocumentImpl;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import org.apache.xpath.XPathAPI;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * TestClient is an application for testing business logic on
 * pustefix-based projects.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestClient {

    //~ Instance/static variables ..................................................................

    private static final String    XMLONLY_PARAM_KEY   = "__xmlonly";
    private static final String    XMLONLY_PARAM_VALUE = "1";
    private static final int       LOOP_COUNT          = 1;
    private static final String    GNU_DIFF = "diff -u";
    private String                 srcDir;
    private String                 tmpDir;
    private String                 styleDir;
    private HttpConnection         httpConnect;
    private DocumentBuilderFactory doc_factory;
    private long                   request_count       = 0;
    private String                 sessionId;
    private String                 hostName;
    private int                    hostPort;
    private boolean 			   modeQuiet = false;
	private boolean 			   modeVerbose = false;
    //~ Constructors ...............................................................................

    public TestClient() throws TestClientException {
        System.getProperties().put(TraxXSLTProcessor.DOCB_FAC_KEY, 
                                   TraxXSLTProcessor.DOCB_FAC_XERCES);
        System.getProperties().put(TraxXSLTProcessor.TRANS_FAC_KEY, 
                                   TraxXSLTProcessor.TRANS_FAC_SAXON);
        doc_factory = DocumentBuilderFactory.newInstance();
        doc_factory.setValidating(false);
        doc_factory.setNamespaceAware(true);
    }

    //~ Methods ....................................................................................

    public static void main(String[] args) {
        TestClient tc = null;
        try {
            tc = new TestClient();
            ArrayList files;
            if (tc.scanOptions(args)) {
                files = tc.getFiles();
            } else {
                tc.printUsage();
                return;
            }
            StepConfig[] config = tc.doPrepare(files);
            tc.initHttpConnection();
            for (int i = 0; i < LOOP_COUNT; i++) {
                System.out.println("################################################");
                tc.doTest(config);
            }
        } catch (TestClientException e) {
            System.out.println("\n**********************************************");
            System.out.println("ERROR in TestClient");
            System.out.println("Exception:");
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("Nested Exception:");
            System.out.println(e.getCause().getMessage());
            e.getCause().printStackTrace();
            System.out.println("\n**********************************************");
        }
    }

    private void doTest(StepConfig[] config) throws TestClientException {
    	boolean has_diff = false;
        for (int j = 0; j < config.length; j++) {
        	if(modeQuiet) {
        	} else if(modeVerbose) {
        		System.out.println("\nDoing step "+j+" from file :"+config[j].getFileName());
        	} else {
        		System.out.println("\nDoing step "+j);
        	}
        	
        	
            Document current_output_tree = null;
            try {
                current_output_tree = getResultFromFormInput(config[j].getRecordedInput());
            } catch (TestClientException e) {
                if (e.getCause() instanceof HttpRecoverableException) {
                    System.out.println("Uuuups...skipping...");
                    break;
                } else {
                    throw e;
                }
            }
            if(modeQuiet) {
            } else if(modeVerbose) {
            	System.out.println("  Transforming recorded and current output document...");
            } else {
            	System.out.println("  Transforming...");
            }
            Document tmp_rec       = doTransform(config[j].getRecordedOutput(), 
                                                 config[j].getStyleSheet());
            Document tmp_out       = doTransform(current_output_tree, config[j].getStyleSheet());
            String   tmp_fname_cur = tmpDir + "/_current" + j;
            String   tmp_fname_rec = tmpDir + "/_recorded" + j;
            writeDocument(tmp_out, tmp_fname_cur);
            writeDocument(tmp_rec, tmp_fname_rec);
            if(modeQuiet) {
            } else if(modeVerbose) {
            	System.out.println("  Diffing " + tmp_fname_cur + " and " + tmp_fname_rec + " ...");
            } else {
            	System.out.println("  Diffing...");
            }
            String msg = doDiff(tmp_fname_cur, tmp_fname_rec);
            if(modeQuiet) {
            } else if(modeVerbose) {
            	System.out.print("  Output:");
            } else {
            	System.out.print("  Output:");
            }
            if (msg == null || msg.equals("")) {
                msg = ":-)";
            } else {	
            	has_diff = true;
            }
            System.out.println("  " + msg);   
        }
        System.out.print("\n*** Resut: ***");
        System.out.println(has_diff ? ";-(" : ";-)" );
    }

    private StepConfig[] doPrepare(ArrayList files) throws TestClientException {
        DocumentBuilder doc_builder;
        StepConfig[]    config = new StepConfig[files.size()];
        try {
            doc_builder = doc_factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TestClientException("Could not get a DocumentBuilder!", e);
        }
        for (int i = 0; i < files.size(); i++) {
            File file = (File) files.get(i);
            if(modeQuiet) {
            } else if(modeVerbose) {
            	System.out.println("-------Analyzing " + file.getName() + "-------------");
            } else {
            }
            Document file_content = null;
            try {
                file_content = doc_builder.parse(file);
            } catch (SAXException e) {
            	if(e instanceof SAXParseException) {
            		SAXParseException saxex = (SAXParseException) e;
	            	throw new TestClientException("SAXException occured at line "+
	            									saxex.getLineNumber()+
	            									"in file "+file.getName(), e);
            	} else {
            		throw new TestClientException("SAXException occured", e);
            	}
            } catch (IOException e) {
            	throw new TestClientException("IOException occured", e);
            }
            Document recorded_out = new DocumentImpl();
            Document recorded_in = new DocumentImpl();
            Node     result_out  = null;
            try {
                Element e = (Element) XPathAPI.selectSingleNode(file_content, "/step");
                hostName = e.getAttribute("hostname");
                hostPort = Integer.parseInt(e.getAttribute("hostport"));
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            try {
                result_out = XPathAPI.selectSingleNode(file_content, "/step/formresult/");
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            Node result_in = null;
            try {
                result_in = XPathAPI.selectSingleNode(file_content, "/step/request/");
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            String stylessheet = null;
            try {
                stylessheet = XPathAPI.selectSingleNode(file_content, "/step/stylesheet").getFirstChild()
                        .getNodeValue();
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
            Node n_out = recorded_out.importNode(result_out, true);
            Node n_in = recorded_in.importNode(result_in, true);
            recorded_out.appendChild(n_out);
            recorded_in.appendChild(n_in);
            StepConfig conf = new StepConfig(((File)files.get(i)).getName(), recorded_in, recorded_out, stylessheet);
            config[i] = conf;
        }
        return config;
    }

    private Document doTransform(Document in, String stylesheet_name) throws TestClientException {
        TransformerFactory trans_fac     = TransformerFactory.newInstance();
        StreamSource       stream_source = new StreamSource(styleDir + "/" + stylesheet_name);
        Templates          templates     = null;
        try {
            templates = trans_fac.newTemplates(stream_source);
        } catch (TransformerConfigurationException e) {
            throw new TestClientException("TransformerConfigurationException occured!", e);
        }
        Transformer trafo = null;
        try {
            trafo = templates.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new TestClientException("TransformerConfigurationException occured!", e);
        }
        DOMSource dom_source = new DOMSource(in);
        DOMResult dom_result = new DOMResult();
        try {
            trafo.transform(dom_source, dom_result);
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        return (Document) dom_result.getNode();
    }

    private void writeDocument(Document doc, String path) throws TestClientException {
        XMLSerializer ser        = new XMLSerializer();
        OutputFormat  out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        ser.setOutputFormat(out_format);
        FileWriter file_writer = null;
        try {
            file_writer = new FileWriter(path);
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        ser.setOutputCharStream(file_writer);
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            throw new TestClientException("IOException ocuured!", e);
        }
    }

    private String documentToString(Document doc) throws Exception {
        XMLSerializer ser        = new XMLSerializer();
        OutputFormat  out_format = new OutputFormat("xml", "ISO-8859-1", true);
        out_format.setIndent(2);
        out_format.setPreserveSpace(false);
        ser.setOutputFormat(out_format);
        StringWriter string_writer = new StringWriter();
        ser.setOutputCharStream(string_writer);
        ser.serialize(doc);
        return string_writer.getBuffer().toString();
    }

    private String doDiff(String path1, String path2) throws TestClientException {
        String  diff    = GNU_DIFF + path1 + " " + path2;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(diff);
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String         s;
        StringBuffer   buf = new StringBuffer();
        try {
            while ((s = reader.readLine()) != null) {
                buf.append(s).append("\n");
            }
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new TestClientException("InterruptedException occured!", e);
        }
        return buf.toString();
    }

    private ArrayList getFiles() {
        File dir = new File(srcDir);
        if (! dir.isDirectory() || ! dir.canRead()) {
            System.out.println(srcDir + " is not a directory or not readable");
            return null;
        }
        File[]    all_files = dir.listFiles();
        ArrayList files = new ArrayList();
        for (int i = 0; i < all_files.length; i++) {
            if (all_files[i].canRead() && all_files[i].isFile()) {
                files.add(all_files[i]);
            }
        }
        Arrays.sort(files.toArray());
        if(modeQuiet) {
        } else if(modeVerbose) {
        	System.out.println("Reading files...");
        	for (int i = 0; i < files.size(); i++) {
            	System.out.println(((File) files.get(i)).getName());
        	}
        } else {
        	System.out.println("Reading files...");
        }
        return files;
    }

    private boolean scanOptions(String[] args) {
        Getopt getopt = new Getopt("TestClient", args, "d:t:s:qv");
        int    c = 0;
        while ((c = getopt.getopt()) != -1) {
            switch (c) {
                case 'd':
                    srcDir = getopt.getOptarg();
                    break;
                case 't':
                    tmpDir = getopt.getOptarg();
                    break;
                case 's':
                    styleDir = getopt.getOptarg();
                    break;
              	case 'q':
              		modeQuiet = true;
              		modeVerbose = false;
              		break;
              	case 'v':
              		modeVerbose = true;
              		modeQuiet = false;
              		break;
                default:
            }
        }

        if (srcDir == null || srcDir.equals("") || tmpDir == null || tmpDir.equals("")
            || styleDir == null || styleDir.equals("")) {
            return false;
        } else {
            return true;
        }
    }

    private void printUsage() {
        System.out.println("TestClient -d [recorded dir] -t [temporary dir] -s [stylesheet dir] -q -v");
    }

    private void initHttpConnection() throws TestClientException {
        try {
            httpConnect = new HttpConnection(hostName, hostPort);
            httpConnect.open();
        } catch (IOException e) {
            throw new TestClientException("Unable to reopen HTTP connection!", e);
        }
    }

    private Document getResultFromFormInput(Document form_data) throws TestClientException {
        if (! httpConnect.isOpen()) {
            System.out.println("HTTP Connection not open. Doing reinit...");
            initHttpConnection();
        }
        NodeList value = null;
        try {
            value = XPathAPI.selectNodeList(form_data, "/request");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        Element root = (Element) value.item(0);
        String  uri = root.getAttribute("uri");
        try {
            value = XPathAPI.selectNodeList(form_data, "/request/param");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        NameValuePair[] post_params = new NameValuePair[value.getLength() + 1];
        int             i = 0;
        for (int ii = 0; ii < value.getLength(); ii++) {
            Element e           = (Element) value.item(ii);
            String  param_name  = e.getAttribute("name");
            String  param_value = e.hasChildNodes() ? e.getFirstChild().getNodeValue() : "";
            post_params[i] = new NameValuePair(param_name, param_value);
            i++;
        }
        post_params[i] = new NameValuePair(XMLONLY_PARAM_KEY, XMLONLY_PARAM_VALUE);
        PostMethod post        = null;
        int        status_code = -1;
        if (! httpConnect.isOpen()) {
            initHttpConnection();
        }
        
        
        
        String uri_session = null;
        if (sessionId != null) {
            //It not the first request, we already have a session
            uri_session = uri.substring(0, uri.indexOf('=') + 1) + sessionId;
            post        = new PostMethod(uri_session);
            post.setFollowRedirects(false);
            post.addParameters(post_params);
            try {
                status_code = post.execute(new HttpState(), httpConnect);
                request_count++;
            } catch (HttpException e) {
                System.err.println("Error after " + request_count + " requests");
                throw new TestClientException("HTTPException occured!:" + status_code, e);
            }
             catch (IOException e) {
                throw new TestClientException("IOException occured!", e);
            }
        } else {
            // it's the first request
            post = new PostMethod(uri);
            post.setFollowRedirects(true);
            post.addParameters(post_params);
            try {
                status_code = post.execute(new HttpState(), httpConnect);
                request_count++;
            } catch (HttpException e) {
                System.err.println("Error after " + request_count + " requests");
                throw new TestClientException("HTTPException occured!:" + status_code, e);
            }
             catch (IOException e) {
                throw new TestClientException("IOException occured!", e);
            }
            try {
                uri = post.getURI().toString();
            } catch (URIException e) {
                throw new TestClientException("URIException occured!", e);
            }
            sessionId = uri.substring(uri.indexOf('=') + 1, uri.length());
            uri_session = uri.substring(0, uri.indexOf('=') + 1) + sessionId;
        }
        String request_body = null;
        try {
            request_body = post.getRequestBodyAsString();
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        
       	if(modeQuiet) {
        } else if(modeVerbose) {
        	System.out.println("  Doing HTTP-POST");
        	System.out.println("          URI=" + uri_session);
        	System.out.println("       PARAMS=" + request_body);
        	System.out.println("   StatusCode=" +status_code);
        } else {
        	System.out.println("  Doing HTTP-POST..."+status_code);
        } 
       
        if (status_code != HttpStatus.SC_OK) {
            String resp = post.getResponseBodyAsString();
            System.err.print("Fatal Error !!!!!!!!\n" + resp);
            throw new TestClientException("HTTP-Status code =" + status_code + " (Must be 200)! ", 
                                          null);
        }
        InputStream response_stream = null;
        try {
            response_stream = post.getResponseBodyAsStream();
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        DocumentBuilder doc_builder = null;
        try {
            doc_builder = doc_factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TestClientException("ParserConfigurationException occured", e);
        }
        Document doc = null;
        try {
            doc = doc_builder.parse(response_stream);
        } catch (SAXException e) {
            throw new TestClientException("SaxException occured", e);
        }
         catch (IOException e) {
            throw new TestClientException("IOException occured", e);
        }
        return doc;
    }
}

class StepConfig {

    //~ Instance/static variables ..................................................................

    private Document recordedInput;
    private Document recordedOutput;
    private String   styleSheet;
	private String fileName;
    //~ Constructors ...............................................................................

    StepConfig(String filename, Document recordIn, Document recordOut, String stylesheet) {
		this.fileName = filename;
        this.recordedInput  = recordIn;
        this.recordedOutput = recordOut;
        this.styleSheet     = stylesheet;
    }

    //~ Methods ....................................................................................

    /**
     * Returns the recordedInput.
     * @return Document
     */
    public Document getRecordedInput() {
        return recordedInput;
    }

    /**
     * Returns the recordedOutput.
     * @return Document
     */
    public Document getRecordedOutput() {
        return recordedOutput;
    }

    /**
     * Returns the styleSheet.
     * @return String
     */
    public String getStyleSheet() {
        return styleSheet;
    }
    
    
    /**
     * Returns the fileName.
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

}