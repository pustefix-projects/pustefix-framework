package org.pustefixframework.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import junit.framework.TestCase;

public abstract class AbstractSessionHandlingTest extends TestCase {

    protected static String SESSION_PARAM_NAME = "jsessionid";
    
    protected static Pattern PATTERN_URL = Pattern.compile("(https?)://(([^:]*)(:(\\d+))?)(/[^;?]*)?/?(;" + 
                                                            SESSION_PARAM_NAME + "=([^?]+))?(\\?.*)?");
    protected static Pattern PATTERN_COUNT = Pattern.compile(".*<!--(\\d+)-->.*");
    protected static Pattern COOKIE_SESSION = Pattern.compile(AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME + "=(\\w++).*");
    
    protected static Server server;
    protected static int HTTP_PORT = 8080;
    protected static int HTTPS_PORT = 8443;
    
    protected static Server createServer(int httpPort, int httpsPort, Class<? extends SessionTrackingStrategy> sessionTrackingStrategy, boolean cookieSessionHandlingDisabled) throws Exception {
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
        
        logger = Logger.getLogger("org.pustefixframework");
        logger.setLevel((Level)Level.WARN);
        logger.addAppender(appender);
        
        //Start embedded Jetty
        Server server = new Server();
        
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(HTTP_PORT);
        
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());
        SslContextFactory sslContextFactory = new SslContextFactory();
        
        URL res = AbstractSessionHandlingTest.class.getClassLoader().getResource("org/pustefixframework/http/keystore");
        sslContextFactory.setKeyStorePath(res.toExternalForm());
        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setKeyManagerPassword("password");
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        sslConnector.setPort(httpsPort);

        server.setConnectors(new Connector[] {connector, sslConnector});
        
        ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
        root.setContextPath("/");
        server.setHandler(root);
        
        Properties properties = new Properties();
        properties.setProperty("pfixcore.ssl_redirect_port.for." + httpPort, "" + httpsPort);
        properties.setProperty("servlet.encoding", "utf-8");
        root.addServlet(new ServletHolder(new SessionHandlingTestServlet(sessionTrackingStrategy, properties)), "/*");
           
        ((AbstractSessionManager)root.getSessionHandler().getSessionManager()).setSecureRequestOnly(true);
        if(cookieSessionHandlingDisabled) ((AbstractSessionManager)root.getSessionHandler().getSessionManager()).setUsingCookies(false);
            
        server.start();
        
        //Initialize HttpClient
        ProtocolSocketFactory protocolFactory = new SSLProtocolSocketFactory();
        Protocol protocol = new Protocol("https", protocolFactory, 443);
        Protocol.registerProtocol("https", protocol);
        
        return server;
    
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
    
}
