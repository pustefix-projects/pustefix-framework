/*
 * Created on 07.08.2003
 *
 */
package de.schlund.pfixxml.testenv;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.pfix_httpclient.Header;
import org.apache.commons.pfix_httpclient.HttpConnection;
import org.apache.commons.pfix_httpclient.HttpException;
import org.apache.commons.pfix_httpclient.HttpRecoverableException;
import org.apache.commons.pfix_httpclient.HttpState;
import org.apache.commons.pfix_httpclient.HttpStatus;
import org.apache.commons.pfix_httpclient.NameValuePair;
import org.apache.commons.pfix_httpclient.methods.PostMethod;
import org.apache.log4j.Category;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */
public class TestcaseStep {

    private static Category CAT = Category.getInstance(TestcaseStep.class.getName());
    private static DocumentBuilderFactory DOCBUILDERFAC = DocumentBuilderFactory.newInstance();
    private static String XMLONLY_PARAM_KEY = "__xmlonly";
    private static String XMLONLY_PARAM_VALUE = "1";

    private Document formInput;
    private Document referenceData;
    private String targetHost;
    private int targetPort;
    private String targetProto;
    private String targetURL;
    
    private TestcaseStepResult result;
    private HttpConnection httpConnection;
    private String sessionID;

    public TestcaseStep(Document form_input) throws Exception { 
        if(form_input == null) {
            throw new IllegalArgumentException("A NP as forminput is not allowed here!");
        }
        
        result = new TestcaseStepResult();
        formInput = form_input;
        
        targetHost = getHostnameFromInput();
        targetPort = getPortFromInput();
        targetProto = getProtoFromInput();
        targetURL = getURIFromInput();
        sessionID ="";
    }

    public void setHttpConnection(HttpConnection con) {
        if(con == null) {
            throw new IllegalArgumentException("A NP as httpConnection is not allowed here!");
        }
        httpConnection = con;
    }

    public void doExecute() throws Exception {
        if(CAT.isDebugEnabled()) {
            CAT.debug("===================================================="); 
        }
        
        getResultFromFormInput();
        if(CAT.isDebugEnabled()) {
            CAT.debug("===================================================="); 
        }
    }

    public String getHostname() {
        return targetHost;
    }

    public int getPort() {
        return targetPort;
    }

    public String getProto() {
        return targetProto;
    }
    
    public void setSessionID(String id) {
        sessionID = id;
    }
    
    public String getSessionID() {
        return sessionID;
    }
    
    public TestcaseStepResult getResult() {
        return result;
    }

