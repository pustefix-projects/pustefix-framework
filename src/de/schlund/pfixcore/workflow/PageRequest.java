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

package de.schlund.pfixcore.workflow;


/**
 * Describe class <code>PageRequest</code> here.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */
public class PageRequest {
    private              PageRequestStatus status    = PageRequestStatus.UNDEF;
    private              String            preqname;
    private              String            rootname;

    /**
     * Describe <code>getName</code> method here.
     *
     * @return a <code>String</code> value
     */
    public String getName() {
        return preqname;
    }

    /**
     * Describe <code>toString</code> method here.
     *
     * @return a <code>String</code> value
     */
    @Override
    public String toString() { 
        return getName();
    }

    /**
     * Describe <code>setStatus</code> method here.
     *
     * @param status a <code>PageRequestStatus</code> value
     */
    public void setStatus(PageRequestStatus status) {
        this.status = status;
    }

    /**
     * Describe <code>getStatus</code> method here.
     *
     * @return a <code>PageRequestStatus</code> value
     */
    public PageRequestStatus getStatus() {
        return status;
    }

    /**
     * Describe <code>equals</code> method here.
     *
     * @param arg an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    @Override
    public boolean equals(Object arg) {
        if ((arg != null) && (arg instanceof PageRequest)) {
            return preqname.equals(((PageRequest) arg).getName());
        } else {
            return false;
        }
    }

    /**
     * Describe <code>hashCode</code> method here.
     *
     * @return an <code>int</code> value
     */
    @Override
    public int hashCode() {
        if (preqname == null) {
	    return 0;
        } else {
	    return preqname.hashCode();
        }
    }

    /**
     * Describe <code>isEmpty</code> method here.
     *
     * @return a <code>boolean</code> value
     */
//     public boolean isEmpty() {
//         if (preqname == null) {
//             return true;
//         } else {
//             return false;
//         }
//     }
    
    /**
     * Creates a new <code>PageRequest</code> instance.
     *
     * @param name a <code>String</code> value
     */
    public PageRequest(String name) {
        preqname = name;
        if (name != null && name.indexOf("::") > 0) {
            rootname = name.substring(0, name.indexOf("::"));
        } else {
            rootname = name;
        }
        // CAT.debug("=== PR: " + preqname + " (" + rootname + ") ===");
    }

    public String getRootName() {
        return rootname;
    }

}
