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
 *
 */

package de.schlund.pfixcore.example;

import org.pustefixframework.container.annotations.Inject;

import de.schlund.pfixcore.example.iwrapper.CounterInput;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

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
    // private static final Logger LOG = Logger.getLogger(CounterSetHandler.class);

    private ContextCounter contextCounter;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        CounterInput counter = (CounterInput) wrapper;
        Integer      count = counter.getSet();
        if (count != null) {
            
            contextCounter.setCounter(count);

            if (count > 9 ) {
                context.addPageMessage(StatusCodeLib.COUNTER_WARN_GREATER_9, new String[] {"" + count}, "error");
                context.prohibitContinue();
            } else if (count > 5 ) {
                context.addPageMessage(StatusCodeLib.COUNTER_WARN_GREATER_5, new String[] {"" + count}, "warn");
            } else if (count > 3 ) {
                context.addPageMessage(StatusCodeLib.COUNTER_INFO_GREATER_3, new String[] {"" + count}, "info");
            }
        }
        
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        CounterInput counter = (CounterInput) wrapper;
        counter.setStringValSet("" + contextCounter.getCounter());
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
    public void setContextCounter(ContextCounter contextCounter) {
        this.contextCounter = contextCounter;
    }    

}// CounterSetHandler
