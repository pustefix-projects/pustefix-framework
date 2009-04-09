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
 */

package de.schlund.pfixxml.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletInputStream;

/**
 * This class wrapes a {@link javax.servlet.ServletInputStream ServletInputStream}
 * and takes care of boundaries from a multipart message.
 * <p/>
 * <b>Attention</b><br/>
 * This class is not reentrant!
 *
 *
 */

public class MultipartStream extends InputStream {

    public static final int DEF_BUFFER_SIZE = 1024;
    public static final String DEF_CHAR_ENC = "ISO-8859-1";
    public static final long DEF_MAX_PART_SIZE = 10 * 1024 * 1024; // 10 MB
    public static final int CR = 13;
    public static final int LF = 10;
    public static final int DASH = 45;
    public static final int STATE_BEGIN = 0;
    public static final int STATE_CR = 1;
    public static final int STATE_LF = 2;
    public static final int STATE_TEST = 3;
        
    private PushbackInputStream in = null;
    private int[] buf = null;
    private int[] boundaryArray = null;
    private int boundaryLength = -1;
    private int bufSize = DEF_BUFFER_SIZE;
    private boolean eom = false;
    private boolean eop = false;
 
    private long maxPartSize = DEF_MAX_PART_SIZE;
    private long partSize = 0;
    
    /**
     * Constructor for MultipartStream.
     */
    public MultipartStream() {
        this(null, -1);
    }

    public MultipartStream(ServletInputStream inS) {
        this(inS, -1);
    }

    public MultipartStream(ServletInputStream inS, int bSize) {
        super();
        bufSize = bSize;
        if (bSize <= 0) bufSize = DEF_BUFFER_SIZE;
        buf = new int[bufSize];
        in = new PushbackInputStream(inS, bufSize);
        eom = false;
        eop = false;
        boundaryArray = new int[0];
    }
    
    public void setServletInputStream(ServletInputStream inS) {
        in = new PushbackInputStream(inS, bufSize);
    }
    
    public void setMaxPartSize(long pSize) {
        maxPartSize = pSize;
    }
    
    public long getMaxPartSize() {
        return maxPartSize;
    }
    
    public void setBoundary(String bParam) {
        if (bParam == null) throw new IllegalArgumentException("Given boundary is null");
        
        boundaryLength = bParam.length() + 2;
        String boundary = "--" + bParam + "--";
        eom = false;
        eop = false;
        partSize = 0;
        try {
            byte[] tmpArr = boundary.getBytes("ISO-8859-1");
            if (boundaryArray == null || boundaryArray.length < (tmpArr.length)) {
                boundaryArray = new int[tmpArr.length];                
            }
            for (int i = 0; i < boundaryArray.length; i++) {
                boundaryArray[i] = tmpArr[i];
            }
            if (buf == null || buf.length < (boundaryArray.length + 1)) {
                buf = new int[boundaryArray.length + 1];
            }
        } catch (UnsupportedEncodingException e) {
        }
    }

    public void skipBoundary() {
        if (boundaryArray == null || boundaryArray.length == 0) throw new IllegalStateException("Have no boundary");
        if (in == null) throw new IllegalStateException("Have no stream");
        
        boolean doLoop = true;
        
        int tmpVal = -1;
        int compIdx = 0;
        partSize = 0;
        eop = false;
        eom = false;
        while (doLoop) {
            tmpVal = getByteFromStream();
            if (tmpVal != -1) {
                if (boundaryArray[compIdx++] == tmpVal) {
                    if (boundaryLength == compIdx) {
                        eop = true;
                        eom = false;
                    } else if (boundaryArray.length == compIdx) {
                        eop = false;
                        eom = true;
                        doLoop = false;
                    }
                } else if (eop) {
                    doLoop = false;
                    try {
                        in.unread(tmpVal);
                    } catch (IOException e) {
                        eop = false;
                        eom = true;
                    }
                } else {
                    gotoStartOfLine();
                    compIdx = 0;
                }
            } else {
                eom = true;
                eop = false;
                doLoop = false;
            }
        }
        if (tmpVal != -1) {
            gotoStartOfLine();
            eop = false;
            eom = false;    
        }
    }

