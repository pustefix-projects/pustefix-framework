package de.schlund.pfixcore.editor.resources;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.editor.EditorProduct;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResourceManager;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.testenv.RecordManager;
import de.schlund.pfixxml.testenv.RecordManagerFactory;
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

    private String [] selectedTestcases = null;
    private String availableTestcasesDirectory = null;
    private String availableTestcases = null;
    private ArrayList testOutput = null;
    private ContextResourceManager cRM = null;
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
        selectedTestcases = new String[cases.length];
        for(int i=0; i<cases.length; i++) {
            selectedTestcases[i] =  cases[i];
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTestcasesForProcessing()
     */
    public String[] getSelectedTestcases() {
        return selectedTestcases;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#hasTestcasesForProcessing()
     */
    public boolean hasSelectedTestcases() {
        return ((selectedTestcases == null) || 
                (selectedTestcases.length < 1)) ? false : true;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getAvailableTestcases()
     */
    public String[] getAvailableTestcases() throws Exception {
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
        if(availableTestcasesDirectory == null) {
            availableTestcases = getAvailableTestcaseDir();
        }
        return availableTestcases;
    }

    public void setAvailableTestcasesDirectory(String dir) {
        availableTestcasesDirectory = dir;
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#executeTest()
     */
    public void executeTest() throws Exception {
        testOutput = new ArrayList();
        for(int i=0; i<selectedTestcases.length; i++) {
            TestClient tc = new TestClient();
            try {
                String dir = getAvailableTestcaseDir() + "/" + selectedTestcases[i];
                String[] result = tc.makeTest(dir, 
                                getAvailableTestcaseDir() + "/" + selectedTestcases[i],
                                getTemporaryDirectoryForTestcase(selectedTestcases[i]));
                testOutput.add(i, result);
            } catch(TestClientException e) {
                CAT.error("TestClientException: "+e.getMessage()+" "+e.getExceptionCause().getMessage());
                throw e;
            }
        }
    }

    /**
     * @see de.schlund.pfixcore.editor.resources.CRTestcase#getTestResult()
     */
    public ArrayList getTestResult() {
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
    }

    private String getAvailableTestcaseDir() throws Exception {
        EditorSessionStatus esess = EditorRes.getEditorSessionStatus(cRM);
        EditorProduct product = esess.getProduct();
        String depend = product.getDepend();
        RecordManager recman = RecordManagerFactory.getInstance().createRecordManager(depend);
        return recman.getRecordmodeBaseDir();
    }
}
