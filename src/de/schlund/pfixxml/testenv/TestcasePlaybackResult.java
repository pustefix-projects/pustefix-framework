package de.schlund.pfixxml.testenv;

import java.util.ArrayList;

/**
 * Class encapsulating the result of a whole testcase after playback.
 * Aggregates serveral {@link TestcaseStepResult} objects.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcasePlaybackResult {
    private ArrayList stepResults = null;
    private TestClientException ex = null;
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
    public TestcaseStepResult getStepResult(int index) {
        return (TestcaseStepResult) stepResults.get(index);
    }
    
    public int getNumStepResult() {
        return stepResults.size();
    }
   

    /**
     * Add a TestcaseStepResult object to the Testcase result.
     * @param the TestcaseStepResult to be added
     */
    public void addTestcaseStepResult(TestcaseStepResult step) {
        if(step == null) {
            throw new IllegalArgumentException("A NP as step is not allowed here!");
        }
        stepResults.add(step);
    }
    
    public void setException(TestClientException e) {
        this.ex = e;
    }
    
    public TestClientException getException() {
        return ex;
    }

    public boolean hasException() {
        return ex == null ? false : true;
    }

}
