package de.schlund.util.statuscodes;

/**
 * StatusCode - a class that encapsulates all information necessary for status or error codes
 * StatusCode instances where produced by a instance of the StatusCodeFactory which guaranties that
 * the StatusCode is unique
 * 
 * @author Wolfram M�ller
 *
 */

public class StatusCode {
    /**
     * the Domain of the StatusCode without trailing "."
     */
    private String scdomain = null;

    /**
    * the StatusCodeIdentifier without any domain
    */
    private String scid = null;

    /**
     * the default StatusMessage itself
     */
    private String scmsg = null;
   
    /**
     * gets the StatusCode without domain, do no comparisons with it - just for output
     * for comparisons use the equals method
     */
    public String getStatusCode() {
        return scid;
    }

    /**
     * gets the domain of the StatusCode
     */
    public String getStatusCodeDomain() {
        return scdomain;
    }

    /**
     * gets the default Message 
     */
    public String getDefaultMessage() {
        return scmsg;
    }

    /**
     * gets the StatusCode with domain
     */
    public String getStatusCodeWithDomain() { 
        if (scdomain.equals("")) {
            return scid;
        } else {
            return scdomain + "." + scid; 
        }
    }

    public String toString() {
        return getStatusCodeWithDomain() + "=" + getDefaultMessage();
    }
    /**
     * constructor - initialise ErrorCode object with 
     * @param scdomain - the domain of the StatusCode without endig "."
     * @param scid - the StatusCode string
     * @param scmsg - the default ErrorMessage
     */
    protected StatusCode (String scdomain, String scid, String scmsg) { 
        this.scdomain = scdomain;
        this.scid     = scid;
        this.scmsg    = scmsg;
    }

}
