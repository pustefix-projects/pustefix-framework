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

package de.schlund.pfixcore.util.basicapp.objects;

import org.apache.log4j.Logger;

/**
 * Representation of  a servlet object for the
 * project
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */

public class ServletObject {
    private static final Logger LOG = Logger.getLogger(ServletObject.class);
    private String servletName = null;
    private int objId          = 0;
    private int counter        = 0;
    
    public ServletObject(int objId) {
        LOG.debug("A new Servlet Object has been created. Id = " + objId);
        counter++;
        this.objId = objId;
    }
    
    //--> Start Getter and setter
    /**
     * @return Returns the servletName.
     */
    public String getServletName() {
        return servletName;
    }
    /**
     * @param servletName The servletName to set.
     */
    public void setServletName(String servletName) {
        LOG.debug("The ServletName for Servlet " + objId + " is: " + servletName);
        this.servletName = servletName;
    }  
    /**
     * @return Returns the objId.
     */
    public int getObjId() {
        return objId;
    }
    /**
     * returns the counter
     */
    public int getCounter() {
        return counter;
    }
}
