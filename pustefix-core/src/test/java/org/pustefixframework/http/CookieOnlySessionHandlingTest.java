package org.pustefixframework.http;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;


public class CookieOnlySessionHandlingTest extends AbstractSessionHandlingTest {
    
    static {
        HTTP_PORT = findFreePort();
        HTTPS_PORT = findFreePort();
        try {
            createServer(HTTP_PORT, HTTPS_PORT, CookieOnlySessionTrackingStrategy.class);
        } catch(Exception x) {
            throw new RuntimeException("Error creating embedded server", x);
        }
    }
        
    public void testNoSessionHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpExSSL() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", CookieOnlySessionTrackingStrategy.COOKIE_SESSION_SSL, "true");
        cookie.setPath("/");
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
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
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                  
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(session, getSessionFromRequestCookie(method));

    }
    
    public void testInvalidHttpsSessionHttpExSSL() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", CookieOnlySessionTrackingStrategy.COOKIE_SESSION_SSL, "true");
        cookie.setPath("/");
        client.getState().addCookie(cookie);
        cookie = new Cookie("localhost", "AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME", "xyz");
        cookie.setPath("/");
        cookie.setSecure(true);
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
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
        assertEquals("http", getProtocol(location));
        assertEquals(HTTP_PORT, getPort(location));
                  
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        assertEquals(session, getSessionFromRequestCookie(method));

    }
    
    public void testNoSessionHttps() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testNoSessionHttpNoCookie() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testNoSessionHttpsNoCookie() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpToHttpsGET() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String insecureSession = getSessionFromResponseCookie(method);
        assertNull(insecureSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
  
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));

    }
    
    public void testNoSessionHttpToHttpsPOST() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new PostMethod("http://localhost:"+HTTP_PORT+"/ssl");
        ((PostMethod)method).setParameter("foo", "bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String insecureSession = getSessionFromResponseCookie(method);
        assertNotNull(insecureSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        String secureSession = getSessionFromResponseCookie(method);
        assertFalse(insecureSession.equals(secureSession));

    }
    
    public void testNoSessionHttpToHttpsNoCookies() throws Exception {
        
        HttpClient client = new HttpClient();

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        String insecureSession = getSessionFromResponseCookie(method);
        assertNull(insecureSession);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionHttp() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionHttpForceLocal() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar&__forcelocal=1");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionCookieHttp() throws Exception {
        
        HttpClient client = new HttpClient();

        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);
        
        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/?foo=bar");
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionHttps() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);
        cookie = new Cookie("localhost", "_PFXSSL_", "true");
        cookie.setPath("/");
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"?foo=bar");
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionHttpsForceLocal() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("https://localhost:"+HTTPS_PORT+"?foo=bar&__forcelocal=1");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
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
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
    }
    
    public void testInvalidSessionHttpToHttps() throws Exception {
        
        HttpClient client = new HttpClient();
        
        Cookie cookie = new Cookie("localhost", AbstractPustefixRequestHandler.DEFAULT_SESSION_COOKIE_NAME, "xyz");
        cookie.setPath("/");
        client.getState().addCookie(cookie);

        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        assertNull(getSession(location));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
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
        assertNull(getSession(location));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
                   
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
            
    }
    
    public void testHttpToHttpsBackButton() throws Exception {
        
        HttpClient client = new HttpClient();

        //get HTTP session
        
        HttpMethod method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        int statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_OK, statusCode);
        String session = getSessionFromResponseCookie(method);
        assertNotNull(session);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(1, getCount(method.getResponseBodyAsString()));
        
        //get HTTPS session
        
        method = new GetMethod("http://localhost:"+HTTP_PORT+"/ssl?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String location = method.getResponseHeader("Location").getValue();
        assertNull(getSession(location));
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));

        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        statusCode = client.executeMethod(method);

        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(2, getCount(method.getResponseBodyAsString()));
        
        //do first HTTP request again
        
        method = new GetMethod("http://localhost:"+HTTP_PORT+"?foo=bar");
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY); 
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        assertEquals("https", getProtocol(location));
        assertEquals(HTTPS_PORT, getPort(location));
        
        method.releaseConnection();
        method = new GetMethod(location);
        method.setFollowRedirects(false);
        method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            
        statusCode = client.executeMethod(method);
        
        assertEquals(HttpStatus.SC_OK, statusCode);
        assertTrue(method.getResponseBodyAsString().contains("<body>test</body>"));
        assertTrue(method.getResponseBodyAsString().contains("<!--foo=bar-->"));
        assertEquals(3, getCount(method.getResponseBodyAsString()));
    }

}
