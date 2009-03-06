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
 * Exception signalling include part has been edited by two users concurrently
 * and conflicts have to be resolved.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class EditorIncludeHasChangedException extends EditorException {

    /**
     * 
     */
    private static final long serialVersionUID = -1940266869207320438L;

    private String merged;

    private String hash;

    public EditorIncludeHasChangedException(String merged, String hash) {
        super();
        this.merged = merged;
        this.hash = hash;
    }

    /**
     * Returns a text containing changes from both versions.
     * 
     * @return Merged text
     */
    public String getMerged() {
        return this.merged;
    }

    /**
     * Returns hash value of the saved text which has been used for the merger.
     * 
     * @return Hash value of saved version
     */
    public String getNewHash() {
        return this.hash;
    }
}
