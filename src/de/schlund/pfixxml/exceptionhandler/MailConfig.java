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

/*
 *
 */

/**
 * @author jh
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
class MailConfig {

    //~ Instance/static variables ..............................................

    private String from_ =null;
    private String host_ =null;
    private boolean send_=false;
    private String[] to_ =null;

    //~ Constructors ...........................................................

    MailConfig(String[] to, String from, String host, boolean send) {
        this.to_  =to;
        this.from_=from;
        this.host_=host;
        this.send_=send;
    }

    //~ Methods ................................................................

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