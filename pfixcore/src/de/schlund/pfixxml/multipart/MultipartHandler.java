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



import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeUtility;
import javax.servlet.GenericServlet;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;

import de.schlund.pfixxml.AbstractXMLServlet;

/**
 *
 *
 */

public class MultipartHandler {

    public static final String CTYPE_HEADER = "Content-Type";
    public static final String MULTI_FORM_DATA = "multipart/form-data";
    public static final String PARAM_BOUNDARY = "boundary";
    public static final String DEFAULT_CHARSET = "iso-8859-1";
    public static final String DEFAULT_TRANS_ENC = "7bit";
    public static final String DEFAULT_CTYPE = "text/plain; charset=" + DEFAULT_CHARSET;
    public static final String CHARSET_PARAM = "charset";
    public static final String CONTENT_DISP_HEADER = "Content-Disposition";
    public static final String CONTENT_TRANS_ENC_HEADER = "Content-Transfer-Encoding";
    public static final String NAME_PARAM = "name";
    public static final String FILENAME_PARAM = "filename";
    public static final String CREATION_DATE_PARAM = "creation-date";
    public static final String MODIFICATION_DATE_PARAM = "modification-date";
    public static final String READ_DATE_PARAM = "read-date";
    public static final String SIZE_PARAM = "size";
    public static final String FNAME_PATTERN = "00000000";

    private final static Object lock = new Object();
    private final static DecimalFormat format = new DecimalFormat(FNAME_PATTERN);

    private HttpServletRequest req = null;
    private String dir = null;
    private File dirFile = null;
    private HashMap<String,ArrayList<PartData>> parameter = null;
    private GenericServlet logBase = null;
    private ArrayList<FileData> fileuploads = null;
    private List<Exception> failedParts = null;
    private long maxPartSize = -1;

    private final static Logger LOG = Logger.getLogger(MultipartHandler.class);

    protected static File getDestFile(File dir, String fName) throws IOException {
        TempFile rc = null;
        synchronized (lock) {
            StringBuffer buf = new StringBuffer(fName);
            buf.append('-');
            long curSec = new Date().getTime();
            buf.append(curSec).append(FNAME_PATTERN);
            rc = new TempFile(dir, buf.toString());
            if (rc.exists()) {
                long count = 1;
                String base = buf.substring(0, buf.length() - FNAME_PATTERN.length());
                while (rc.exists()) {
                    rc = new TempFile(dir, base + format.format(count++));
                }
            }
            rc.createNewFile();
        }
        return rc;
    }

    public MultipartHandler(HttpServletRequest pReq, String pDir) throws IllegalArgumentException {
        this(null, pReq, pDir);
    }

