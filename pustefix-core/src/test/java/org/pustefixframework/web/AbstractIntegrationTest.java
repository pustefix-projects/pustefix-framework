package org.pustefixframework.web;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.apache.catalina.Context;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.pustefixframework.http.SSLProtocolSocketFactory;

import junit.framework.TestCase;

public abstract class AbstractIntegrationTest extends TestCase {

    private static Map<String, ServerSetup> servers = new HashMap<>();

    protected int HTTP_PORT;
    protected int HTTPS_PORT;

    @Override
    protected void setUp() throws Exception {

        String serverCacheKey = getClass().getName();
        ServerSetup server = servers.get(serverCacheKey);
        if(server == null) {
            server = new ServerSetup();
            server.httpPort = findFreePort();
            server.httpsPort = findFreePort();
            try {
                createServer(server.httpPort, server.httpsPort);
                servers.put(serverCacheKey, server);
            } catch(Exception x) {
                throw new RuntimeException("Error creating embedded server", x);
            }
        }
        HTTP_PORT = server.httpPort;
        HTTPS_PORT = server.httpsPort;
    }

    protected abstract void configure(ServletContext context);

    protected Tomcat createServer(int httpPort, int httpsPort) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("target/tomcat");
        tomcat.setPort(httpPort);

        tomcat.getConnector();

        Connector httpsConnector = new Connector();
        httpsConnector.setPort(httpsPort);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keyAlias", "dummy");
        httpsConnector.setAttribute("keystorePass", "passphrase");

        URL res = getClass().getClassLoader().getResource("org/pustefixframework/http/keystore");
        httpsConnector.setAttribute("keystoreFile", res.toExternalForm());
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);

        Service service = tomcat.getService();
        service.addConnector(httpsConnector);
        tomcat.getConnector().setRedirectPort(httpsPort);

        Context ctx = tomcat.addWebapp("/", new File("target/test-classes/webapp").getAbsolutePath());

        Set<Class<?>> classes = new HashSet<>();
        ctx.addServletContainerInitializer(new ContextInitializer(this), classes);

        Properties properties = new Properties();
        properties.setProperty("pfixcore.ssl_redirect_port.for." + httpPort, "" + httpsPort);
        properties.setProperty("servlet.encoding", "utf-8");

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

    private static class ServerSetup {

        int httpPort;
        int httpsPort;

    }

    @HandlesTypes(javax.servlet.ServletContext.class)
    private static class ContextInitializer implements ServletContainerInitializer {

        AbstractIntegrationTest test;

        ContextInitializer(AbstractIntegrationTest test) {
            this.test = test;
        }

        @Override
        public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
            test.configure(ctx);
        }
    }

}