    /** get result from server after posting request data */
    private void getResultFromFormInput() throws Exception {
        
        NameValuePair[] post_params = getPostParamsFromInput();
        
        if(CAT.isDebugEnabled()) {
            CAT.debug("Post params...");
            for (int i = 0; i < post_params.length; i++) {
                CAT.debug(post_params[i].getName() + "=" + post_params[i].getValue());
            }
        }

        PostMethod post = null;
        int status_code = -1;
        String currenturl = targetProto + "://" + targetHost + ":"+ targetPort + targetURL + ";jsessionid=" + sessionID;
 
        boolean redirect_needed = true;
        
        while (redirect_needed) {
            if(CAT.isDebugEnabled()) {
                CAT.debug("Exceuting POST to " + currenturl);
            }
            
            post = new PostMethod(currenturl);
            post.addParameters(post_params);
            
            try {
                status_code = post.execute(new HttpState(), httpConnection);
            } catch (HttpException e) {
                if( e instanceof HttpRecoverableException) {
                    CAT.warn("Recieved statuscode "+status_code+" -> "+e.getMessage());
                    // FIXME
                    if(status_code <= 0) {
                        status_code = 500;
                    }
                } else {
                    CAT.error("HttpException!", e);
                    throw new TestClientException("HTTPException occured!:" + status_code, e);
                }
            } catch (IOException e) {
                throw new TestClientException("IOException occured!", e);
            }
            if (CAT.isDebugEnabled()) {
                CAT.debug("   StatusCode=" + status_code);
            }

            if (status_code == HttpStatus.SC_MOVED_PERMANENTLY || status_code == HttpStatus.SC_MOVED_TEMPORARILY) {
                result.setStatuscode(status_code);
                if(CAT.isDebugEnabled()) {
                    CAT.debug("-------------Header data---------------------------");
                    Header[] headers = post.getResponseHeaders();
                    for(int i=0; i<headers.length; i++) {
                        CAT.debug(headers[i].getName()+":"+headers[i].getValue());
                    }
                    CAT.debug("-------------Header data---------------------------");
                }
                
                Header conheader = post.getResponseHeader("connection");
                if(conheader.getValue().equals("close")) {
                    CAT.error("Connection closed by server. Either the server does not support keep-alive or" +
                        "the testcase has more steps then keep-alive request are allowed by the server");
                    return;
                }
                
                Header locheader = post.getResponseHeader("location");
                if(locheader == null) {
                    CAT.error("Unable to get location information from header!");
                    return;
                }
                String redirect_location = locheader.getValue();
                currenturl = redirect_location;
                if(CAT.isDebugEnabled()) {
                    CAT.debug("redirected to " + redirect_location);
                }

            } else if (status_code != HttpStatus.SC_OK) {
                CAT.warn("StatsuCode ==" + status_code);
                result.setStatuscode(status_code);
                result.setServerError(true);
                redirect_needed = false;
            } else {
                
                if(redirect_needed) {
                    int sess_start = currenturl.indexOf('=');
                    sess_start++;
                    int sess_end = currenturl.indexOf('?');
                    if(sess_end > -1) {
                        sessionID = currenturl.substring(sess_start, sess_end);
                    } else {
                        sessionID = currenturl.substring(sess_start);
                    }
                }
                redirect_needed = false;
                InputStream response_stream = null;
                try {
                    response_stream = post.getResponseBodyAsStream();
                } catch (IOException e) {
                    throw new TestClientException("IOException occured!", e);
                }
                result.setServerResponse(convertServerResponseToDocument(response_stream));
                result.setStatuscode(status_code);
            }
        }

    }

    /** extract postparams from the recorded request data */
    private NameValuePair[] getPostParamsFromInput() throws TestClientException {
        NodeList value = null;
        try {
            value = XPathAPI.selectNodeList(formInput, "/request/params/param");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        NameValuePair[] post_params = new NameValuePair[value.getLength() + 1];
        int i = 0;
        for (int ii = 0; ii < value.getLength(); ii++) {
            Element e = (Element) value.item(ii);
            String param_name = e.getAttribute("name");
            String param_value = e.hasChildNodes() ? e.getFirstChild().getNodeValue() : "";
            post_params[i] = new NameValuePair(param_name, param_value);
            i++;
        }
        post_params[i] = new NameValuePair(XMLONLY_PARAM_KEY, XMLONLY_PARAM_VALUE);
        return post_params;
    }

    /** extract the target hostname from the recorded request data */
    private String getHostnameFromInput() throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(formInput, "/request/hostname");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String name = ((Element) value).getFirstChild().getNodeValue();
        return name;
    }

    /** extract the target hostport from the recorded request data */
    private int getPortFromInput() throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(formInput, "/request/port");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String p = ((Element) value).getFirstChild().getNodeValue();
        int port = Integer.parseInt(p);
        return port;
    }

    /** extract the protocol from the recorded request data */
    private String getProtoFromInput() throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(formInput, "/request/proto");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String proto = ((Element) value).getFirstChild().getNodeValue();
        return proto;
    }

    /** extract the URI from the recorded request data */
    private String getURIFromInput() throws TestClientException {
        Node value = null;
        try {
            value = XPathAPI.selectSingleNode(formInput, "/request/uri");
        } catch (TransformerException e) {
            throw new TestClientException("TransformerException occured!", e);
        }
        String uri = ((Element) value).getFirstChild().getNodeValue();
        return uri;
    }

    /** Convert the inputstream from the server into a XML-document */
    private Document convertServerResponseToDocument(InputStream istream)
        throws ParserConfigurationException, SAXException, IOException {
        return DOCBUILDERFAC.newDocumentBuilder().parse(istream);
    }
}
