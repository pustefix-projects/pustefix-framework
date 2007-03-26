/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.multipart;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import junit.framework.TestCase;

/**
 * This class tests the RFC822Headers class by parsing
 * some example headers and recorded multipart/form-data 
 * requests made by different browsers
 * 
 * @author mleidig@schlund.de
 */
public class RFC822HeadersTest extends TestCase {
    
	static final byte CR=0x0D;
    static final byte LF=0x0A;
    static final byte[] CRLF={CR,LF};
	
    public void testComplexHeader() throws Exception {
        InputStream in=getInputStream("complex_header.dump");
        InternetHeaders mailHeaders=new InternetHeaders(in);
        HashSet<String> headerSet=new HashSet<String>();
        Enumeration headersEnum=mailHeaders.getAllHeaders();
        while(headersEnum.hasMoreElements()) {
            Header header=(Header)headersEnum.nextElement();
            String name=header.getName();
            headerSet.add(name);
        }
        in.close();
        in=getInputStream("complex_header.dump");
        RFC822Headers headers=new RFC822Headers(in,null);
        in.close();
        Iterator<String> it=headerSet.iterator();
        while(it.hasNext()) {
            String name=it.next();
            String[] refValues=mailHeaders.getHeader(name);
            String[] values=headers.getHeader(name);
            assertNotNull(values);
            if(values.length!=refValues.length) assertEquals(values.length,refValues.length);
            else for(int i=0;i<values.length;i++) assertEquals(values[i],refValues[i]);
        }
    }
    
    public void testIEWinUpload() throws Exception {
    	String boundary="-----------------------------7d6203aa006c";
    	String filename="ie_win_upload.dump";
    	uploadTest(boundary,filename);
    }
    
    public void testFFWinUpload() throws Exception {
    	String boundary="-----------------------------41184676334";
    	String filename="ff_win_upload.dump";
    	uploadTest(boundary,filename);
    }
    
    public void testFFLinuxUpload() throws Exception {
    	String boundary="-----------------------------1383361789892271212848029586";
    	String filename="ff_linux_upload.dump";
    	uploadTest(boundary,filename);
    }
    
    private void uploadTest(String boundary,String filename) throws Exception {
    	InputStream in=getInputStream(filename);
    	List<Part> refParts=parseMultipart(in,boundary,false);
    	in=getInputStream(filename);
    	List<Part> parts=parseMultipart(in,boundary,true);
    	assertEquals(parts.size(),refParts.size());
    	for(int i=0;i<parts.size();i++) {
            byte[] body=parts.get(i).body;
            byte[] refBody=refParts.get(i).body;
    		assertEquals(body.length,refBody.length);
            for(int j=0;j<body.length;j++) assertEquals(body[j],refBody[j]);
    		assertEquals(parts.get(i).ctype,refParts.get(i).ctype);
    	}
    }
    
    private InputStream getInputStream(String fileName) {
        InputStream in=getClass().getClassLoader().getResourceAsStream("de/schlund/pfixxml/multipart/"+fileName);
        if(in==null) {
            try {
                in=new FileInputStream("tests/junit/de/schlund/pfixxml/multipart/"+fileName);
            } catch(FileNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return in;
    }
    
    private List<Part> parseMultipart(InputStream in,String boundary,boolean custom) throws IOException,MessagingException {
    	ArrayList<Part> parts=new ArrayList<Part>();
    	ByteArrayOutputStream body=new ByteArrayOutputStream();
    	ByteArrayOutputStream out=new ByteArrayOutputStream();
    	boolean endBoundary=false;
    	int b=-1;
    	while((b=in.read())!=-1) {
    		 if(b==CR) {
    			 b=in.read();
    			 if(b==LF) {
    				 byte[] lineBytes=out.toByteArray();
    				 if(isBoundary(lineBytes,boundary)) {
    					 if(endBoundary) {
    						 byte[] bodyBytes=body.toByteArray();
    						 Part part=new Part();
    						 parts.add(part);
    						 part.body=bodyBytes;
    						 body.reset();
    						 if(custom) {
    							 RFC822Headers headers=new RFC822Headers(in,"UTF-8");
    							 String vals[]=headers.getHeader("Content-Type");
    							 if(vals!=null&&vals.length>0) part.ctype=vals[0];
    							 vals=headers.getHeader("Content-Disposition");
    							 if(vals!=null&&vals.length>0) part.cdisp=vals[0];
    						 } else {
    							 InternetHeaders headers=new InternetHeaders(in);
    							 String[] vals=headers.getHeader("Content-Type");
    							 if(vals!=null&&vals.length>0) part.ctype=vals[0];
    							 vals=headers.getHeader("Content-Disposition");
    							 if(vals!=null&&vals.length>0) part.cdisp=vals[0];
    						 }
    					 }
    					 endBoundary=true;
    				 } else body.write(lineBytes);
    				 out.reset();
    			 } else {
    				 out.write(CR);
    				 out.write(b);
    			 }
    		 } else out.write(b);
    	}
    	return parts;
    }
    
    private boolean isBoundary(byte[] bytes,String boundary) {
    	if(bytes.length>=boundary.length()) {
    		for(int i=0;i<boundary.length();i++) if(bytes[i]!=boundary.charAt(i)) return false;
    		return true;
    	}
    	return false;
    }
    
    class Part {
    	byte[] body;
    	String ctype;
    	String cdisp;
    }
    
    public static void main(String[] args) throws Exception {
        RFC822HeadersTest test=new RFC822HeadersTest();
        test.testComplexHeader();
        test.testFFLinuxUpload();
        test.testFFWinUpload();
        test.testIEWinUpload();
    }

}
