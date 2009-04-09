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

import de.schlund.pfixcore.example.iwrapper.TShirt;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * TShirtHandler.java
 *
 *
 * Created: Thu Oct 18 18:53:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class TShirtHandler implements IHandler {

    private ContextTShirt cts;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        TShirt        tshirt  = (TShirt) wrapper;
        Integer       color   = tshirt.getColor();
        String        size    = tshirt.getSize();
        Integer[]     feature = tshirt.getFeature();

        if (size.equals("L") && color.equals(new Integer(2))) {
            // The combination size "L" and color No. "2" is considered invalid (maybe out of stock) 
            tshirt.addSCodeSize(StatusCodeLib.TSHIRT_SIZECOLOR_OUTOF_STOCK, new String[]{"L", "2"}, "note");
            return;
        }

        // Everything was ok, store it.
        cts.setSize(size);
        cts.setColor(color);
        if (feature != null) {
            cts.setFeature(feature);
        } else {
            cts.setFeature(new Integer[]{new Integer(-1)});
            // This is needed so we produce some output at all on retrieveCurrentStatus when
            // the user decided to NOT check any checkbox in the UI (this makes defaults work)
        }
        
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        TShirt                 tshirt = (TShirt) wrapper;

        if (!cts.needsData()) {
            tshirt.setColor(cts.getColor());
            tshirt.setSize(cts.getSize());
            tshirt.setFeature(cts.getFeature());
        }
    }
    
    public boolean needsData(Context context) throws Exception{
        return cts.needsData();
    }
    
    public boolean prerequisitesMet(Context context) throws Exception{
        return true;
    }

    public boolean isActive(Context context) throws Exception{
        return true;
    }
    
    @Inject
    public void setContextTShirt(ContextTShirt cts) {
        this.cts = cts;
    }

}// TShirtHandler
