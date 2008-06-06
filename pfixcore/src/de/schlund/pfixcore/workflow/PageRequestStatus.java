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
 * PageRequestStatus.java
 *
 *
 * Created: Thu Oct  4 14:10:35 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 */

public class PageRequestStatus {
    public static final PageRequestStatus WORKFLOW = new PageRequestStatus("WORKFLOW");
    public static final PageRequestStatus SELECT   = new PageRequestStatus("SELECT");
    public static final PageRequestStatus JUMP     = new PageRequestStatus("JUMP");
    public static final PageRequestStatus UNDEF    = new PageRequestStatus("UNDEF");
    
    private String tag;
    
    private PageRequestStatus(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "[" + getTag() + "]";
    }
    
    public String getTag() {
        return tag;
    }
    
}// PageRequestStatus
