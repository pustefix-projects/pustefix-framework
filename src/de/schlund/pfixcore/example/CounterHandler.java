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

import de.schlund.util.statuscodes.*;
import de.schlund.pfixcore.example.iwrapper.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import org.apache.log4j.*;

/**
 * CounterHandler.java
 *
 *
 * Created: Wed Nov 21 18:44:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class CounterHandler implements IHandler {
    private Category  CAT  = Category.getInstance(this.getClass().getName());

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Counter                counter = (Counter) wrapper;
        ContextCounter         cc      = SampleRes.getContextCounter(context);

        Boolean showcounter = counter.getShowCounter();
        Integer count       = counter.getAdd();

        if (showcounter != null) {
            cc.setShowCounter(showcounter);
        }

        if (cc.getShowCounter().booleanValue() && count != null) {
            cc.addToCounter(count.intValue());
        }

        // demo of pageMessage feature
        if ( cc.getCounter() > 5 ) {
            StatusCodeFactory sfac = new StatusCodeFactory("pfixcore.example.counter");
            context.addPageMessage(sfac.getStatusCode("WARN_GREATER_5"), "warn");
        } else if ( cc.getCounter() > 3 ) {
            StatusCodeFactory sfac = new StatusCodeFactory("pfixcore.example.counter");
            context.addPageMessage(sfac.getStatusCode("INFO_GREATER_3"), "info", new String[] {"3"});
        }

    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // We do nothing here. There are no formelements that need prefilling.
    }

    public boolean needsData(Context context) {
        return false;
    }

    public boolean isActive(Context context) {
        return true;
    }

    public boolean prerequisitesMet(Context context) {
        return true;
    }

}// CounterHandler
