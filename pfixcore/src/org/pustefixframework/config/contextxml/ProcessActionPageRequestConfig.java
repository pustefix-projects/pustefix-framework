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

package org.pustefixframework.config.contextxml;


/**
 * Provides configuration for process actions of a pagerequest instance
 * 
 * @author Jens Lautenbacher <jtl@1und1.de>
 */
public interface ProcessActionPageRequestConfig {

    /**
     * Returns the name of the action.
     * 
     * @return String
     */
    String getName();
    /**
     * 
     * @return String if a pageflow should be set by this action (or null)
     */
    String getPageflow();
    /**
     * 
     * @return String if a jumpToPage should be set by this action (or null)
     */
    String getJumpToPage();
    /**
     * 
     * @return String if a jumpToPageFlow should be set by this action (or null)
     */
    String getJumpToPageFlow();
    /**
     * 
     * @return String 'true' if the pageflow should stop here, 'step' if it should advance at most one step further, 'false' otherwise
     */
    String getForceStop();
}