package de.schlund.pfixcore.editor.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.apache.oro.text.perl.Perl5Util;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.testenv.RecordManager;
import de.schlund.pfixxml.testenv.RecordManagerFactory;
import de.schlund.pfixxml.testenv.Testcase;
import de.schlund.pfixxml.testenv.TestClientException;
import de.schlund.pfixxml.testenv.TestcasePlaybackResult;
import de.schlund.pfixxml.testenv.TestcaseStepResult;


/**
 * Implementation of the <code>CRTestcase</code> interface.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class CRTestcaseImpl implements CRTestcase {

    /** list containing Objects of type Testcase */
    private ArrayList selectedTestcases = null;
    private boolean hasStartedTestcases = false;
    private HashMap testOutput = null;
    private ContextResourceManager cRM = null;
    private boolean isTestExecuted = false;
    private static Category CAT = Category.getInstance(CRTestcase.class.getName());
    private static String TEMP_DIR_DEFAULT = "/tmp";
    /**
     * @see de.schlund.pfixcore.workflow.ContextResource#init(Context)
     */
    public void init(Context context) throws Exception {
        cRM = context.getContextResourceManager();
    }

    /**
     * @see de.schlund.pfixcore.workflow.ContextResource#insertStatus(ResultDocument, Element)
     */
    public void insertStatus(ResultDocument resdoc, Element elem)
        throws Exception {
        
        String[] cases = getAvailableTestcases();
        Element ele = resdoc.createNode("available");
        for(int i=0; i<cases.length; i++) {
            Element e = ResultDocument.addTextChild(ele, "testcase", cases[i]);
            ele.appendChild(e);
        }
        elem.appendChild(ele);
        CRTestcase crtc = EditorRes.getCRTestcase(cRM);
        String dir = crtc.getAvailableTestcasesDirectoryForProduct();
        elem.setAttribute("directory", dir);
    
        ArrayList scases = getSelectedTestcases();
        if(scases != null) {
            Element ele2 = resdoc.createNode("selected");
            Iterator iter = scases.iterator();
            while(iter.hasNext()) {
                Testcase testcase = (Testcase) iter.next();
                Element e = ResultDocument.addTextChild(ele2, "testcase", testcase.getName());
                e.setAttribute("tmp_directory", getTemporaryDirectoryForTestcase(testcase.getName()));
                e.setAttribute("number_of_steps", ""+testcase.getNumberOfStepsForTestcase());
                ele2.appendChild(e);
            }
            elem.appendChild(ele2);
        }
        
        HashMap result = getTestResult();
        if(result != null) {
            Iterator keys = result.keySet().iterator();
            Element ele3 = resdoc.createNode("test_results");
            while(keys.hasNext()) {
                String key = (String) keys.next();
                Element el3 = resdoc.createNode("test");
                el3.setAttribute("id", key);
                TestcasePlaybackResult playresult = (TestcasePlaybackResult) result.get(key);
                if(playresult.hasException()) {
                    TestClientException ex = playresult.getException();
                    el3.appendChild(el3.getOwnerDocument().importNode(ex.toXMLRepresentation().getFirstChild(), true));
                } else {   
                      Element timing_ele = resdoc.createSubNode(el3, "timing");
                      timing_ele.setAttribute("total", ""+playresult.getTotalDuration());
                      timing_ele.setAttribute("getdom", ""+playresult.getTotalGetDomDuration());
                      timing_ele.setAttribute("hdldoc", ""+playresult.getTotalHandleDocumentDuartion());
                      timing_ele.setAttribute("prepro", ""+playresult.getTotalPreProcessingDuration());
                    for(int j=0; j<playresult.getNumStepResult(); j++) {
                        TestcaseStepResult stepres = playresult.getStepResult(j);
                        String str = stepres.getDiffString();
                        Perl5Util perl = new Perl5Util();
                        ArrayList lines = new ArrayList();
                        if(str != null)
                            perl.split(lines, "/\n/", str);
                        Element elem3 = resdoc.createNode("step");
                        elem3.setAttribute("id", ""+j);
                        elem3.setAttribute("statuscode", ""+stepres.getStatuscode());
                        for(int k=0; k<lines.size(); k++) {
                            //skip emtpy lines
                            if(((String) lines.get(k)).equals("")) continue;
                            Element e3 = ResultDocument.addTextChild(ele3, "line", (String) lines.get(k));
                            e3.setAttribute("id", ""+k);
                            elem3.appendChild(e3);
                        }
                        el3.appendChild(elem3);
                    }
                }
                ele3.appendChild(el3);
            }
            elem.appendChild(ele3);
        }
    }

    /**
     * @see de.schlund.pfixcore.workflow.ContextResource#reset()
     */
    public void reset() throws Exception {
    }

    /**
     * @see de.schlund.pfixcore.workflow.ContextResource#needsData()
     */
    public boolean needsData() throws Exception {
        return false;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#setTestcasesForProcessing(String[])
     */
    public void setSelectedTestcases(String[] cases) throws TestClientException {
        selectedTestcases = new ArrayList(cases.length);
        for(int i=0; i < cases.length; i++) {
            String tcasename = cases[i];
            String dir = getAvailableTestcaseDir() + "/" + tcasename;
            Testcase testcase  = new Testcase();
            testcase.setOptions(dir, 
                    getTemporaryDirectoryForTestcase(tcasename),
                    getAvailableTestcaseDir() + "/" + tcasename,
                    tcasename);
            selectedTestcases.add(i, testcase);
            
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTestcasesForProcessing()
     */
    public ArrayList getSelectedTestcases() {
        return selectedTestcases;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#hasTestcasesForProcessing()
     */
    public boolean hasSelectedTestcases() {
        return ((selectedTestcases == null) || 
                (selectedTestcases.size() < 1)) ? false : true;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getAvailableTestcases()
     */
    public String[] getAvailableTestcases() throws Exception {
        String d = getAvailableTestcaseDir();
        if(d == null) {
            return new String[0];
        }
        File dir = new File(getAvailableTestcaseDir());
        String [] files = dir.list();
        if(files == null) {
            files = new String[0];
        }
        return files;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getAvailableTestcasesDirectory()
     */
    public String getAvailableTestcasesDirectoryForProduct() throws Exception {
        return getAvailableTestcaseDir();
    }

    

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#executeTest()
     */
    public HashMap executeTest() throws Exception  {
        hasStartedTestcases = true;
        testOutput = new HashMap();
        for(int i=0; i<selectedTestcases.size(); i++) {
            Testcase testcase = (Testcase) selectedTestcases.get(i);
            
            try {
                TestcasePlaybackResult result = testcase.execute(); 
                testOutput.put(testcase.getName(), result);
            } catch(TestClientException e) {
                CAT.error("TestClientException: "+e.getMessage()+" -> "+e.getExceptionCause().getMessage());
                TestcasePlaybackResult res = new TestcasePlaybackResult();
                res.setException(e);
                testOutput.put(testcase.getName(), res);
            }
        }   
        isTestExecuted = true;
        return testOutput;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTestResult()
     */
    public HashMap getTestResult() {
        return testOutput;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTemporaryDirectory()
     */
    public String getTemporaryDirectoryForTestcase(String casename) {
        String tmp_dir = System.getProperties().getProperty("java.io.tmpdir");
        if(tmp_dir == null || tmp_dir.equals("")) {
            tmp_dir = TEMP_DIR_DEFAULT;
        }
        return tmp_dir + "/" + casename;
    }
    
  

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#doReset()
     */
    public void doReset() {
        if(CAT.isDebugEnabled()) {
            CAT.debug("Resetting CRTestcase!");
        }
        selectedTestcases = null;
        testOutput = null;
        hasStartedTestcases = false;
        isTestExecuted = false;
    }

    private String getAvailableTestcaseDir() throws TestClientException  {
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(cRM);
        EditorProduct product = esess.getProduct();
        if(product == null) {
            return null;
        }
        String depend = product.getDepend();
        RecordManager recman = null;
        try {
            recman = RecordManagerFactory.getInstance().createRecordManager(depend);
        } catch (SAXException e) {
            throw new TestClientException("SAXException!", e);
        } catch (IOException e) {
            throw new TestClientException("IOException!", e);
        }
        return recman.getRecordmodeBaseDir();
    }
    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#removeTestcase(String)
     */
    public void removeTestcase(String[] names) throws Exception {
        for(int i=0; i<names.length; i++) {
            String abs_path = getAvailableTestcaseDir() + "/" + names[i];
            if(CAT.isDebugEnabled()) {
                CAT.debug("removing "+abs_path);
            }
            File file = new File(abs_path);
            File[] files = file.listFiles();
            if (files != null) {
                for(int j=0; j<files.length; j++) {
                    boolean ok = files[j].delete();
                    if(!ok) {
                        CAT.error("Unable to remove file: "+files[j]);
                    }
                }
            }
            boolean ok = file.delete();
            if(!ok) {
                CAT.error("Unable to remove file: "+abs_path);
            }
        }
        // remove deleted testcases from the selected list
        if(getSelectedTestcases()!= null) {
            Iterator iter = getSelectedTestcases().iterator();
            for(int i=0; i<names.length; i++) {
                while(iter.hasNext()) {
                    String str = (String) iter.next();
                    if(names[i].equals(str)) {
                        iter.remove();
                    }
                }
            }
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#hasStartedTestcases()
     */
    public boolean hasStartedTestcases() {
        return hasStartedTestcases;
    }

   
    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#isTestExecuted()
     */
    public boolean isTestExecuted() {
        return isTestExecuted;
    }
}
