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

package de.schlund.pfixcore.example;

import org.pustefixframework.container.annotations.Inject;

import de.schlund.pfixcore.example.iwrapper.TogglePic;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;


/**
 * TogglePicHandler.java
 *
 *
 * Created: Tue Apr 23 19:09:39 2002
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class TogglePicHandler implements IHandler {
// implementation of de.schlund.pfixcore.generator.IHandler interface

    private ContextTogglePic ctp;

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        TogglePic        tpic = (TogglePic) wrapper;
        Boolean          show = tpic.getToggle();
        if ((show != null) && show.equals(Boolean.TRUE)) {
            ctp.setShow(!ctp.getShow());
        }
    }

    public void retrieveCurrentStatus(Context param1, IWrapper param2) throws Exception {
        // 
    }

    public boolean prerequisitesMet(Context param1) throws Exception {
        return true;
    }

    public boolean isActive(Context param1) throws Exception {
        return true;
    }

    public boolean needsData(Context param1) throws Exception {
        return false;
    }
    
    @Inject
    public void setContextTogglePic(ContextTogglePic ctp) {
        this.ctp = ctp;
    }

}// TogglePicHandler
