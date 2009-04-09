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

package org.pustefixframework.editor.common.exception;

/**
 * Signals a problem with the index during a search operation.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorSearchIndexException extends EditorSearchException {

    /**
     * 
     */
    private static final long serialVersionUID = -7099684812635407162L;

    public EditorSearchIndexException() {
        super();
    }

    public EditorSearchIndexException(String arg0) {
        super(arg0);
    }

    public EditorSearchIndexException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public EditorSearchIndexException(Throwable arg0) {
        super(arg0);
    }

}
