package org.pustefixframework.web;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public abstract class AbstractSessionHandlingTest extends AbstractIntegrationTest {

    Pattern PATTERN_URL = Pattern.compile("(https?)://(([^:]*)(:(\\d+))?)(/[^;?]*)?/?(;" +
                                            "jsessionid=([^?]+))?(\\?.*)?");
    Pattern PATTERN_COUNT = Pattern.compile(".*<!--(\\d+)-->.*");
    Pattern PATTERN_PARAM = Pattern.compile("<!--foo=(\\w+)-->");
    Pattern PATTERN_HREF = Pattern.compile("href=\"([^\"]+)\"");
    Pattern PATTERN_COOKIE_SESSION = Pattern.compile("JSESSIONID=([^;]*);.*");

    int getPort(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if(matcher.matches()) return Integer.parseInt(matcher.group(5));
        return 80;
    }

    String getProtocol(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if(matcher.matches()) return matcher.group(1);
        return null;
    }

    int getCount(String content) {
        Matcher matcher = PATTERN_COUNT.matcher(content);
        if(matcher.find()) return Integer.parseInt(matcher.group(1));
        return 0;
    }

    String getParam(String content) {
        Matcher matcher = PATTERN_PARAM.matcher(content);
        if(matcher.find()) return matcher.group(1);
        return null;
    }

    boolean checkEncodedLinks(String content, boolean requiresEncoding) {
        Matcher matcher = PATTERN_HREF.matcher(content);
        while(matcher.find()) {
            if(matcher.group(1).contains(";jsessionid=") ^ requiresEncoding) {
                return false;
            }
        }
        return true;
    }

    public class Client {

        HttpClient client;

        public Client() {
            client = new HttpClient();
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        }

        public Client disableCookies() {
            client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            return this;
        }

        public Client userAgent(String userAgent) {
            client.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
            return this;
        }

        public void addCookie(String domain, String path, boolean secure, String cookieName, String cookieValue) {
            Cookie cookie = new Cookie(domain, cookieName, cookieValue);
            cookie.setPath(path);
            cookie.setSecure(secure);
            client.getState().addCookie(cookie);
        }

        public void addCookie(String cookieName, String cookieValue) {
            addCookie("localhost", "/", false, cookieName, cookieValue);
        }

        public void addCookie(String cookieName, String cookieValue, boolean secure) {
            addCookie("localhost", "/", secure, cookieName, cookieValue);
        }

        public ClientMethod doGet(String url) throws Exception {
            HttpMethod method = new GetMethod(url);
            method.setFollowRedirects(false);
            client.executeMethod(method);
            return new ClientMethod(method);
        }

        public ClientMethod followRedirect(ClientMethod origMethod) throws Exception {
            HttpMethod method = new GetMethod(origMethod.method.getResponseHeader("Location").getValue());
            method.setFollowRedirects(false);;
            client.executeMethod(method);
            return new ClientMethod(method);
        }

        public Client hasNoSessionCookie() {
            Cookie cookie = getSessionCookie();
            assertNull(cookie);
            return this;
        }

        public Client hasSessionCookie() {
            Cookie cookie = getSessionCookie();
            assertNotNull(cookie);
            return this;
        }

        public Client hasSecureSessionCookie() {
            Cookie cookie = getSessionCookie();
            assertNotNull(cookie);
            assertTrue(cookie.getSecure());
            return this;
        }

        private Cookie getSessionCookie() {
            Cookie[] cookies = client.getState().getCookies();
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals("JSESSIONID")) {
                    return cookie;
                }
            }
            return null;
        }

        public ClientMethod followLink(ClientMethod origMethod) throws IOException {
            return followLink(origMethod, 0);
        }

        public ClientMethod followLink(ClientMethod origMethod, int no) throws IOException {
            int ind = 0;
            Matcher matcher = PATTERN_HREF.matcher(origMethod.method.getResponseBodyAsString());
            while(matcher.find()) {
                if(ind == no) {
                    URI uri = origMethod.method.getURI();
                    String link;
                    if(matcher.group(1).startsWith("http")) {
                        link = matcher.group(1);
                    } else {
                        link = uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + matcher.group(1);
                    }
                    HttpMethod method = new GetMethod(link);
                    method.setFollowRedirects(false);
                    client.executeMethod(method);
                    return new ClientMethod(method);
                }
                ind++;
            }
            throw new IllegalArgumentException("Link not found: " + no);
        }

    }

    public class ClientMethod {

        HttpMethod method;
        String location;

        ClientMethod(HttpMethod method) {
            this.method = method;
            Header header = method.getResponseHeader("Location");
            if(header != null) {
                location = header.getValue();
            }
        }

        public ClientMethod is200() {
            assertEquals(HttpStatus.SC_OK, method.getStatusCode());
            return this;
        }

        public ClientMethod is301() {
            assertEquals(HttpStatus.SC_MOVED_PERMANENTLY, method.getStatusCode());
            return this;
        }

        public ClientMethod is302() {
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, method.getStatusCode());
            return this;
        }

        public ClientMethod is307() {
            assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, method.getStatusCode());
            return this;
        }

        public ClientMethod clearsSessionCookie() {
            String session = getSessionFromResponseCookie(method);
            assertTrue(session != null && ( session.equals("\"\"") || session.isEmpty()));
            return this;
        }

        public ClientMethod setsSessionCookie() {
            String session = getSessionFromResponseCookie(method);
            assertTrue(session != null && !( session.equals("\"\"") || session.isEmpty()));
            return this;
        }

        public ClientMethod setsNoSessionCookie() {
            String session = getSessionFromResponseCookie(method);
            assertNull(session);
            return this;
        }

        private String getSessionFromResponseCookie(HttpMethod method) {
            Header[] headers = method.getResponseHeaders("Set-Cookie");
            for(Header header: headers) {
                String value = header.getValue();
                if(value != null) {
                    Matcher matcher = PATTERN_COOKIE_SESSION.matcher(value);
                    if(matcher.matches()) return matcher.group(1);
                }
            }
            return null;
        }

        public ClientMethod redirectsWithSessionParam() {
            assertTrue(location.contains("jsessionid"));
            return this;
        }

        public ClientMethod redirectsWithoutSessionParam() {
            assertFalse(location.contains("jsessionid"));
            return this;
        }

        public ClientMethod redirectsToHttps() {
            assertEquals("https", getProtocol(location));
            assertEquals(HTTPS_PORT, getPort(location));
            return this;
        }

        public ClientMethod isFirstVisit() throws IOException {
            assertEquals(1, getCount(method.getResponseBodyAsString()));
            return this;
        }

        public ClientMethod isSecondVisit() throws IOException {
            assertEquals(2, getCount(method.getResponseBodyAsString()));
            return this;
        }

        public ClientMethod isThirdVisit() throws IOException {
            assertEquals(3, getCount(method.getResponseBodyAsString()));
            return this;
        }

        public ClientMethod isFourthVisit() throws IOException {
            assertEquals(4, getCount(method.getResponseBodyAsString()));
            return this;
        }

        public ClientMethod hasContent() throws IOException {
            String path = method.getPath();
            if(path.startsWith("/")) {
                path = path.substring(1);
            }
            int ind = path.indexOf(';');
            if(ind == -1) {
                ind = path.indexOf("?");
            }
            if(ind > -1) {
                path = path.substring(0, ind);
            }
            if(path.equals("")) {
                path = "home";
            }
            assertTrue(method.getResponseBodyAsString().contains("<h2>" + path + "</h2>"));
            return this;
        }

        public ClientMethod hasParam() throws IOException {
            assertEquals("bar", getParam(method.getResponseBodyAsString()));
            return this;
        }

        public ClientMethod hasNoParam() throws IOException {
            assertNull(getParam(method.getResponseBodyAsString()));
            return this;
        }

        public ClientMethod hasEncodedLinks() throws IOException {
            assertTrue(checkEncodedLinks(method.getResponseBodyAsString(), true));
            return this;
        }

        public ClientMethod hasNoEncodedLinks() throws IOException {
            assertTrue(checkEncodedLinks(method.getResponseBodyAsString(), false));
            return this;
        }

        public ClientMethod isSecure() throws URIException {
            assertEquals("https", method.getURI().getScheme());
            return this;
        }

        public ClientMethod isInsecure() throws URIException {
            assertEquals("http", method.getURI().getScheme());
            return this;
        }

        public void dump() {
            StringBuilder sb = new StringBuilder();
            try {
                sb.append(method.getURI());
            } catch (URIException e) {
                sb.append("-");
            }
            sb.append("\n\n");
            sb.append(method.getName()).append(" ").append(method.getPath()).append(method.getQueryString()).append("\n");
            Header[] headers = method.getRequestHeaders();
            for(Header header: headers) {
                sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
            }
            sb.append("\n");
            sb.append(method.getStatusLine()).append("\n");
            headers = method.getResponseHeaders();
            for(Header header: headers) {
                sb.append(header.getName()).append(": ").append(header.getValue()).append("\n");
            }
            Header ctypeHeader = method.getResponseHeader("Content-Type");
            if(ctypeHeader != null && ctypeHeader.getValue().startsWith("text/")) {
                try {
                    sb.append("\n").append(method.getResponseBodyAsString()).append("\n");
                } catch(IOException e) {
                    //ignore
                }
            }
            System.out.println(sb.toString());
        }
    }

}
