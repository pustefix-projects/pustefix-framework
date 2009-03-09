package de.schlund.pfixxml.testrecording;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Pustefix application running on a given tomcat.
 * Note that https is not a property of an application because the same application
 * may run secure and insecure.
 */
public class Application implements Serializable {
    private final String name;
    private final String server;
    private final boolean tomcat;
    private final String startPath;
    private final String sessionSuffix;
    
    private static final long serialVersionUID = 6157375568733984286L;

    public Application(String name, String server, boolean tomcat, String startPath, String sessionSuffix) {
        if (server.indexOf('/') != -1) {
            throw new IllegalArgumentException(server);
        }
        if (!startPath.startsWith("/") || startPath.endsWith("/")) {
            throw new IllegalArgumentException(startPath);
        }
        this.name = name;
        this.server = server;
        this.tomcat = tomcat;
        this.startPath = startPath;
        this.sessionSuffix = sessionSuffix;
    }
    
    public Application() {
        throw new IllegalStateException("TODO: castor");
    }

    /** server as stored in session */
    public String getServer() {
        return server;
    }

    public String getStartPath() {
        return startPath;
    }

    public URL getUrl(boolean https, String path) {
        return getUrl(https, path, "nosuchsession." + sessionSuffix);
    }

    public URL getUrl(boolean https, String path, String sessionId) {
        String protocol;
        String port;
        
        if (!path.startsWith("/") || path.endsWith("/")) {
            throw new IllegalArgumentException("invalid path: " + path);
        }
        if (https) {
            protocol = "https";
        } else {
            protocol = "http";
        }
        if (tomcat) {
            port = https ? ":8443" : ":8080";
        } else {
            port = "";
        }
        try {
            return new URL(protocol + "://" + server + port + path + ";jsessionid=" + sessionId);
        } catch (MalformedURLException e) {
            throw new RuntimeException("TODO", e);
        }
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return "application(name=" + name + ", tomcat=" + tomcat + ", server=" + server + ")";
    }
}
