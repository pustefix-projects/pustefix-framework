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

import de.schlund.pfixxml.*;
import java.util.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

/**
 *
 *
 */

public class PageRequest {
    public  static final String            PAGEPARAM = "__page";
    private static final Category          CAT       = Category.getInstance(PageRequest.class.getName());
    private              PageRequestStatus status    = PageRequestStatus.UNDEF;
    private              String            pagename;
    
    public String getName() {
	return pagename;
    }

    public String toString() {
	return getName();
    }

    public void setStatus(PageRequestStatus status) {
        this.status = status;
    }

    public PageRequestStatus getStatus() {
        return status;
    }
    
    public boolean equals(Object arg) {
	if ((arg != null) && (arg instanceof PageRequest)) {
	    return pagename.equals(((PageRequest) arg).getName());
	} else {
	    return false;
	}
    }

    public int hashCode() {
	if (pagename == null) {
	    return 0;
	} else {
	    return pagename.hashCode();
	}
    }

    public boolean isEmpty() {
        if (pagename == null) {
            return true;
        } else {
            return false;
        }
    }
    
    public PageRequest(PfixServletRequest preq) {
        pagename = null;
        String       pathinfo = preq.getPathInfo();
        RequestParam name     = preq.getRequestParam(PAGEPARAM);
        if (name != null && !name.getValue().equals("")) {
            pagename = name.getValue();
        } else if (pathinfo != null && !pathinfo.equals("") && 
                   pathinfo.startsWith("/") && pathinfo.length() > 1) {
            pagename = pathinfo.substring(1);
        }
    }

    public PageRequest(String name){
	pagename = name;
    }

}
