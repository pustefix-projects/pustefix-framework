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

package de.schlund.pfixxml.exceptionhandler;

/**
 * Class holding mail configuration for exception handler.<br/>
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class MailConfig {

    //~ Instance/static variables ..............................................

    private String from_ =null;
    private String host_ =null;
    private boolean send_=false;
    private String[] to_ =null;
    private static MailConfig theInstance = new MailConfig();
    //~ Constructors ...........................................................

    /*MailConfig(String[] to, String from, String host, boolean send) {
        this.to_  =to;
        this.from_=from;
        this.host_=host;
        this.send_=send;
    }*/
    private MailConfig() {
    }

    //~ Methods ................................................................

    static MailConfig getInstance() {
        return theInstance;
    }

    void configure(String[] to, String from, String host, boolean send) {
        this.to_ = to;
        this.from_ = from;
        this.host_ = host;
        this.send_ = send;
    }

    /**
     * Returns the from.
     * @return String
     */
    String getFrom() {
        return from_;
    }

    /**
     * Returns the host.
     * @return String
     */
    String getHost() {
        return host_;
    }

    /**
     * Returns the send.
     * @return boolean
     */
    boolean isSend() {
        return send_;
    }

    /**
     * Returns the to.
     * @return String[]
     */
    String[] getTo() {
        return to_;
    }
}