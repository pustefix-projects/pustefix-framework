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
import java.util.List;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
import org.xml.sax.SAXException;

import com.sun.net.ssl.KeyManager;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.X509TrustManager;
import de.schlund.pfixxml.ServletManager;

/**
 * Class for playback of a testcase.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class Testcase {

    //~ Instance/static variables ..................................................................

    
    
    private static Category CAT = Category.getInstance(Testcase.class.getName());
    private static DocumentBuilderFactory DOCBFAC = DocumentBuilderFactory.newInstance();

    private String srcDir;
    private String tmpDir;
    private String styleDir;
    private HttpConnection httpConnect;
    private HostConfiguration currentConfig = new HostConfiguration();
    private SimpleHttpConnectionManager conMan = new SimpleHttpConnectionManager();
    private boolean sslNeedsInit = true;
    private List allSteps;   
    private String testCaseName;
    
    /**
     * Create a new TestClient. 
     */
    public Testcase() {
    }

    /**
     * Start playback the testcase. Please call setOptions
     * before calling this method.
     * @return the result of the testcase
     */
    public TestcasePlaybackResult execute() throws Exception {
        if (CAT.isInfoEnabled()) {
            CAT.info("Starting test NOW");
        }
        String session = "";
        TestcasePlaybackResult playbackresult = new TestcasePlaybackResult();
        
        for(Iterator iter = allSteps.iterator(); iter.hasNext(); ) {

            TestcaseStep step = (TestcaseStep) iter.next();
            initHttpConnection(step.getHostname(), step.getPort(), step.getProto());
            step.setHttpConnection(httpConnect);
            step.setSessionID(session);
            TestcaseStepResult stepresult = step.doExecute();
            session = step.getSessionID();
            playbackresult.addTestcaseStepResult(stepresult); 
        }
        
        conMan.releaseConnection(httpConnect);
        return playbackresult;
    }
    
    private void init() throws TestClientException {
        File tmp = new File(tmpDir);
        if (!tmp.exists()) {
            if (CAT.isDebugEnabled()) {
                CAT.debug("Creating tmp dir = " + tmpDir);
            }
            tmp.mkdirs();
        }
        checkOptions();
        ArrayList files = getAllFilesFromTestcase();
        
        DocumentBuilder builder;
        try {
            builder = DOCBFAC.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TestClientException(e.getClass().getName(), e);
        }
        int i = 0;
        allSteps = new ArrayList();
        
        for(Iterator iter = files.iterator(); iter.hasNext(); ) {
            Document doc;
            try {
                doc = builder.parse(((File) iter.next()).getAbsoluteFile());
            } catch (SAXException e2) {
                throw new TestClientException(e2.getClass().getName(), e2);
            } catch (IOException e2) {
                throw new TestClientException(e2.getClass().getName(), e2);
            }
            Node input;
            Node refnodes;
            String stylesheet;
            try {
                input = XPathAPI.selectSingleNode(doc, "/step/request");
                refnodes = XPathAPI.selectSingleNode(doc, "/step/formresult");
                stylesheet = ((Text)XPathAPI.selectSingleNode(doc, "/step/stylesheet").getFirstChild()).getData();
            } catch (TransformerException e1) {
                throw new TestClientException(e1.getClass().getName(), e1);
            }
            
            Document indoc = builder.newDocument();
            indoc.appendChild(indoc.importNode(input, true));
            
            Document recrefdoc = builder.newDocument();
            recrefdoc.appendChild(recrefdoc.importNode(refnodes, true));
            
            TestcaseStep step = new TestcaseStep(indoc);
            step.setRecordedReferenceDoc(recrefdoc);
            step.setTempDir(tmpDir);
            step.setStylesheetDir(styleDir);
            step.setStylesheet(stylesheet);
            allSteps.add(step);
        }
    }

    /**
     * Configure the TestClient
     * @param src_dir the path to the testcase to playback. Not null.
     * @param tmp_dir directory where temporay data is written. If null, defaults are used.
     * @param style_dir directory containing stylesheets. If null, defaults are used.
     */
    public void setOptions(String src_dir, String tmp_dir, String style_dir, String name) throws TestClientException {
        testCaseName = name;
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
        init();
    }
    
    public int getNumberOfStepsForTestcase() {
        return allSteps.size(); 
    }

    public String getName() {
        return testCaseName;
    }

    /** read testcase files */
    private ArrayList getAllFilesFromTestcase() {
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
        if(ServletManager.isSslPort(port) || proto.toLowerCase().equals("https")) {
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
        Protocol myHTTPS = new Protocol("https", myssl, ServletManager.APACHE_SSL_PORT); // TODO: tomcat ssl port?
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


