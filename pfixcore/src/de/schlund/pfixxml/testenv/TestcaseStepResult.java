package de.schlund.pfixxml.testenv;

/**
 * Class encapsulating the result of a single step in a testcase after playback.
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TestcaseStepResult {
    private String diffString;
    private int statusCode;

    /**
     * Returns the diff for this step.
     * @return String
     */
    public String getDiffString() {
        return diffString;
    }

    /**
     * Returns the status code for this step.
     * @return int
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the diff for this step.
     * @param diffString the diff 
     */
    public void setDiffString(String diffString) {
        this.diffString = diffString;
    }

    /**
     * Sets the status for this step.
     * @param statusCode the statusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
