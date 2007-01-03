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
 */

package de.schlund.pfixcore.exception;

/**
 * Signals an error within application code. This exception is usually thrown
 * by Pustefix core code when it receives an exception from application code
 * and contains the original exception as the cause.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class PustefixApplicationException extends PustefixException {

    private static final long serialVersionUID = -2344395245761071775L;

    public PustefixApplicationException() {
        super();
    }

    public PustefixApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PustefixApplicationException(String message) {
        super(message);
    }

    public PustefixApplicationException(Throwable cause) {
        super(cause);
    }

}
