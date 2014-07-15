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

import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.example.iwrapper.Counter;
import de.schlund.pfixcore.workflow.Context;

/**
 * CounterHandler.java
 *
 * Created: Wed Nov 21 18:44:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class CounterHandler implements InputHandler<Counter> {

    @Autowired
    private Context context;
    private ContextCounter cc;
    
    public void handleSubmittedData(Counter counter) {
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

    public void retrieveCurrentStatus(Counter counter) {
        // We do nothing here. There are no form elements that need pre-filling.
    }

    public boolean needsData() {
        return false;
    }

    public boolean isActive() {
        return true;
    }

    public boolean prerequisitesMet() {
        return true;
    }

    @Autowired
    public void setContextCounter(ContextCounter cc) {
        this.cc = cc;
    }

}
