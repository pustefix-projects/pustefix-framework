package org.pustefixframework.http;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
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
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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
        
        ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%p: %m\n"));
        Logger logger=Logger.getRootLogger();
        logger.setLevel((Level)Level.WARN);
        logger.removeAllAppenders();
        logger.addAppender(appender);
        
        logger = Logger.getLogger("org.pustefixframework");
        logger.setLevel((Level)Level.WARN);
        logger.addAppender(appender);
        
        Tomcat tomcat = new Tomcat();
        
        tomcat.setPort(HTTP_PORT);
        
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

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

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
        Tomcat.addServlet(ctx, "SessionHandlingTestServlet", new SessionHandlingTestServlet(sessionTrackingStrategy, properties));
        ctx.addServletMappingDecoded("/*", "SessionHandlingTestServlet");
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
