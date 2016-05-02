/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixxml;

import de.schlund.pfixcore.exception.PustefixCoreException;

/**
 * Signals an error that occured while AbstractXMLServlet was trying to render
 * a page.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RenderingException extends PustefixCoreException {

    private static final long serialVersionUID = -3105151699364263553L;

    public RenderingException() {
        super();
    }

    public RenderingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenderingException(String message) {
        super(message);
    }

    public RenderingException(Throwable cause) {
        super(cause);
    }

}
