package de.schlund.pfixxml.jmx;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


/** Pustefix application running on a given tomcat */
public class Application implements Serializable {
    private final String name;
    private final String server;
    private final boolean tomcat;
    private final String startPath;
    private final String sessionSuffix;
    
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

    public URL getUrl() {
        return getUrl(startPath);
    }
    
    public URL getUrl(String path) {
        return getUrl(path, "nosuchsession." + sessionSuffix);
    }

    public URL getUrl(String path, String sessionId) {
        String port;
        
        if (!path.startsWith("/") || path.endsWith("/")) {
            throw new IllegalArgumentException("invalid path: " + path);
        }
        if (tomcat) {
            port = ":8080";  // /xml/config not needed here, it's part of the test case
        } else {
            port = "";
        }
        try {
            return new URL("http://" + server + port + path + ";jsessionid=" + sessionId + "&__forcelocal=1");
        } catch (MalformedURLException e) {
            throw new RuntimeException("TODO", e);
        }
    }

    public String getName() {
        return name;
    }
}
