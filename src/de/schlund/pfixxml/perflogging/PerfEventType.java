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

package de.schlund.pfixxml.perflogging;


/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public enum PerfEventType {
    
    XMLSERVER_PRE_PROCESS,
    IHANDLER_IS_ACTIVE,IHANDLER_NEEDS_DATA,IHANDLER_PREREQUISITES_MET,IHANDLER_HANDLE_SUBMITTED_DATA,
    CONTEXTRESOURCE_INSERT_STATUS,CONTEXTRESOURCE_OBSERVER_UPDATE,CONTEXT_CREATE_NAVI_COMPLETE,CONTEXT_CREATE_NAVI_REUSE,
    PAGE_IS_ACCESSIBLE,PAGE_NEEDS_DATA,PAGE_HANDLE_SUBMITTED_DATA,PAGE_INIT_IWRAPPERS,PAGE_RETRIEVE_CURRENT_STATUS,
    XMLSERVER_HANDLE_DOCUMENT,XMLSERVER_CALL_PROCESS,XMLSERVER_GET_DOM,
    PFIXSERVLETREQUEST_INIT,
    WEBSERVICE_PROCESSING,WEBSERVICE_INVOCATION;
    
}

