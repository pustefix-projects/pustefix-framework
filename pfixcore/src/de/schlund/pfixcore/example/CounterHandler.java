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

import org.pustefixframework.container.annotations.Inject;

import de.schlund.pfixcore.example.iwrapper.Counter;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * CounterHandler.java
 *
 * Created: Wed Nov 21 18:44:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class CounterHandler implements IHandler {
    // private final static Logger LOG  = Logger.getLogger(CounterHandler.class);

    private ContextCounter cc;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Counter counter     = (Counter) wrapper;
        Boolean showcounter = counter.getShowCounter();
        Integer count       = counter.getAdd();

        if (showcounter != null) {
            cc.setShowCounter(showcounter);
        }

        if (cc.getShowCounter().booleanValue() && count != null) {
            cc.addToCounter(count);
            // demo of pageMessage feature
            int c = cc.getCounter();
            if (c > 9 ) {
                context.addPageMessage(StatusCodeLib.COUNTER_WARN_GREATER_9, new String[] {"" + c}, "error");
                context.prohibitContinue();
            } else if (c > 5 ) {
                context.addPageMessage(StatusCodeLib.COUNTER_WARN_GREATER_5, new String[] {"" + c}, "warn");
            } else if (c > 3 ) {
                context.addPageMessage(StatusCodeLib.COUNTER_INFO_GREATER_3, new String[] {"" + c}, "info");
            }
        }
        

    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // We do nothing here. There are no form elements that need pre-filling.
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

    @Inject
    public void setContextCounter(ContextCounter cc) {
        this.cc = cc;
    }

}// CounterHandler
