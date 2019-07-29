package org.pustefixframework.http;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.HandlesTypes;

import org.apache.catalina.Context;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import junit.framework.TestCase;

public abstract class AbstractSessionHandlingTest extends TestCase {

    protected static String SESSION_PARAM_NAME = "jsessionid";
    
    protected static Pattern PATTERN_URL = Pattern.compile("(https?)://(([^:]*)(:(\\d+))?)(/[^;?]*)?/?(;" + 
                                                            SESSION_PARAM_NAME + "=([^?]+))?(\\?.*)?");
    protected static Pattern PATTERN_COUNT = Pattern.compile(".*<!--(\\d+)-->.*");
    protected static Pattern COOKIE_SESSION = Pattern.compile(AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME + "=(\\w++).*");
    
    protected static int HTTP_PORT = 8080;
    protected static int HTTPS_PORT = 8443;
    
    protected static Tomcat createServer(int httpPort, int httpsPort, Class<? extends SessionTrackingStrategy> sessionTrackingStrategy) throws Exception {
        
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("target/tomcat");
        tomcat.setPort(HTTP_PORT);
        tomcat.getConnector().setRedirectPort(HTTPS_PORT);

        Connector httpsConnector = new Connector();
        httpsConnector.setPort(HTTPS_PORT);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keyAlias", "jetty");
        httpsConnector.setAttribute("keystorePass", "password");

        URL res = AbstractSessionHandlingTest.class.getClassLoader().getResource("org/pustefixframework/http/keystore");
        httpsConnector.setAttribute("keystoreFile", res.toExternalForm());
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);

        Service service = tomcat.getService();
        service.addConnector(httpsConnector);

        Context ctx = tomcat.addWebapp("/", new File("target/test-classes/webapp").getAbsolutePath());

        Set<SessionTrackingMode> modes = new HashSet<>();
        if(sessionTrackingStrategy == CookieOnlySessionTrackingStrategy.class) {
            modes.add(SessionTrackingMode.COOKIE);
        } else if(sessionTrackingStrategy == URLRewriteSessionTrackingStrategy.class) {
            modes.add(SessionTrackingMode.URL);
        } else {
            modes.add(SessionTrackingMode.COOKIE);
            modes.add(SessionTrackingMode.URL);
        }
        
        Set<Class<?>> classes = new HashSet<>();
        ctx.addServletContainerInitializer(new ContextInitializer(modes), classes);
        
        Properties properties = new Properties();
        properties.setProperty("pfixcore.ssl_redirect_port.for." + httpPort, "" + httpsPort);
        properties.setProperty("servlet.encoding", "utf-8");
        
        //Initialize HttpClient
        ProtocolSocketFactory protocolFactory = new SSLProtocolSocketFactory();
        Protocol protocol = new Protocol("https", protocolFactory, 443);
        Protocol.registerProtocol("https", protocol);
        
        ctx.addParameter("pustefix.https.port", String.valueOf(httpsPort));
        tomcat.start();
        return tomcat;
    }
    
    protected static int findFreePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch(IOException x) {
            throw new RuntimeException("Can't get free port", x);
        }
    }

    public static String getProtocol(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if(matcher.matches()) return matcher.group(1);
        return null;
    }

    public static String getHost(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if(matcher.matches()) return matcher.group(3);
        return null;
    }

    public static int getPort(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if(matcher.matches()) return Integer.parseInt(matcher.group(5));
        return 80;
    }

    public static int getCount(String content) {
        Matcher matcher = PATTERN_COUNT.matcher(content);
        if(matcher.find()) return Integer.parseInt(matcher.group(1));
        return 0;
    }

    public static String getSession(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if(matcher.matches()) return matcher.group(8);
        return null;
    }

    public static String getSessionFromResponseCookie(HttpMethod method) {
        Header[] headers = method.getResponseHeaders("Set-Cookie");
        for(Header header: headers) {
            String value = header.getValue();
            if(value != null) {
                Matcher matcher = COOKIE_SESSION.matcher(value);
                if(matcher.matches()) return matcher.group(1);
            }
        }
        return null;
    }

    public static String getSessionFromRequestCookie(HttpMethod method) {
        Header[] headers = method.getRequestHeaders("Cookie");
        for(Header header: headers) {
            String value = header.getValue();
            if(value != null) {
                Matcher matcher = COOKIE_SESSION.matcher(value);
                if(matcher.matches()) return matcher.group(1);
            }
        }
        return null;
    }

    public static void printDump(HttpMethod method) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(dump(method));
        System.out.println("--------------------------------------------------------------------------------");
    }

    public static String dump(HttpMethod method) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(method.getURI());
        } catch (URIException e) {
            sb.append("-");
        }
        sb.append("\n\n");
        sb.append(method.getName()).append(" ").append(method.getPath()).append(method.getQueryString()).append("\n");
        Header[] headers = method.getRequestHeaders();
        for(Header header: headers) {
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        sb.append("\n");
        sb.append(method.getStatusLine()).append("\n");
        headers = method.getResponseHeaders();
        for(Header header: headers) {
            sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        Header ctypeHeader = method.getResponseHeader("Content-Type");
        if(ctypeHeader != null && ctypeHeader.getValue().startsWith("text/")) {
            try {
                sb.append("\n").append(method.getResponseBodyAsString()).append("\n");
            } catch(IOException e) {
                //ignore
            }
        }
        return sb.toString();
    }


    @HandlesTypes(javax.servlet.ServletContext.class)
    private static class ContextInitializer implements ServletContainerInitializer {

        Set<SessionTrackingMode> modes;

        ContextInitializer(Set<SessionTrackingMode> modes) {
            this.modes = modes;
        }

        @Override
        public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
            ctx.setSessionTrackingModes(modes);
        }
    }

}
