package de.schlund.pfixcore.editor.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.apache.oro.text.perl.Perl5Util;
import org.w3c.dom.Element;


import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.testenv.RecordManager;
import de.schlund.pfixxml.testenv.RecordManagerFactory;
import de.schlund.pfixxml.testenv.TestClient;
import de.schlund.pfixxml.testenv.TestClientException;
import de.schlund.pfixxml.testenv.TestcasePlaybackResult;
import de.schlund.pfixxml.testenv.TestcaseStepResult;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CRTestcaseImpl implements CRTestcase {

    private ArrayList selectedTestcases = null;
    private String availableTestcasesDirectory = null;
    private String availableTestcases = null;
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
            Element e = resdoc.addTextChild(ele, "testcase", cases[i]);
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
                String name = (String) iter.next();
                Element e = resdoc.addTextChild(ele2, "testcase", name);
                e.setAttribute("tmp_directory", getTemporaryDirectoryForTestcase(name));
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
                ArrayList steps = playresult.getStepResults();
                for(int j=0; j<steps.size(); j++) {
                    String str = ((TestcaseStepResult)steps.get(j)).getDiffString();
                    Perl5Util perl = new Perl5Util();
                    ArrayList lines = new ArrayList();
                    perl.split(lines, "/\n/", str);
                    Element elem3 = resdoc.createNode("step");
                    elem3.setAttribute("id", ""+j);
                    elem3.setAttribute("statuscode", ""+((TestcaseStepResult)steps.get(j)).getStatusCode());
                    for(int k=0; k<lines.size(); k++) {
                        //skip emtpy lines
                        if(((String) lines.get(k)).equals("")) continue;
                        Element e3 = resdoc.addTextChild(ele3, "line", (String) lines.get(k));
                        e3.setAttribute("id", ""+k);
                        elem3.appendChild(e3);
                    }
                    el3.appendChild(elem3);
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
    public void setSelectedTestcases(String[] cases) {
        selectedTestcases = new ArrayList(cases.length);
        for(int i=0; i<cases.length; i++) {
            selectedTestcases.add(cases[i]);
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

    public void setAvailableTestcasesDirectory(String dir) {
        availableTestcasesDirectory = dir;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#executeTest()
     */
    public HashMap executeTest() throws Exception {
        hasStartedTestcases = true;
        testOutput = new HashMap();
        for(int i=0; i<selectedTestcases.size(); i++) {
            TestClient tc = new TestClient();
            try {
                String tcase = (String) selectedTestcases.get(i);
                String dir = getAvailableTestcaseDir() + "/" + tcase;
                tc.setOptions(dir, 
                    getTemporaryDirectoryForTestcase(tcase),
                    getAvailableTestcaseDir() + "/" + tcase);
                TestcasePlaybackResult result = tc.makeTest(); 
                testOutput.put(tcase, result);
            } catch(TestClientException e) {
                CAT.error("TestClientException: "+e.getMessage()+" "+e.getExceptionCause().getMessage());
                throw e;
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
        availableTestcasesDirectory = null;
        availableTestcases = null;
        testOutput = null;
        hasStartedTestcases = false;
        isTestExecuted = false;
    }

    private String getAvailableTestcaseDir() throws Exception {
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(cRM);
        EditorProduct product = esess.getProduct();
        if(product == null) {
            return null;
        }
        String depend = product.getDepend();
        RecordManager recman = RecordManagerFactory.getInstance().createRecordManager(depend);
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
            for(int j=0; j<files.length; j++) {
                boolean ok = files[j].delete();
                if(!ok) {
                    CAT.error("Unable to remove file: "+files[j]);
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
