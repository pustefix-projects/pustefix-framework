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

package de.schlund.pfixcore.editor.resources;

/**
 * EditorSearchContext.java
 *
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 *
 */

public class EditorSearchContext {
    private String pre;
    private String match;
    private String post;
    
    public EditorSearchContext(String pre, String match, String post) {
        this.pre   = pre;
        this.match = match;
        this.post  = post;
    }
    
    /**
     * Gets the value of pre
     *
     * @return the value of pre
     */
    public String getPre() {
        return this.pre;
    }
    
    /**
     * Gets the value of match
     *
     * @return the value of match
     */
    public String getMatch() {
            return this.match;
    }
    
    /**
     * Gets the value of post
     *
     * @return the value of post
     */
    public String getPost() {
        return this.post;
    }
}

