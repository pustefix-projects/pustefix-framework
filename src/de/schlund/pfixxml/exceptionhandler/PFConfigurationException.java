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
class PFConfigurationException extends Exception {

    //~ Instance/static variables ..............................................

    private Exception cause_=null;
    private String msg_=null;
    //~ Constructors ...........................................................

    PFConfigurationException(String error, Exception cause) {
        super(error);
        this.msg_  =error;
        this.cause_=cause;
    }

    //~ Methods ................................................................

    Exception getCause() {
        if(cause_==null)
            return new Exception("unkown reason");
        return cause_;
    }
    
    public String getMessage() {
        if(msg_==null)
            return "";
        return msg_;
    }
}