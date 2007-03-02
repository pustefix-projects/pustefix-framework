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

import de.schlund.pfixcore.example.iwrapper.*;
import de.schlund.pfixcore.generator.*;
import de.schlund.pfixcore.workflow.*;
import de.schlund.util.statuscodes.StatusCodeLib;

import org.apache.log4j.*;

/**
 * CounterSetHandler.java
 *
 *
 * Created: Wed Nov 21 18:44:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class CounterSetHandler implements IHandler {
    private static final Logger LOG = Logger.getLogger(CounterSetHandler.class);

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        CounterInput    counter = (CounterInput) wrapper;
        ContextCounter  cc      = SampleRes.getContextCounter(context);

        Integer count = counter.getSet();
        if (count != null) {
            cc.setCounter(count.intValue());
        }
        
        // demo of pageMessage feature
        int c = cc.getCounter();
        if (c > 9 ) {
            context.addPageMessage(StatusCodeLib.PFIXCORE_EXAMPLE_COUNTER_WARN_GREATER_9, new String[] {""+c}, "error");
            context.prohibitContinue();
        } else if (c > 5 ) {
            context.addPageMessage(StatusCodeLib.PFIXCORE_EXAMPLE_COUNTER_WARN_GREATER_5, new String[] {""+c}, "warn");
        } else if (c > 3 ) {
            context.addPageMessage(StatusCodeLib.PFIXCORE_EXAMPLE_COUNTER_INFO_GREATER_3, new String[] {""+c}, "info");
        }
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        CounterInput    counter = (CounterInput) wrapper;
        ContextCounter  cc      = SampleRes.getContextCounter(context);
        counter.setStringValSet("" + cc.getCounter());
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

}// CounterSetHandler
