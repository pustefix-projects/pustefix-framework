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

package de.schlund.pfixxml.testenv;




import de.schlund.pfixxml.util.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.apache.log4j.Category;
import org.apache.oro.text.regex.*;
import org.w3c.dom.*;

/**
 * @author Joerg Haecker <haecker@schlund.de>
 *
 */
public class TestcaseStepResult {

    private static String GNU_DIFF = "diff -u";
    private static Category CAT = Category.getInstance(TestcaseStepResult.class.getName());
    private int statusCode;
    private String diffString;
    private Document serverResponse;
    private Document recordedReferenceDoc;
    private boolean serverError = false;
    private long stepDuration = 0;
    private long preProc = 0;
    private long getDom = 0;
    private long hdlDoc = 0;

    public void setRecordedReferenceDoc(Document reference_data) {
        if (reference_data == null) {
            throw new IllegalArgumentException("A NP as referencedata is NOT allowed here!");
        }
        recordedReferenceDoc = reference_data;
    }

    public Document getServerResponse() {
        return serverResponse;
    }

    public int getStatuscode() {
        return statusCode;
    }

    public void setStatuscode(int code) {
        statusCode = code;
    }

    public void setServerResponse(Document response) {
        if (response == null) {
            throw new IllegalArgumentException("A NP as response is not allowed here!");
        }
        serverResponse = response;
        extractAdditionTimingInfo();
    }

    public void setDuration(long duration) {
        stepDuration = duration;
    }
    
    public long getDuration() {
        return stepDuration;
    }
    
    public long getPreProcessingDuration() {
        return preProc;
    }

    public long getGetDocumentDuration() {
        return getDom;    
    }

    public long getHandleDocumentDuration() {
        return hdlDoc;
    }


    public void createDiff(String tmpdir, String style_dir, String stylesheet) throws Exception {
        if (serverError) {
            diffString = ":-(";
            return;
        }

        doTransform(style_dir, stylesheet);

        String ref_path = tmpdir + "/_recorded" + this.hashCode();
        String srv_path = tmpdir + "/_current" + this.hashCode();
        Xml.serialize(serverResponse, srv_path, true, true);
        Xml.serialize(recordedReferenceDoc, ref_path, true, true);
        doDiff(srv_path, ref_path);
    }

    public String getDiffString() {
        return diffString;
    }

    public void extractAdditionTimingInfo()  {
        String core_timing_info = serverResponse.getFirstChild().getNextSibling().getNodeValue();
        Perl5Matcher perl = new Perl5Matcher();
        Perl5Compiler perlcomp = new Perl5Compiler();
        try {
            perl.contains(core_timing_info, perlcomp.compile("PRE_PROC:[ *](\\d*)[ *]GET_DOM:[ *](\\d*)[ *]HDL_DOC:[ *](\\d*)"));
        } catch (MalformedPatternException e) {
            e.printStackTrace();
        }
        String pre_proc = perl.getMatch().group(1);
        String get_dom = perl.getMatch().group(2);
        String hdl_doc = perl.getMatch().group(3);

       // System.out.println(pre_proc+"|"+get_dom+"|"+hdl_doc);

        preProc = Integer.parseInt(pre_proc);
        getDom = Integer.parseInt(get_dom);
        hdlDoc = Integer.parseInt(hdl_doc);
    }
    
    
    /** remove the serial number from the result document */
    private void removeSerialNumbers() {
        Node node1 = recordedReferenceDoc.getFirstChild();
        ((Element) node1).setAttribute("serial", "0");

        Node node2 = serverResponse.getFirstChild();
        ((Element) node2).setAttribute("serial", "0");
    }
    
  

    /** start GNU diff process */
    private void doDiff(String path1, String path2) throws Exception {
        String diff = GNU_DIFF + " " + path2 + " " + path1;

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(diff);
        } catch (IOException e) {
            throw new TestClientException("IOException occured!", e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String s;
        StringBuffer buf = new StringBuffer();

        while ((s = reader.readLine()) != null) {
            buf.append(s).append("\n");
        }

        reader.close();
        process.waitFor();
        diffString = buf.toString();
    }

    public boolean isServerError() {
        return serverError;
    }

    public void setServerError(boolean error) {
        serverError = error;
    }

    private void doTransform(String style_dir, String stylesheet) throws TestClientException {

        removeSerialNumbers();

        String path = style_dir + "/" + stylesheet;
        
       // System.out.println("Stylesheet--->"+path);
        
        File stylesheetFile = new File(path);
        if (stylesheetFile.exists()) {
            if (CAT.isInfoEnabled()) {
                CAT.info("  Transforming using stylesheet :"+path);
            }
            Templates trafo;
            try {
                trafo = Xslt.loadTemplates(Path.create(stylesheetFile));
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
                throw new TestClientException("TransformerConfigurationException occured!", e);
            }
            try {
                DOMResult result = new DOMResult();
                Xslt.transform(serverResponse, trafo, null, result);
                serverResponse = (Document) result.getNode();
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }

            try {
                DOMResult result = new DOMResult();
                Xslt.transform(recordedReferenceDoc, trafo, null, result);
                recordedReferenceDoc = (Document) result.getNode();
            } catch (TransformerException e) {
                throw new TestClientException("TransformerException occured!", e);
            }
        } else {
            CAT.info("Stylesheet "+path+" not found.");
        }
    }
}
