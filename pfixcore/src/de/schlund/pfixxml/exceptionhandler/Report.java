/*
 * Created on 17.06.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.schlund.pfixxml.exceptionhandler;

/**
 * Class representing a report of collected throwables.
 *<br/>
 *@author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class Report {
    
    /**The containing message */
    private String message;
    /**Number of collected exceptions */
    private int count;
    
    Report(String message, int count) {
        if(message == null)
            throw new IllegalArgumentException("A NP as message is not allowed here.");
            
        if(count == 0)
            throw new IllegalArgumentException("0 as count is not sensible here.");
            
        this.message = message;
        this.count = count;
    }
    
    

    /**
     * Retrieve number of collected throwables in the report.
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * Retrieve the textual message in the report.
     * @return
     */
    public String getMessage() {
        return message;
    }

}
