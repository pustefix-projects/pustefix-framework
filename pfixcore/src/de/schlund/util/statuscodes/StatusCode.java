/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

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
    private final String scdomain;

    /**
    * the StatusCodeIdentifier without any domain
    */
    private final String scid;

    /**
     * the default StatusMessage itself
     */
    private final String scmsg;
   
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
