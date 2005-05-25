package de.schlund.pfixxml.jmx;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;


/** Pustefix application running on a given tomcat */
public class Application implements Serializable {
    public static Application create(String name, String server, boolean tomcat) {
        String testSuffix;
        String browserSuffixSuffix;
        URL testUrl;
        URL browserUrl;
        
        if (tomcat) {
            testSuffix = ":8080";  // /xml/config not needed here, it's part of the test case
            browserSuffixSuffix = "/xml/config";
        } else {
            testSuffix = "";
            browserSuffixSuffix = "";
        }
        try {
            testUrl = new URL("http://" + server + testSuffix);
            browserUrl = new URL("http://" + server + testSuffix + browserSuffixSuffix); 
        } catch (MalformedURLException e) {
            throw new RuntimeException("TODO", e);
        }
        return new Application(name, server, testUrl, browserUrl);
    }
    
    //--
    
    private final String name;
    private final String server;
    
    /** initial url for tests */
    private final URL testUrl;

    /** to launch application in a browser */
    private final URL browserUrl;
    
    public Application() {
        throw new IllegalStateException("TODO: castor");
    }
    
    public Application(String name, String server, URL testUrl, URL browserUrl) {
        this.name = name;
        this.server = server;
        this.testUrl = testUrl;
        this.browserUrl = browserUrl;
    }
    
    public String getName() {
        return name;
    }
    
    /** server as stored in session */
    public String getServer() {
        return server;
    }

    public URL getTestUrl() {
        return testUrl;
    }
    
    public URL getBrowserUrl() {
        return browserUrl;
    }
}
