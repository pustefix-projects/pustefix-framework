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

package de.schlund.pfixcore.editor2.core.exception;

/**
 * Exception signaling something went wrong during the initialization process
 * (e.g. error during loading of configuration file).
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorInitializationException extends EditorException {

    /**
     * 
     */
    private static final long serialVersionUID = 4679520574932496001L;

    public EditorInitializationException() {
        super();
    }

    public EditorInitializationException(String arg0) {
        super(arg0);
    }

    public EditorInitializationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public EditorInitializationException(Throwable arg0) {
        super(arg0);
    }

}
