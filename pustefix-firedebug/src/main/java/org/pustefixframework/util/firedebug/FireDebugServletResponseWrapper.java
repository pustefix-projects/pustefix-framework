package org.pustefixframework.util.firedebug;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * FireDebugServletResponseWrapper
 * 
 * FireDebugServletResponseWrapper is needed to wrap the HttpServletResponse in
 * order to set the response header after Pustefix has finished, because
 * otherwise Pustefix already sends the response.
 * 
 * @author Holger Rüprich
 */

public class FireDebugServletResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream output;

    public FireDebugServletResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    public byte[] getData() {
        return output.toByteArray();
    }

    public ServletOutputStream getOutputStream() {
        return new FilterServletOutputStream(output);
    }

    public PrintWriter getWriter() {
        return new PrintWriter(getOutputStream(), true);
    }

    class FilterServletOutputStream extends ServletOutputStream {
        private DataOutputStream stream;

        public FilterServletOutputStream(OutputStream output) {
            stream = new DataOutputStream(output);
        }

        public void write(int b) throws IOException {
            stream.write(b);
        }

        public void write(byte[] b) throws IOException {
            stream.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            stream.write(b, off, len);
        }
    }
}
