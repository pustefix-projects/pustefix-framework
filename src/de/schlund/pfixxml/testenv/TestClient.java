package de.schlund.pfixxml.testenv;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.pfix_httpclient.HostConfiguration;
import org.apache.commons.pfix_httpclient.HttpConnection;
import org.apache.commons.pfix_httpclient.SimpleHttpConnectionManager;
import org.apache.commons.pfix_httpclient.protocol.Protocol;
import org.apache.commons.pfix_httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.log4j.Category;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.X509TrustManager;

/**
 * Class for playback of a testcase.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestClient {

    //~ Instance/static variables ..................................................................

    
    
    private static Category CAT = Category.getInstance(TestClient.class.getName());
    private static DocumentBuilderFactory DOCBFAC = DocumentBuilderFactory.newInstance();

    private String srcDir;
    private String tmpDir;
    private String styleDir;
    private HttpConnection httpConnect;
    private long request_count = 0;
    private String sessionId;
    private boolean ssl = false;
    private HostConfiguration currentConfig = new HostConfiguration();
    private String uri_session = new String();
    private SimpleHttpConnectionManager conMan = new SimpleHttpConnectionManager();
    private boolean sslNeedsInit = true;
   
    /**
     * Create a new TestClient. 
     */
    public TestClient() {
    }

    /**
     * Start playback the testcase. Please call setOptions
     * before calling this method.
     * @return the result of the testcase
     */
    public TestcasePlaybackResult makeTest() throws Exception {
        if (CAT.isInfoEnabled()) {
            CAT.info("Starting test NOW");
        }
        File tmp = new File(tmpDir);
        if (!tmp.exists()) {
            if (CAT.isDebugEnabled()) {
                CAT.debug("Creating tmp dir = " + tmpDir);
            }
            tmp.mkdirs();
        }
        checkOptions();
        ArrayList files = getAllFilesFromTestcase();
        String session = "";
        
        TestcasePlaybackResult playbackresult = new TestcasePlaybackResult();
        DocumentBuilder builder = DOCBFAC.newDocumentBuilder();
        int i = 0;
        
        for(Iterator iter = files.iterator(); iter.hasNext(); ) {
            Document doc = builder.parse(((File) iter.next()).getAbsoluteFile());
            Node input = XPathAPI.selectSingleNode(doc, "/step/request");
            Node refnodes = XPathAPI.selectSingleNode(doc, "/step/formresult");
            
            String stylesheet = ((Text)XPathAPI.selectSingleNode(doc, "/step/stylesheet").getFirstChild()).getData();
            
            Document indoc = builder.newDocument();
            indoc.appendChild(indoc.importNode(input, true));
            
            Document recrefdoc = builder.newDocument();
            recrefdoc.appendChild(recrefdoc.importNode(refnodes, true));
            
            TestcaseStep step = new TestcaseStep(indoc);
            
            initHttpConnection(step.getHostname(), step.getPort(), step.getProto());
            step.setHttpConnection(httpConnect);
            step.setSessionID(session);
            step.doExecute();
            session = step.getSessionID();
            
            TestcaseStepResult stepresult = step.getResult();
            stepresult.setRecordedReferenceDoc(recrefdoc);
            stepresult.createDiff(tmpDir, i++, styleDir, stylesheet);
            playbackresult.addTestcaseStepResult(stepresult); 
        }
        
        return playbackresult;
    }

    /**
     * Configure the TestClient
     * @param src_dir the path to the testcase to playback. Not null.
     * @param tmp_dir directory where temporay data is written. If null, defaults are used.
     * @param style_dir directory containing stylesheets. If null, defaults are used.
     */
    public void setOptions(String src_dir, String tmp_dir, String style_dir) throws TestClientException {
        if (src_dir == null) {
            throw new IllegalArgumentException(
                "The value 'null' is not allowed here for src_dir! "
                    + "Arguments: Directories for testcases: "
                    + src_dir
                    + ", temporary: "
                    + tmp_dir
                    + ", stylesheets: "
                    + style_dir);
        }
        if (CAT.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append("\n");
            sb.append("Setting directory for testcases to " + src_dir).append("\n");
            sb.append("Setting temporary directory to " + tmp_dir).append("\n");
            sb.append("Setting stylesheet directory to " + style_dir).append("\n");
            CAT.debug(sb.toString());
        }
        srcDir = src_dir;
        if (tmp_dir == null) {
            tmpDir = System.getProperties().getProperty("java.io.tmpdir");
        } else {
            tmpDir = tmp_dir;
        }
        if (style_dir == null) {
            styleDir = srcDir;
        } else {
            styleDir = style_dir;
        }
    }
    
    public static int getNumberOfStepsForTestcase(String dir_to_testcase) {
        File dir = new File(dir_to_testcase);
        File[] files = dir.listFiles();
        return files.length; 
    }

