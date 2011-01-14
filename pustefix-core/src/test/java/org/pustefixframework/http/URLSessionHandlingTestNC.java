package org.pustefixframework.http;


/**
 * Test URL rewrite session handling with cookies disabled for 
 * session tracking on the Servlet API.
 * 
 * @author mleidig@schlund.de
 *
 */
public class URLSessionHandlingTestNC extends URLSessionHandlingTest {
    
    static {
        HTTP_PORT = findFreePort();
        HTTPS_PORT = findFreePort();
        try {
            server = createServer(HTTP_PORT, HTTPS_PORT, URLRewriteSessionTrackingStrategy.class, true);
        } catch(Exception x) {
            throw new RuntimeException("Error creating embedded server", x);
        }
    }
    
    public URLSessionHandlingTestNC() {
        cookieSessionHandlingDisabled = true;
    }
    
}
