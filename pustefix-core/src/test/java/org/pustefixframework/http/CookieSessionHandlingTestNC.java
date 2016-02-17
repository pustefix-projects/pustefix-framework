package org.pustefixframework.http;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;

public class CookieSessionHandlingTestNC extends CookieSessionHandlingTest {

    static {
        HTTP_PORT = findFreePort();
        HTTPS_PORT = findFreePort();
        try {
            server = createServer(HTTP_PORT, HTTPS_PORT, CookieSessionTrackingStrategy.class, true);
        } catch(Exception x) {
            throw new RuntimeException("Error creating embedded server", x);
        }
    }
    
    public void testNoSessionHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?nossl&foo=bar");
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
        assertEquals(session, getSession(method.getURI().toString()));

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
        
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(session, getSession(method.getURI().toString()));
        
    }
    
    public void testNoSessionHttpToHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String insecureSession = getSession(location);
        assertNotNull(insecureSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String newSession = getSession(location);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
            
       
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(newSession, getSession(method.getURI().toString()));
        
    }
    
    public void testInvalidSessionHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/;jsessionid=xyz?nossl&foo=bar");
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
        assertEquals(session, getSession(method.getURI().toString()));

    }
    
    public void testInvalidSessionCookieHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);
        
        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/?nossl&foo=bar");
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
        assertEquals(session, getSession(method.getURI().toString()));
        
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(session, getSession(method.getURI().toString()));
        
    }
    
    public void testInvalidSessionCookieHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);
        
        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"/?foo=bar");
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(session, getSession(method.getURI().toString()));
        
    }
    
    public void testInvalidSessionHttpToHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/;jsessionid=xyz?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        assertNull(getSession(location));
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String oldSession = getSession(location);
        assertNotNull(oldSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        location = method.getResponseHeader("Location").getValue();
        String session = getSession(location);
        assertNotNull(session);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        assertFalse(oldSession.equals(session));
            
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(session, getSession(method.getURI().toString()));
    }
    
}