//    /** execute test */
//  /*  private TestcasePlaybackResult doTest(ArrayList files) throws TestClientException {
//        TestcasePlaybackResult tcresult = new TestcasePlaybackResult();
//        TestcaseStepResultSpecial stepresult = null;
//        boolean has_diff = false;
//        int scode = 0;
//        for (int j = 0; j < files.size(); j++) {
//            stepresult = new TestcaseStepResultSpecial();
//            if (CAT.isDebugEnabled()) {
//                StringBuffer sb = new StringBuffer();
//                sb.append("\n________________________________________________________________\n");
//                sb.append("Step ").append(j).append("\n");
//                //sb.append("  File=").append(config[j].getFileName()).append("\n");
//                CAT.debug(sb.toString());
//            } else if (CAT.isInfoEnabled()) {
//                CAT.info("\nDoing step " + j);
//            }
//            Document current_output_tree = null;
//            //TestcaseSimpleStepResult res = null;
//            TestcaseStepResult result = null;
//            try {
//                //res = getResultFromFormInput(config[j].getRecordedInput(), null);
//                TestcaseStep step = new TestcaseStep((String) files.get(j), httpConnect);
//                result = step.doExecute();
//                current_output_tree = result.getServerResponse();
//                scode = result.getStatuscode();
//            } catch (TestClientException e) {
//                if (e.getExceptionCause() instanceof HttpRecoverableException) {
//                    CAT.warn("Uuuups...skipping...");
//                    break;
//                } else {
//                    throw e;
//                }
//            }
//            if (scode == HttpStatus.SC_MOVED_PERMANENTLY || scode == HttpStatus.SC_MOVED_TEMPORARILY) {
//                try {
//                    String loc = res.getRedirectLocation();
//                    //System.out.println("redirect nach:" + loc);
//                    res = getResultFromFormInput(config[j].getRecordedInput(), loc);
//                    current_output_tree = res.getDoc();
//                    scode = res.getScode();
//                } catch (TestClientException e) {
//                    if (e.getExceptionCause() instanceof HttpRecoverableException) {
//                        CAT.warn("Uuuups...skipping...");
//                        break;
//                    } else {
//                        throw e;
//                    }
//                }
//            }
//
//            if (scode == HttpStatus.SC_OK) {
//
//                Document tmp_rec = doTransform(config[j].getRecordedOutput(), config[j].getStyleSheet());
//                Document tmp_out = doTransform(current_output_tree, config[j].getStyleSheet());
//                String tmp_fname_cur = tmpDir + "/_current" + j;
//                String tmp_fname_rec = tmpDir + "/_recorded" + j;
//                try {
//                    XMLSerializeUtil.getInstance().serializeToFile(tmp_out, tmp_fname_cur, 2, false);
//                } catch (FileNotFoundException e) {
//                    throw new TestClientException("Unable to serialize! File not found.", e);
//                } catch (IOException e) {
//                    throw new TestClientException("Unable to serialize! IOException.", e);
//                }
//                try {
//                    XMLSerializeUtil.getInstance().serializeToFile(tmp_rec, tmp_fname_rec, 2, false);
//                } catch (FileNotFoundException e) {
//                    throw new TestClientException("Unable to serialize! File not found.", e);
//                } catch (IOException e) {
//                    throw new TestClientException("Unable to serialize! IOException.", e);
//                }
//                if (CAT.isDebugEnabled()) {
//                    CAT.debug("  Diffing " + tmp_fname_cur + " and " + tmp_fname_rec + " ...");
//                } else if (CAT.isInfoEnabled()) {
//                    CAT.info("  Diffing...");
//                }
//                String msg = doDiff(tmp_fname_cur, tmp_fname_rec);
//                if (CAT.isInfoEnabled()) {
//                    CAT.info("  Diff " + (msg == null || msg.equals("") ? "not exists" : "exists"));
//                }
//                stepresult.setDiffString(msg);
//                stepresult.setStatusCode(scode);
//                if (msg == null || msg.equals("")) {
//                    msg = ":-)";
//                } else {
//                    has_diff = true;
//                }
//                tcresult.addTestcaseStepResult(stepresult);
//            } else {
//                stepresult.setStatusCode(scode);
//                stepresult.setDiffString(null);
//                tcresult.addTestcaseStepResult(stepresult);
//                return tcresult;
//            }
//        }
//        if (CAT.isInfoEnabled()) {
//            CAT.warn("\n*** Resut: ***\n" + (has_diff ? ";-(" : ";-)"));
//        }
//        return tcresult;
//    }
//
//    /** prepare */
//    private StepConfig[] doPrepare(ArrayList files) throws TestClientException {
//        DocumentBuilder doc_builder;
//        StepConfig[] config = new StepConfig[files.size()];
//        boolean ssl_initialized = false;
//        try {
//            doc_builder = docBFactory.newDocumentBuilder();
//        } catch (ParserConfigurationException e) {
//            throw new TestClientException("Could not get a DocumentBuilder!", e);
//        }
//        if (CAT.isInfoEnabled()) {
//            CAT.info("Analyzing files...");
//        }
//        for (int i = 0; i < files.size(); i++) {
//            File file = (File) files.get(i);
//            if (CAT.isDebugEnabled()) {
//                CAT.debug("  " + file.getName());
//            }
//            Document file_content = null;
//            try {
//                file_content = doc_builder.parse(file);
//            } catch (SAXException e) {
//                if (e instanceof SAXParseException) {
//                    SAXParseException saxex = (SAXParseException) e;
//                    throw new TestClientException("SAXException occured at line " + saxex.getLineNumber() + "in file " + file.getName(), e);
//                } else {
//                    throw new TestClientException("SAXException occured", e);
//                }
//            } catch (IOException e) {
//                throw new TestClientException("IOException occured", e);
//            }
//            Document recorded_out = new DocumentImpl();
//            Document recorded_in = new DocumentImpl();
//            Node result_out = null;
//            try {
//                result_out = XPathAPI.selectSingleNode(file_content, "/step/formresult");
//            } catch (TransformerException e) {
//                throw new TestClientException("TransformerException occured!", e);
//            }
//            Node result_in = null;
//            try {
//                result_in = XPathAPI.selectSingleNode(file_content, "/step/request");
//            } catch (TransformerException e) {
//                throw new TestClientException("TransformerException occured!", e);
//            }
//            String stylessheet = null;
//            try {
//                Node ssheet = XPathAPI.selectSingleNode(file_content, "/step/stylesheet");
//                if (ssheet != null) {
//                    stylessheet = ssheet.getFirstChild().getNodeValue();
//                }
//            } catch (TransformerException e) {
//                throw new TestClientException("TransformerException occured!", e);
//            }
//            Node n_out = recorded_out.importNode(result_out, true);
//            Node n_in = recorded_in.importNode(result_in, true);
//            recorded_out.appendChild(n_out);
//            recorded_in.appendChild(n_in);
//            StepConfig conf = new StepConfig(((File) files.get(i)).getName(), recorded_in, recorded_out, stylessheet);
//            config[i] = conf;
//            // check, if we must init SSL
//            String proto = null;
//            try {
//                proto = XPathAPI.selectSingleNode(recorded_in, "/request/proto").getFirstChild().getNodeValue();
//            } catch (TransformerException e) {
//                throw new TestClientException("TransformerException occured!", e);
//            }
//            if (proto.toUpperCase().equals("https".toUpperCase()) && !ssl_initialized) {
//                if (CAT.isInfoEnabled()) {
//                    CAT.info("https detected!");
//                    CAT.info("Initializing SSL...");
//                }
//                initSSL();
//                if (CAT.isInfoEnabled()) {
//                    CAT.info("Done");
//                }
//                ssl_initialized = true;
//            }
//        }
//        if (CAT.isInfoEnabled()) {
//            CAT.info("Done");
//        }
//        return config;
//    }

    /** do the transformation */

   
    /** read testcase files */
    private ArrayList getAllFilesFromTestcase() throws TestClientException {
        File dir = new File(srcDir);
        File[] all_files = dir.listFiles();
        ArrayList files = new ArrayList();
        for (int i = 0; i < all_files.length; i++) {
            if (all_files[i].canRead() && all_files[i].isFile() && !all_files[i].getName().startsWith(".")) {
                files.add(all_files[i]);
            }
        }
        Arrays.sort(files.toArray());
        if (CAT.isDebugEnabled()) {
            CAT.debug("Reading files...");
            for (int i = 0; i < files.size(); i++) {
                CAT.debug("  " + ((File) files.get(i)).getName());
            }
        } else if (CAT.isInfoEnabled()) {
            CAT.info("Reading files...");
        }
        if (CAT.isInfoEnabled()) {
            CAT.info("Done");
        }
        return files;
    }

    /** check options */
    private void checkOptions() throws TestClientException {
        if (CAT.isDebugEnabled()) {
            CAT.debug("Checking options...");
        }
        File input_dir = new File(srcDir);
        if (!input_dir.isDirectory() || !input_dir.canRead()) {
            throw new TestClientException(srcDir + " is not a directory or not readable!", null);
        }
        File tmp_dir = new File(tmpDir);
        if (!tmp_dir.isDirectory() || !tmp_dir.canRead()) {
            throw new TestClientException(tmpDir + " is not a directory or not readable!", null);
        }
        File style_dir = new File(styleDir);
        if (!style_dir.isDirectory() || !style_dir.canRead()) {
            throw new TestClientException(styleDir + " is not a directory or not readable!", null);
        }
        if (CAT.isDebugEnabled()) {
            CAT.debug("Checking options done!");
        }
    }

    /** initialize the HTTP-connection */
    private void initHttpConnection(String hostname, int port, String proto) throws TestClientException {
        HostConfiguration config = new HostConfiguration();
        if(port == 443 || proto.toLowerCase().equals("https")) {
            if(sslNeedsInit) {
                if(CAT.isDebugEnabled()) {
                    CAT.debug("Initialising SSL");
                }
                initSSL();
                sslNeedsInit = false;
            }
        }   
        config.setHost(hostname, port, proto);
        if (httpConnect != null && config.hostEquals(httpConnect)) {
            return;
        } else {
            StringBuffer sb = new StringBuffer(255);
            currentConfig = config;
            sessionId = null;
            sb.append("\n----------------------------------------------\n");
            if (httpConnect == null) {
                sb.append("No HTTP Connection. Establishing new connection.\n");
            } else if (!config.hostEquals(httpConnect)) {
                sb.append("HostConfiguration has changed. Establishing new connection.\n");
            }
            sb.append("  Host=").append(currentConfig.getHost()).append("\n");
            sb.append("  Port=").append(currentConfig.getPort()).append("\n");
            sb.append(" Proto=").append(currentConfig.getProtocol().toString()).append("\n");
            if (httpConnect != null && httpConnect.isOpen()) {
                httpConnect.close();
                conMan.releaseConnection(httpConnect);
            }
            httpConnect = conMan.getConnection(currentConfig);
            try {
                httpConnect.open();
            } catch (IOException e) {
                throw new TestClientException("Unable to reopen HTTP connection!", e);
            }
            sb.append("   SSL=").append(httpConnect.isSecure()).append("\n");
            sb.append("\n----------------------------------------------\n");
            if (CAT.isInfoEnabled()) {
                CAT.info(sb.toString());
            }
        }
    }

 

   

    /** initialize SSL */
    private void initSSL() throws TestClientException {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        X509TrustManager tm = new MyX509TrustManager();
        KeyManager[] km = null;
        TrustManager[] tma = { tm };
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSLv3");
        } catch (NoSuchAlgorithmException e) {
            throw new TestClientException(" Error during SSLInit: No such Algorithm!", e);
        }
        try {
            sslContext.init(km, tma, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            throw new TestClientException("KeyManagmentException during SSLInit!", e);
        }
        SSLSocketFactory ssl_fac = sslContext.getSocketFactory();
        MySSLSocketfactory myssl = new MySSLSocketfactory(ssl_fac);
        Protocol myHTTPS = new Protocol("https", myssl, 443);
        Protocol.registerProtocol("https", myHTTPS);
    }
}



/** HACK for handling recorded data over SSL */
class MyX509TrustManager implements X509TrustManager {

    //~ Methods ....................................................................................

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] chain) {
        return true;
    }
}

/** HACK for handling recorded data over SSL */
class MySSLSocketfactory implements SecureProtocolSocketFactory {

    //~ Instance/static variables ..................................................................

    private SSLSocketFactory sslImpl;

    //~ Constructors ...............................................................................

    public MySSLSocketfactory(SSLSocketFactory ssl) {
        sslImpl = ssl;
    }

    //~ Methods ....................................................................................

    public Socket createSocket(String host, int port) throws IOException {
        return sslImpl.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress client_address, int client_port) throws IOException {
        return sslImpl.createSocket(host, port, client_address, client_port);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean close) throws IOException {
        return sslImpl.createSocket(socket, host, port, close);
    }
}


