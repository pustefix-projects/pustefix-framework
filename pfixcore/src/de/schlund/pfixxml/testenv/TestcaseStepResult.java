package de.schlund.pfixxml.testenv;

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestcaseStepResult {
    private String diffString;
    private int statusCode;

    /**
     * Returns the diffString.
     * @return String
     */
    public String getDiffString() {
        return diffString;
    }

    /**
     * Returns the statusCode.
     * @return int
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the diffString.
     * @param diffString The diffString to set
     */
    public void setDiffString(String diffString) {
        this.diffString = diffString;
    }

    /**
     * Sets the statusCode.
     * @param statusCode The statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
