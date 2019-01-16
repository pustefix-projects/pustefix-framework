package org.pustefixframework.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class VirtualHttpServletResponse implements HttpServletResponse {
    
    private final static String DEFAULT_CHARSET = "ISO-8859-1";
    
    private String charset = DEFAULT_CHARSET;
    private String contentType;
    private ServletOutputStream outputStream;
    private PrintWriter writer;
    private ByteArrayOutputStream out;
    private Map<String, List<String>> headers = new HashMap<>();
    private int status = HttpServletResponse.SC_OK;
    private boolean committed;
    private int bufferSize;
    private Locale locale = Locale.getDefault();
    private List<Cookie> cookies = new ArrayList<>();
    
    @Override
    public String getCharacterEncoding() {
        return charset;
    }
    
    @Override
    public void setCharacterEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
    
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if(writer != null) {
            throw new IllegalStateException("getWriter already called");
        }
        if(outputStream == null) {
            out = new ByteArrayOutputStream();
            outputStream = new ByteArrayServletOutputStream(out);
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if(outputStream != null) {
            throw new IllegalStateException("getOutputStream already called");
        }
        if(writer == null) {
            out = new ByteArrayOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(out, charset));
        }
        return writer;
    }
    
    public byte[] getOutput() {
        if(out != null) {
            return out.toByteArray();
        }
        return null;
    }
  
    @Override
    public void setContentLength(int contentLength) {
        setIntHeader("Content-Length", contentLength);
    }

   

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void flushBuffer() throws IOException {
        committed = true;
    }

    @Override
    public void resetBuffer() {
        if(committed) {
            throw new IllegalStateException("Response was already committed.");
        }
        out.reset();
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public void reset() {
        if(committed) {
            throw new IllegalStateException("Response was already committed.");
        }
        resetBuffer();
        charset = DEFAULT_CHARSET;
        contentType = null;
        headers.clear();
        locale = Locale.getDefault();
        headers.clear();
        cookies.clear();
        
//        private String charset = DEFAULT_CHARSET;
//        private String contentType;
//        private ServletOutputStream outputStream;
//        private PrintWriter writer;
//        private ByteArrayOutputStream out;
//        private Map<String, List<String>> headers = new HashMap<>();
//        private int status = HttpServletResponse.SC_OK;
//        private boolean committed;
//        private int bufferSize;
//        private Locale locale = Locale.getDefault();
//        private List<Cookie> cookies = new ArrayList<>();
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url;
    }

    @Override
    public String encodeUrl(String url) {
        return url;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return url;
    }

    @Override
    public void sendError(int status, String message) throws IOException {
        if(committed) {
            throw new IllegalStateException("Response was already committed.");
        }
        setStatus(status);
        committed = true;
    }

    @Override
    public void sendError(int status) throws IOException {
        if(committed) {
            throw new IllegalStateException("Response was already committed.");
        }
        setStatus(status);
        committed = true;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if(committed) {
            throw new IllegalStateException("Response was already committed.");
        }
        setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        setHeader("Location", location);
        committed = true;
    }

    @Override
    public void setDateHeader(String name, long date) {
        setHeader(name, String.valueOf(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        addHeader(name, String.valueOf(date));
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<String>();
        values.add(value);
        headers.put(name, values);
    }

    @Override
    public void addHeader(String name, String value) {
        List<String> values = headers.get(name);
        if(values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        values.add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int status) {
        if(!committed) {
            this.status = status;
        }
    }

    @Override
    public void setStatus(int status, String message) {
        if(!committed) {
            this.status = status;
        }
    }
        
    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        if(values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<String> values = headers.get(name);
        if(values != null) {
            List<String> copy = new ArrayList<String>(values.size());
            copy.addAll(values);
            return copy;
        }
        return new ArrayList<String>(0);
    }

    @Override
    public Collection<String> getHeaderNames() {
        Set<String> keys = headers.keySet();
        List<String> copy = new ArrayList<String>(keys.size());
        copy.addAll(keys);
        return copy;
    }
    
    
    class ByteArrayServletOutputStream extends ServletOutputStream {
        
        private ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        ByteArrayServletOutputStream(ByteArrayOutputStream out) {
            this.out = out;
        }
        
        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public boolean isReady() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }

    }


    @Override
    public void setContentLengthLong(long len) {
        throw new UnsupportedOperationException();
    }

}
