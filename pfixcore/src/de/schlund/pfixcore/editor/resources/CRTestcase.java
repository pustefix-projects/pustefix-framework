package de.schlund.pfixcore.editor.resources;

import java.util.ArrayList;
import java.util.HashMap;

import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.testenv.TestClientException;


/**
 * ContextResource responsible managing all testcases for a specific product.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public interface CRTestcase extends ContextResource {
    /**
     * Set the selected testcases.
     * @param cases the names of the selected testcases
     */
    public void setSelectedTestcases(String[] cases);
    
    /**
     * Retrieve all selected testcases.
     * @return all selected testcases
     */
    public ArrayList getSelectedTestcases();
    
    /**
     * Determine if user has selected testcases. 
     * @return true if user has selected one or more testcases
     */
    public boolean hasSelectedTestcases();
    
    /**
     * Determine if user has selected testcases for execution.
     * @return true if user has marked the selected testcase to be play-backed.
     */
    public boolean hasStartedTestcases();
    
    /**
     * Determine if a testcase has been executed and a result exists.
     * @return true if result exists.
     */
    public boolean isTestExecuted();
    
    /**
     * Retrieve the base directory for recorded files in the current product. 
     * @return the name of the directory
     * @throws Exception on all errors
     */
    public String getAvailableTestcasesDirectoryForProduct() throws Exception;
    
    /**
     * Retrieve all testcases found in the base directory in the current product.
     * @return an array containing the names of the testcases
     * @throws Exception on all errors
     */
    public String[] getAvailableTestcases() throws Exception;
    
    /**
     * Retrieve the directory where temporary data is written.
     * @return the name of the temporary directory
     */
    public String getTemporaryDirectoryForTestcase(String casename);
    
    /**
     * Execute all selected testcases. The testcase will be play-backed and its
     * result will be returned.
     * @return a hash with the name of the testcase as key and a {@link TestcasePlaybackResult}
     * as value.
     * @throws Exception on all errors
     */
    public HashMap executeTest() throws TestClientException;
    
    /**
     * @see public HashMap executeTest() throws Exception;
     * Retrieve the result of the previously executed testcases.
     * @return the testcases.
     */
    public HashMap getTestResult();
    
    /**
     * Reset the context resource. Remove all state sensitiv information.
     */
    public void doReset();
    
    /**
     * Remove  testcases including their temporary files from the filesystem.
     * @param the names of the testcases to be deleted
     */
    public void removeTestcase(String[] names) throws Exception;
}
