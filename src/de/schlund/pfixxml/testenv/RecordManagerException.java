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

package de.schlund.pfixxml.testenv;


/**
 * Exception class for handling errors for {@link RecordManager}
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class RecordManagerException extends Exception {

    //~ Instance/static variables ..................................................................

    private String    errorMessage = null;
    private Exception theCause = null;

    //~ Constructors ...............................................................................

    /**
     * Create a new RecordManagerException
     * @param error an error message
     * @param cause an exception which is the cause for this exception
     */
    public RecordManagerException(String error, Exception cause) {
        super(error);
        this.errorMessage = error;
        this.theCause     = cause;
    }

    //~ Methods ....................................................................................

    /**
     * Get the cause of this exception
     * @return the cause
     */
    public Exception getExceptionCause() {
        return theCause == null ? new Exception("unkown reason") : theCause;
    }


    /**
     * Get the error message
     * @return the message
     */
    public String getMessage() {
        return errorMessage == null ? "" : errorMessage;
    }
}
