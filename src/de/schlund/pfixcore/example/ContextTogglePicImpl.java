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

package de.schlund.pfixcore.example;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;


/**
 * ContextTogglePicImpl.java
 *
 *
 * Created: Tue Apr 23 19:18:25 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class ContextTogglePicImpl implements ContextTogglePic {
    // implementation of de.schlund.pfixcore.workflow.ContextResource interface

    boolean do_show = false;
    
/**
 *
 * @exception java.lang.Exception <description>
 */
    public void reset() throws Exception {
        do_show = false;
    }

/**
 *
 * @param param1 <description>
 * @exception java.lang.Exception <description>
 */
    public void init(Context param1) throws Exception {
        //
    }

/**
 *
 * @param param1 <description>
 * @param param2 <description>
 * @exception java.lang.Exception <description>
 */
    public void insertStatus(ResultDocument resdoc, Element root) throws Exception {
        root.setAttribute("do_show", "" + do_show);
    }

/**
 *
 * @return <description>
 * @exception java.lang.Exception <description>
 */
    public boolean needsData() throws Exception {
        return false;
    }
// implementation of de.schlund.pfixcore.example.ContextTogglePic interface

/**
 *
 * @param param1 <description>
 */
    public void setShow(boolean param1) {
        do_show = param1;
    }

/**
 *
 * @return <description>
 */
    public boolean getShow() {
        return do_show;
    }

}// ContextTogglePicImpl
