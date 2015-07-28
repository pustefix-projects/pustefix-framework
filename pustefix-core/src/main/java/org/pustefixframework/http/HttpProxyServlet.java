package org.pustefixframework.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple HTTP proxy servlet (including header rewriting support).
 * 
 * Translates servlet relative path to target URL, e.g. the servlet 
 * can be mapped to "/foo/*" and the target URL can be set via
 * init-parameter to "http://some.server/foo/".
 *
 */
public class HttpProxyServlet extends HttpServlet {

    private static final long serialVersionUID = 6744329961357727918L;

    private String proxyURL;

    @Override
    public void init() throws ServletException {
        proxyURL = getInitParameter("url");
        if(!proxyURL.endsWith("/")) {
            proxyURL += "/";
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(proxyURL);
        if(req.getPathInfo() != null) {
            urlBuilder.append(req.getPathInfo().substring(1));
        }
        if(req.getRequestedSessionId() != null && req.isRequestedSessionIdFromURL()) {
            urlBuilder.append(";jsessionid=").append(req.getRequestedSessionId());
        }
        if(req.getQueryString() != null) {
            urlBuilder.append("?").append(req.getQueryString());
        }
        URL url = new URL(urlBuilder.toString());

        urlBuilder = new StringBuilder();
        urlBuilder.append(req.getScheme()).append("://").append(req.getServerName());
        if(req.getServerPort() != 80 && req.getServerPort() != 443) {
            urlBuilder.append(":").append(req.getServerPort());
        }
        urlBuilder.append(req.getContextPath()).append(req.getServletPath()).append("/");
        String rewriteURL = urlBuilder.toString();

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setInstanceFollowRedirects(false);
        con.setConnectTimeout(3000);
        con.setReadTimeout(10000);
        con.setUseCaches(false);
        con.setRequestMethod(req.getMethod());
        boolean hasForwardedFor = false;
        Enumeration<String> headerNames = req.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = req.getHeaders(headerName);
            while(headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                if(headerName.equalsIgnoreCase("X-Forwarded-For")) {
                    headerValue += "," + req.getRemoteAddr();
                    hasForwardedFor = true;
                }
                con.setRequestProperty(headerName, headerValue);
            }
        }
        if(!hasForwardedFor) {
            con.setRequestProperty("X-Forwarded-For", req.getRemoteAddr());
        }

        if(req.getMethod().equals("POST")) {
            con.setDoOutput(true);
            InputStream in = req.getInputStream();
            if(in != null) {
                OutputStream out = con.getOutputStream();
                byte[] buffer = new byte[4096];
                int no = 0;
                try {
                    while ((no = in.read(buffer)) != -1) {
                        out.write(buffer, 0, no);
                    }
                    out.flush();
                } finally {
                    in.close();
                    out.close();
                }
            }
        }

        int status = con.getResponseCode();
        res.setStatus(status);
        Map<String, List<String>> headerFields = con.getHeaderFields();
        boolean chunked = false;
        if("chunked".equalsIgnoreCase(con.getHeaderField("Transfer-Encoding")) && con.getHeaderField("Content-Length") == null) {
            chunked = true;
        }
        for(Map.Entry<String, List<String>> headerField : headerFields.entrySet()) {
            if(headerField.getKey() != null && !(headerField.getKey().equals("Transfer-Encoding") && chunked)) {
                for(String headerValue : headerField.getValue()) {
                    headerValue = headerValue.replace(proxyURL, rewriteURL);
                    res.addHeader(headerField.getKey(), headerValue);
                }
            }
        }
        
        if(con.getContentLengthLong() > 0 || con.getContentLengthLong() == -1) {
            InputStream in;
            if(status >= 400) {
                in = con.getErrorStream();
            } else {
                in = con.getInputStream();
            }
            OutputStream out = res.getOutputStream();
            byte[] buffer = new byte[4096];
            int no = 0;
            try {
                while ((no = in.read(buffer)) != -1) {
                    out.write(buffer, 0, no);
                }
            } finally {
                in.close();
                out.close();
            }
        }
    }

}