package de.schlund.pfixcore.editor.resources;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.testenv.TestClient;
import de.schlund.pfixxml.testenv.TestClientException;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class CRTestcaseImpl implements CRTestcase {

    private String [] testCasesForPrcessing = null;
    private String availableTestcasesDirectory = null;
    private String availableTestcases = null;
    private ArrayList testOutput = null;
    private static Category CAT = Category.getInstance(CRTestcase.class.getName());
    /**
     * @see de.schlund.pfixcore.workflow.ContextResource#init(Context)
     */
    public void init(Context context) throws Exception {
    }

    /**
     * @see de.schlund.pfixcore.workflow.ContextResource#insertStatus(ResultDocument, Element)
     */
    public void insertStatus(ResultDocument resdoc, Element elem)
        throws Exception {
        //System.out.println(this.getClass().getName()+" : insertStatus");
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
    public void setTestcasesForProcessing(String[] cases) {
        testCasesForPrcessing = new String[cases.length];
        for(int i=0; i<cases.length; i++) {
            testCasesForPrcessing[i] =  cases[i];
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTestcasesForProcessing()
     */
    public String[] getTestcasesForProcessing() {
        return testCasesForPrcessing;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#hasTestcasesForProcessing()
     */
    public boolean hasTestcasesForProcessing() {
        return ((testCasesForPrcessing == null) || 
                (testCasesForPrcessing.length < 1)) ? false : true;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getAvailableTestcases()
     */
    public String[] getAvailableTestcases() {
        File dir = new File(availableTestcasesDirectory);
        String [] files = dir.list();
        if(files == null) {
            files = new String[0];
        }
        return files;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getAvailableTestcasesDirectory()
     */
    public String getAvailableTestcasesDirectory() {
        return availableTestcasesDirectory;
    }

    public void setAvailableTestcasesDirectory(String dir) {
        availableTestcasesDirectory = dir;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#executeTest()
     */
    public void executeTest() throws TestClientException {
        //System.out.println("Execute test");
        testOutput = new ArrayList();
        for(int i=0; i<testCasesForPrcessing.length; i++) {
            TestClient tc = new TestClient();
            try {
                String dir = availableTestcasesDirectory + "/" + testCasesForPrcessing[i];
                System.out.println("Passing "+dir+" to testclient");
                String[] result = tc.makeTest(dir, 
                                "/home/jh/wuergspace/workspace/pfixcore/example/testenv",
                                "/tmp");
                testOutput.add(i, result);
            } catch(TestClientException e) {
                CAT.error("TestClientException: "+e.getMessage()+" "+e.getCause().getMessage());
                throw e;
            }
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTestResult()
     */
    public ArrayList getTestResult() {
       // System.out.println("getTestResult");
        return testOutput;
    }

}
