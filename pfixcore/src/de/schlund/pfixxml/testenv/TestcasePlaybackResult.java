package de.schlund.pfixxml.testenv;

import java.util.ArrayList;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestcasePlaybackResult {
    private ArrayList stepResults;

    /**
     * Constructor for TestcasePlaybackResult.
     */
    public TestcasePlaybackResult() {
        stepResults = new ArrayList();
    }

    /**
     * Returns the stepResults.
     * @return ArrayList
     */
    public ArrayList getStepResults() {
        return stepResults;
    }

    public void addTestcaseStepResult(TestcaseStepResult step) {
        stepResults.add(step);
    }

}
