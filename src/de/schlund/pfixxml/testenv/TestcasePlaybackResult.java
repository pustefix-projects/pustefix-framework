package de.schlund.pfixxml.testenv;

import java.util.ArrayList;

/**
 * Class encapsulating the result of a whole testcase after playback.
 * Aggregates serveral {@link TestcaseStepResult} objects.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
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
     * Returns the results for all steps in the testcase.
     * @return ArrayList containg TestcaseStepResult objects.
     */
    public ArrayList getStepResults() {
        return stepResults;
    }

    /**
     * Add a TestcaseStepResult object to the Testcase result.
     * @param the TestcaseStepResult to be added
     */
    public void addTestcaseStepResult(TestcaseStepResult step) {
        stepResults.add(step);
    }

}
