package de.schlund.pfixcore.editor.resources;

import java.util.ArrayList;
import java.util.HashMap;

import de.schlund.pfixcore.workflow.ContextResource;


/**
 * ContextResource responsible managing all testcases for a specific product.
 */
public interface CRTestcase extends ContextResource {
    public void setSelectedTestcases(String[] cases);
    public ArrayList getSelectedTestcases();
    public boolean hasSelectedTestcases();
    public boolean hasStartedTestcases();
    
    public String getAvailableTestcasesDirectoryForProduct() throws Exception;
    public String[] getAvailableTestcases() throws Exception;
    
    public String getTemporaryDirectoryForTestcase(String casename);
    
    public void executeTest() throws Exception;
    public HashMap getTestResult();
    
    public void doReset();
    
    public void removeTestcase(String[] names) throws Exception;
}
