/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.multipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;

/**
 * This class is a parser for message headers (as defined in RFC 822),
 * especially for headers from form-based file uploads (as defined in RFC 1867).
 * Due to the fact that current browsers don't encode non-ASCII characters
 * (as they should do and as defined in RFC 1522), but send the raw bytes in
 * the encoding of the page, this class decodes the bytes according to the specified
 * encoding. Parsing is done by reading from an input stream and stopping
 * when the end of the header section is reached or the stream ends.
 *
 * @author mleidig@schlund.de
 */
public class RFC822Headers {

    static final byte CR=0x0D;
    static final byte LF=0x0A;
    static final byte[] CRLF={CR,LF};
    
    static final String DEFAULT_ENCODING="ISO-8859-1";
    
    String encoding;
    HashMap<String,String[]> headers;
    
    public RFC822Headers(InputStream in,String encoding) throws IOException {
    	this.encoding=encoding;
    	if(encoding==null) this.encoding=DEFAULT_ENCODING;
        headers=new HashMap<String,String[]>();
        read(in);
    }
    
    public Set<String> getHeaderNames() {
        return headers.keySet();
    }
    
    public String[] getHeader(String name) {
        return headers.get(name);
    }
    
    private void read(InputStream in) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] name=null;
        boolean inName=true;
        boolean eol=false;
        boolean eoh=false;
        int b=-1;
        while(!eoh&&(b=in.read())!=-1) {
            if(b==CR) {
                b=in.read();
                if(b!=LF) throw new IOException("RFC822 clash: CR has to be followed by a LF.");
                if(eol) {
                    if(name!=null) addHeader(name,out.toByteArray());
                    eoh=true;
                }
                eol=true;
            } else {
                if(eol) {
                    if(b==' '||b=='\t') {
                        out.write(CRLF);
                        out.write(b);
                    } else {
                        addHeader(name,out.toByteArray());
                        out.reset();
                        inName=true;
                        out.write(b);
                    }
                } else if(inName) {
                    if(b==':') {
                        name=out.toByteArray();
                        out.reset();
                        inName=false;
                    } else {
                        out.write(b);
                    }
                } else {
                    out.write(b);
                }
                eol=false;
            }
        }
    }
    
    private void addHeader(byte[] name,byte[] value) throws UnsupportedEncodingException {
        String nameStr=new String(name,encoding);
        nameStr=nameStr.trim();
        String valueStr=new String(value,encoding);
        valueStr=valueStr.trim();
        addHeader(nameStr,valueStr);
    }
    
    public void addHeader(String name,String value) {
        if(headers.containsKey(name)) {
            String[] oldVals=headers.get(name);
            String[] vals=new String[oldVals.length+1];
            for(int i=0;i<oldVals.length;i++) vals[i]=oldVals[i];
            vals[vals.length-1]=value;
            headers.put(name,vals);
        } else {
            String[] vals=new String[] {value};
            headers.put(name,vals);
        }
    }
    
}