    /**
     * Constructor for MulitpartHandler.
     */
    public MultipartHandler(GenericServlet base, HttpServletRequest pReq, String pDir)
        throws IllegalArgumentException {
        logBase = base;
        req = pReq;
        dir = pDir;
        if (req == null) {
            throw new IllegalArgumentException("Got no request req == null.");
        }

        if (!req.getContentType().toLowerCase().startsWith(MULTI_FORM_DATA)) {
            throw new IllegalArgumentException("Request is not a multipart/form-data");
        }

        if (dir == null) {
            dir = System.getProperty(AbstractXMLServlet.DEF_PROP_TMPDIR);
        }
        dirFile = new File(dir);
        if (!dirFile.isDirectory()) {
            throw new IllegalArgumentException("File '" + dir + "' isn't a directory");
        }
        if (!dirFile.canWrite()) {
            throw new IllegalArgumentException("Can't write to dir '" + dir + "'.");
        }
        parameter = new HashMap<String,ArrayList<PartData>>();
        fileuploads = new ArrayList<FileData>();
        failedParts = new ArrayList<Exception>();
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameter.keySet());
    }

    public PartData getParameter(String name) {
        PartData rc = null;
        List<PartData> list = parameter.get(name);
        if (list != null && 0 < list.size()) {
            rc = list.get(0);
        }
        return rc;
    }

    public List<PartData> getAllParameter(String name) {
        return parameter.get(name);
    }

    public List<FileData> getFileUploads() {
        return fileuploads;
    }

    public List<Exception> getExceptionList() {
        return failedParts;
    }

    public void parseRequest() throws IllegalArgumentException, MessagingException, IOException {
        String baseCType = req.getHeader(CTYPE_HEADER);
        printDebug("******** Content-Type is: '" + baseCType + "'");
        ContentType cType = new ContentType(baseCType);
        String boundary = cType.getParameter(PARAM_BOUNDARY);
        printDebug("******** Boundary is: '" + boundary + "'");
        if (boundary == null || boundary.length() <= 0)
            return;

        ServletInputStream inS = req.getInputStream();

        MultipartStream ms = new MultipartStream(inS);
        if (0 < maxPartSize)
            ms.setMaxPartSize(maxPartSize);

        ms.setBoundary(boundary);
        ms.skipBoundary();
        boolean doLoop = true;
        RFC822Headers curHeaders = null;
        ContentType curCt = null;
        String curCtStr = null;
        String[] headers = null;
        do {
            printDebug("\n");
            curHeaders = new RFC822Headers(ms,req.getCharacterEncoding());

            headers = curHeaders.getHeader(CTYPE_HEADER);
            if (headers != null && 0 < headers.length) {
                curCtStr = headers[0];
                curCt = new ContentType(curCtStr);
            } else {
                curCt= new ContentType("text/plain; charset="+req.getCharacterEncoding());
            }

            boolean isfile = false;
            HeaderStruct struct = getHeaderStruct(curHeaders, CONTENT_DISP_HEADER);
            if (struct != null) {
                String filename = struct.getParam(FILENAME_PARAM);
                if (filename != null) {
                    isfile = true;
                    //System.out.println("File============"+filename);
                }
            }
            printDebug("*** IS FILE? " + isfile);
            

            if (curCt.match("text/*") && !isfile) {
                printDebug("parsing text field (" + curCt + ")");
                parseTextField(ms, curHeaders, curCt);
            } else if (curCt.match("multipart/mixed")) {
                printDebug("parsing multipart (" + curCt + ")");
                parseMultiFile(ms, curHeaders, curCt, cType);
            } else {
                printDebug("parsing file (" + curCt + ")");
                parseFile(ms, curHeaders, curCt, null);
            }

            if (!ms.isEndOfMultipart()) {
                printDebug("skipping boundary");
                ms.skipBoundary();
            } else {
                printDebug("end of multipart");
                doLoop = false;
            }
        } while (doLoop);
    }

    protected void parseTextField(MultipartStream ms, RFC822Headers headers, ContentType ct)
        throws MessagingException, IOException {
        String charEnc = ct.getParameter(CHARSET_PARAM);
        if (charEnc == null) {
            charEnc = DEFAULT_CHARSET;
        }
        printDebug("charEnc: " + charEnc);
        HeaderStruct cdStruct = getHeaderStruct(headers, CONTENT_DISP_HEADER);
        HeaderStruct cteStruct = getHeaderStruct(headers, CONTENT_TRANS_ENC_HEADER);
        String transEnc = cteStruct.getValue();
        if (transEnc == null) {
            transEnc = DEFAULT_TRANS_ENC;
        }
        printDebug("transEnc: " + transEnc);
        String fieldName = cdStruct.getParam(NAME_PARAM);
        printDebug("fieldname: " + fieldName);
        if (fieldName == null)
            return;

        InputStream dataIn = MimeUtility.decode(ms, transEnc);
        InputStreamReader inReader = new InputStreamReader(dataIn, charEnc);
        int tmpVal = -1;
        StringBuffer tmpBuf = new StringBuffer();
        PartToLongException ex = null;
        try {
            do {
                tmpVal = inReader.read();
                if (tmpVal != -1) {
                    tmpBuf.append((char) tmpVal);
                }
            } while (tmpVal != -1);
        } catch (PartToLongException e) {
            ex = e;
        }
        if (ex == null) {
            FieldData fPart = new FieldData();
            fPart.setFieldname(fieldName);
            fPart.setPrimaryType(ct.getPrimaryType());
            fPart.setSubType(ct.getSubType());
            fPart.setCharacterset(charEnc);
            fPart.setTransferEncoding(transEnc);
            fPart.setValue(tmpBuf.toString());

            addParameterPart(fPart);
        } else {
            ex.setFieldName(fieldName);
            failedParts.add(ex);
        }
    }

    protected void addParameterPart(PartData part) {
        try {
            String fieldName = part.getFieldname();
            ArrayList<PartData> params = (ArrayList<PartData>) parameter.get(fieldName);
            if (params == null) {
                params = new ArrayList<PartData>();
                parameter.put(fieldName, params);
            }
            params.add(part);
        } catch (NullPointerException e) {
            LOG.error(e,e);
        }
    }

    protected void parseMultiFile(MultipartStream ms, RFC822Headers headers, ContentType ct, ContentType parentCt)
        throws MessagingException, IOException {
        String localBoundary = ct.getParameter(PARAM_BOUNDARY);
        ms.setBoundary(localBoundary);
        ms.skipBoundary();
        RFC822Headers localHeaders = null;
        ContentType tmpCt = null;
        String[] tmpHeaders = null;
        HeaderStruct cdStruct = getHeaderStruct(headers, CONTENT_DISP_HEADER);
        String fieldName = cdStruct.getParam(NAME_PARAM);
        if (fieldName != null && fieldName.length() <= 0)
            fieldName = null;
        while (!ms.isEndOfMultipart()) {
            localHeaders = new RFC822Headers(ms,req.getCharacterEncoding());
            tmpHeaders = localHeaders.getHeader(CTYPE_HEADER);
            if (tmpHeaders != null && 0 < tmpHeaders.length) {
                tmpCt = new ContentType(tmpHeaders[0]);
                parseFile(ms, localHeaders, tmpCt, fieldName);
            }
            if (!ms.isEndOfMultipart())
                ms.skipBoundary();
        }
        ms.setBoundary(localBoundary);
    }

    protected void parseFile(MultipartStream ms, RFC822Headers headers, ContentType ct, String defFieldName)
        throws MessagingException, IOException {
        HeaderStruct cdStruct = getHeaderStruct(headers, CONTENT_DISP_HEADER);
        HeaderStruct cteStruct = getHeaderStruct(headers, CONTENT_TRANS_ENC_HEADER);
        String transEnc = cteStruct.getValue();
        if (transEnc == null) {
            transEnc = DEFAULT_TRANS_ENC;
        }
        String fieldName = null;
        if (defFieldName != null) {
            fieldName = defFieldName;
        }
        if (fieldName == null) {
            fieldName = cdStruct.getParam(NAME_PARAM);
        }
        if (fieldName == null)
            return;
        File localFile = null;
        String filename = cdStruct.getParam(FILENAME_PARAM);
        PartToLongException ex = null;
        if (filename != null && 0 < filename.length()) {
            localFile = getDestFile(dirFile, filename);
            InputStream dataIn = MimeUtility.decode(ms, transEnc);
            OutputStream dataOut = new BufferedOutputStream(new FileOutputStream(localFile));
            try {
                int tmpVal = -1;
                do {
                    tmpVal = dataIn.read();
                    if (tmpVal != -1) {
                        dataOut.write(tmpVal);
                    }
                } while (tmpVal != -1);
            } catch (PartToLongException e) {
                ex = e;
            } finally {
                dataOut.flush();
                dataOut.close();
            }
        }
        
        if(localFile!=null) {
            FileData fileP = new FileData();
            fileP.setPrimaryType(ct.getPrimaryType());
            fileP.setSubType(ct.getSubType());
            fileP.setFieldname(fieldName);
            fileP.setFilename(filename);
            fileP.setTransferEncoding(transEnc);
            fileP.setModificationDate(getDateFromParam(cdStruct.getParam(MODIFICATION_DATE_PARAM)));
            fileP.setReadDate(getDateFromParam(cdStruct.getParam(READ_DATE_PARAM)));
            if (ex == null) {
                fileP.setLocalFilename(localFile.getAbsolutePath());
                fileP.setSize(localFile.length());
                fileP.setLocalFile(localFile);
                fileuploads.add(fileP);
                addParameterPart(fileP);
            } else {
                //delete file resulting from part exceeding the size limit
                if(localFile!=null && localFile.exists()) localFile.delete();
                ex.setFieldName(fieldName);
                failedParts.add(ex);
                //mark as exceeded
                fileP.setExceedsSizeLimit(true);  
                fileP.setLocalFilename("");
                fileP.setSize(0);
                addParameterPart(fileP);
            }
        }

    }

    protected Date getDateFromParam(String inStr) {
        Date rc = null;
        try {
            if (inStr != null && 0 < inStr.length()) {
                MailDateFormat formatter = new MailDateFormat();
                rc = formatter.parse(inStr);
            }
        } catch (java.text.ParseException e) {
            rc = null;
        }
        return rc;
    }

    protected HeaderStruct getHeaderStruct(RFC822Headers headers, String name) {
        HeaderStruct rc = null;
        if (name != null) {
            rc = new HeaderStruct();
            rc.setName(name);
            try {
                String[] valArr = headers.getHeader(name);
                if (valArr != null && 0 < valArr.length) {
                    if (name.equals("Content-Disposition")) {
                        //System.out.println("val before:"+valArr[0]);
                        valArr[0] = removeIEDirtyPath(valArr[0]);
                        //System.out.println("val after :"+valArr[0]);
                    }
                    String value = MimeUtility.decodeText(valArr[0]);
                    int idx = value.indexOf(';');
                    if (0 <= idx) {
                        rc.setValue(value.substring(0, idx).trim());

                        rc.initParams(value.substring(idx));
                    } else {
                        rc.setValue(value.trim());
                    }
                }
            } catch (UnsupportedEncodingException e) {
                rc.resetParams();
            } catch (IndexOutOfBoundsException e) {
                rc.resetParams();
            }
        }
        return rc;
    }

    protected void printDebug(String msg) {
        try {
            if (LOG != null) {
                LOG.debug(msg);
            } else if (logBase != null) {
                logBase.log("[DEBUG] " + msg);
            }
        } catch (Exception e) {
        }
    }
    /**
     * Gets the maxPartSize.
     * @return Returns a long
     */
    public long getMaxPartSize() {
        return maxPartSize;
    }

    /**
     * Sets the maxPartSize.
     * @param maxPartSize The maxPartSize to set
     */
    public void setMaxPartSize(long maxPartSize) {
        this.maxPartSize = maxPartSize;
    }

    private String removeIEDirtyPath(String str) {
        Perl5Util perl = new Perl5Util();
        String ret = str;

        int fnindex = str.indexOf("filename");
        if (fnindex != -1) {
            int blindex = str.indexOf("\\"); 
            if(blindex > -1 && blindex > fnindex ) {
                perl.match("/filename=\"(.*[^\"])\"/", str);
                String fullpath = "";
                fullpath = perl.group(1);

                int index = fullpath.lastIndexOf("\\");
                if (index != -1) {
                    String file = "";
                    file = fullpath.substring(index + 1);
                    file = "\"" + file + "\"";
                    ret = perl.substitute("s/filename=\"(.*[^\"])\"/filename=" + file + "/", str);
                } 
            }
        }
        return ret;
    }

}
