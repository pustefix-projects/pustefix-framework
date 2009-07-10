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

package org.pustefixframework.resource;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Resource providing an output stream to write to its content.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface OutputStreamResource extends Resource {
    /**
     * Opens an output stream to write to this resource's content.
     * 
     * @return output stream writing to the content of this resource
     * @throws IOException if output stream cannot be opened
     */
    OutputStream getOutputStream() throws IOException;
}
