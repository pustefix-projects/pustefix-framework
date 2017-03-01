package org.pustefixframework.http;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;


public class URLSessionHandlingTest extends AbstractSessionHandlingTest {
   
    static {
        HTTP_PORT = findFreePort();
        HTTPS_PORT = findFreePort();
        try {
            createServer(HTTP_PORT, HTTPS_PORT, URLRewriteSessionTrackingStrategy.class);
        } catch(Exception x) {
            throw new RuntimeException("Error creating embedded server", x);
        }
    }
    
    protected boolean cookieSessionHandlingDisabled = false;
    
    public void testNoSessionHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                  
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        if(!cookieSessionHandlingDisabled) {
            Header[] headers = method.getResponseHeaders("Set-Cookie");
            for(Header header: headers) {
                if(header.getValue().contains(AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME)) {
                    assertEquals(AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME + "=" + session + ";Path=/;Expires=Thu, 01-Jan-1970 00:00:00 GMT", header.getValue());
                }
            }
        }

    }
    
    public void testNoSessionHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertFalse(session.equals(newSession));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpNoCookie() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpsNoCookie() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertFalse(session.equals(newSession));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpToHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        assertFalse(session.equals(newSession));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpToHttpsNoCookies() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        assertFalse(session.equals(newSession));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
            
    }
    
    public void testInvalidSessionHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        session = getSession(location);
        assertNotNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testInvalidSessionHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"/;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertFalse(session.equals(newSession));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testInvalidSessionHttpNoCookie() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        session = getSession(location);
        assertNotNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testInvalidSessionHttpsNoCookie() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"/;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertFalse(session.equals(newSession));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionHttpToHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        assertFalse(session.equals(newSession));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testInvalidSessionHttpToHttpsNoCookies() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        assertFalse(session.equals(newSession));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
            
    }
    
    public void testOldSessionHttpToHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
       
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
        method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl;jsessionid=" + session + "?foo=bar");
        String oldLocation = location;
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
       
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));

        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
     
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(2, getCount(method.getResponseBodyAsString()));
        
        method = new GetMethod(oldLocation);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
       
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        newSession = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method.releaseConnection();
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        newSession = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method.releaseConnection();
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(3, getCount(method.getResponseBodyAsString()));
    }
    
    public void testOldSessionHttpToHttpsNoCookies() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
     
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
        method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl;jsessionid=" + session + "?foo=bar");
        String oldLocation = location;
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
       
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));

        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(2, getCount(method.getResponseBodyAsString()));
        
        method = new GetMethod(oldLocation);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
        
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
            
        assertEquals(HttpStatus.SC_OK, statusCode);
        newSession = getSession(location);
        assertNotNull(newSession);
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
    }

}
