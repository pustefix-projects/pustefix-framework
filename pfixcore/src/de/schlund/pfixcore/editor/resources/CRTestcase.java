package de.schlund.pfixcore.editor.resources;

import java.util.ArrayList;

import de.schlund.pfixcore.workflow.ContextResource;


/**
 * ContextResource responsible managing all testcases for a specific product.
 */
public interface CRTestcase extends ContextResource {
    public void setSelectedTestcases(String[] cases);
    public String[] getSelectedTestcases();
    public boolean hasSelectedTestcases();
    
    public String getAvailableTestcasesDirectoryForProduct() throws Exception;
    public String[] getAvailableTestcases() throws Exception;
    
    public String getTemporaryDirectoryForTestcase(String casename);
    
    public void executeTest() throws Exception;
    public ArrayList getTestResult();
    
    public void doReset();
}