    protected void gotoStartOfLine() {
        int tmpVal = -1;
        boolean doLoop = true;
        boolean found = false;
        while (doLoop) {
            tmpVal = getByteFromStream();
            if (0 <= tmpVal) {
                if (tmpVal == CR || tmpVal == LF) {
                    found = true;
                } else if (found) {
                    doLoop = false;
                    try {
                        in.unread(tmpVal);
                        partSize--;
                        if (partSize < 0) partSize = 0;
                    } catch (IOException e) {
                        eop = false;
                        eom = true;
                    }
                }
            } else {
                doLoop = false;
                eop = false;
                eom = true;
            }
        }
    }
    
    public boolean isEndOfPart() {
        return eop;
    }
    
    public boolean isEndOfMultipart() {
        return eom;
    }

    /**
     * @see InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int rc = -1;
        if (isEndOfPart() || isEndOfMultipart()) return rc;
        
        if (maxPartSize < partSize) throw new PartToLongException("Maximum part size of " + maxPartSize + " exeeded");
        rc = in.read();
        partSize++;
        if (rc == CR || rc == LF) {
            rc = checkPartBoundary(rc);
        }
 
        return rc;
    }
    
    private int checkPartBoundary(int val) 
        throws IOException {
        int rc = val;
        boolean doLoop = (0 <= val);
        if (doLoop) {
            in.unread(val);
        }
                
        eop = false;
        eom = false;
        int compIdx = 0;
        int writeIdx = 0;
        int state = STATE_BEGIN;
        boolean innerLoop = true;
        while (doLoop) {
            val = getByteFromStream();
            if (0 <= val) {
                buf[writeIdx++] = val;
                innerLoop = true;
                while (innerLoop) {
                    switch (state) {
                        case STATE_BEGIN:
                            if (val == CR) {
                                state = STATE_CR;
                                innerLoop = false;
                            } else if (val == LF) {
                                state = STATE_LF;
                                innerLoop = false;
                            } else {
                                state = STATE_TEST;
                            }
                            break;
                        case STATE_CR:
                            if (val == LF) {
                                state = STATE_LF;
                                innerLoop = false;
                            } else {
                                state = STATE_TEST;
                            }                                
                            break;
                        case STATE_LF:
                            if (val == CR || val == LF) {
                                innerLoop = false;
                                doLoop = false;
                            } else {
                                state = STATE_TEST;
                            }
                            break;
                        case STATE_TEST:
                            if (boundaryArray[compIdx++] == val) {
                                if (boundaryLength == compIdx) {
                                    eop = true;
                                    eom = false;
                                } else if (boundaryArray.length == compIdx) {
                                    eop = false;
                                    eom = true;
                                    doLoop = false;
                                }
                            } else {
                                doLoop = false;
                            }
                            innerLoop = false;
                            break;
                    }
                }
            } else {
                doLoop = false;
            }
        }
        if (eop || eom) {
            pushbackIntArray(buf, 0, writeIdx);
            rc = -1;
        } else if (0 < --writeIdx) {
            pushbackIntArray(buf, 1, writeIdx);
        }
        return rc;
    }

    /**
     * @see InputStream#available()
     */
    @Override
    public int available() throws IOException {
        return in.available();
    }

    /**
     * @see InputStream#close()
     */
    @Override
    public void close() throws IOException {
        in.close();
    }

    protected int getByteFromStream() {
        int rc = -1;
        try {
            rc = in.read();
        } catch (IOException e) {
            rc = -1;
        }
        return rc;
    }
    
    protected void pushbackIntArray(int[] val, int start, int length) 
        throws IOException {

        int startLoop = start + length - 1;
        for (int i = startLoop; start <= i; i--) {
            in.unread(val[i]);
        }
    }
}
