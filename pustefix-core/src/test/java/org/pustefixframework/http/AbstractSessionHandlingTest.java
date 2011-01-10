package org.pustefixframework.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import junit.framework.TestCase;

public abstract class AbstractSessionHandlingTest extends TestCase {

    protected static Pattern PATTERN_URL = Pattern.compile("(https?)://(([^:]*)(:(\\d+))?)(/[^;?]*)?/?(;jsessionid=([^?]+))?(\\?.*)?");
    protected static Pattern PATTERN_COUNT = Pattern.compile(".*<!--(\\d+)-->.*");
    protected static Pattern COOKIE_SESSION = Pattern.compile("JSESSIONID=(\\w++).*");
    
    protected Server server;
    protected int HTTP_PORT = 8080;
    protected int HTTPS_PORT = 8443;
    
    protected void setUp(Class<? extends SessionTrackingStrategy> sessionTrackingStrategy, boolean cookieSessionHandlingDisabled) throws Exception {
        
        if(server == null) {
            
            ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
            Logger logger=Logger.getRootLogger();
            logger.setLevel((Level)Level.WARN);
            logger.removeAllAppenders();
            logger.addAppender(appender);
            
            logger = Logger.getLogger("org.pustefixframework");
            logger.setLevel((Level)Level.WARN);
            logger.addAppender(appender);
            
            //Start embedded Jetty
            HTTP_PORT = findFreePort();
            HTTPS_PORT = findFreePort();
            server = new Server(HTTP_PORT);
            SslSocketConnector connector = new SslSocketConnector();
            connector.setPort(HTTPS_PORT);
            connector.setKeyPassword("password"); 
            connector.setPassword("password");
            connector.setKeystore("src/test/resources/org/pustefixframework/http/keystore");
            server.addConnector(connector);
            Context root = new Context(server,"/",Context.SESSIONS);
            Properties properties = new Properties();
            properties.setProperty("pfixcore.ssl_redirect_port.for." + HTTP_PORT, "" + HTTPS_PORT);
            properties.setProperty("servlet.encoding", "utf-8");
            root.addServlet(new ServletHolder(new SessionHandlingTestServlet(sessionTrackingStrategy, false, properties)), "/*");
            
            if(cookieSessionHandlingDisabled) ((AbstractSessionManager)root.getSessionHandler().getSessionManager()).setUsingCookies(false);
            
            server.start();
        
            //Initialize HttpClient
            ProtocolSocketFactory protocolFactory = new SSLProtocolSocketFactory();
            Protocol protocol = new Protocol("https", protocolFactory, 443);
            Protocol.registerProtocol("https", protocol);
        }
    
    }
    
    private static int findFreePort() {
        try {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch(IOException x) {
            throw new RuntimeException("Can't get free port", x);
        }
    }
    
}
