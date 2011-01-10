package org.pustefixframework.http;

/**
 * Test URL rewrite session handling with cookies disabled for 
 * session tracking on the Servlet API.
 * 
 * @author mleidig@schlund.de
 *
 */
public class URLSessionHandlingTestNC extends URLSessionHandlingTest {

    @Override
    protected void setUp() throws Exception {
        setUp(URLRewriteSessionTrackingStrategy.class, true);
        cookieSessionHandlingDisabled = true;
    }
    
}
