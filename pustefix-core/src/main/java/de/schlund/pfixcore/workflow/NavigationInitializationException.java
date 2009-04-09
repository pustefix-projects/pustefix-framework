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

package de.schlund.pfixcore.workflow;

import de.schlund.pfixcore.exception.PustefixCoreException;

/**
 * Thrown by {@link NavigationFactory}} to signal an error that ocurred while
 * loading a navigation file.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class NavigationInitializationException extends PustefixCoreException {

    private static final long serialVersionUID = -8109785252428730732L;

    public NavigationInitializationException() {
        super();
    }

    public NavigationInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NavigationInitializationException(String message) {
        super(message);
    }

    public NavigationInitializationException(Throwable cause) {
        super(cause);
    }

}
