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

import de.schlund.pfixxml.*;
import de.schlund.pfixcore.example.iwrapper.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.util.statuscodes.*;
import org.apache.log4j.*;

/**
 * AdultInfoHandler.java
 *
 *
 * Created: Thu Oct 18 18:53:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class AdultInfoHandler implements IHandler {
    private Category  CAT  = Category.getInstance(this.getClass().getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        AdultInfo              info    = (AdultInfo) wrapper;
        ContextAdultInfo       cai     = SampleRes.getContextAdultInfo(context);
        cai.setAdult(info.getAdult());
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        AdultInfo              info = (AdultInfo) wrapper;
        ContextAdultInfo       cai  = SampleRes.getContextAdultInfo(context);

        if (!cai.needsData()) {
            info.setStringValAdult(cai.getAdult().toString());
        } 
    }
    
    public boolean needsData(Context context) throws Exception{
        ContextAdultInfo       cai = SampleRes.getContextAdultInfo(context);
        return cai.needsData();
    }
    
    public boolean isActive(Context context) throws Exception{
        return true;
    }
    
    public boolean prerequisitesMet(Context context) throws Exception{
        return true;
    }

}// AdultInfoHandler
