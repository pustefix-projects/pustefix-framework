package de.schlund.pfixcore.editor.resources;

import java.util.ArrayList;

import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.testenv.TestClientException;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface CRTestcase extends ContextResource {
    public void setTestcasesForProcessing(String[] cases);
    public String[] getTestcasesForProcessing();
    public boolean hasTestcasesForProcessing();
    public String getAvailableTestcasesDirectory();
    public String[] getAvailableTestcases();
    public void setAvailableTestcasesDirectory(String dir);
    public void executeTest() throws TestClientException;
    public ArrayList getTestResult();
}
