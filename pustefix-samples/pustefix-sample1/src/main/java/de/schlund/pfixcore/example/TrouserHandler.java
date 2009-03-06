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

import java.util.Arrays;
import java.util.HashSet;

import org.pustefixframework.container.annotations.Inject;

import de.schlund.pfixcore.example.iwrapper.Trouser;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * TrouserHandler.java
 *
 *
 * Created: Thu Oct 18 18:53:20 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class TrouserHandler implements IHandler {

    private ContextAdultInfo cai;
    private ContextTrouser ct;
    
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Trouser        trouser = (Trouser) wrapper;
        Integer        color   = trouser.getColor();
        String         size    = trouser.getSize();
        Integer[]      feature = trouser.getFeature();

        // Sample Check: reject a certain combination
        if (feature != null && color.equals(new Integer(2))) {
            HashSet<Integer> set = new HashSet<Integer>(Arrays.asList(feature));
            if (set.contains(new Integer(1))) {
                // The combination of feature 1 and color No. 2 is invalid (maybe out of stock) 
                String[]   args  = new String[2];
                args[0] = "1";
                args[1] = "2";
                trouser.addSCodeColor(StatusCodeLib.TROUSER_FEATURECOLOR_OUTOF_STOCK, args, null);
                return;
            }
        }

        // Everything was ok, store it.
        ct.setSize(size);
        ct.setFeature(feature);
        ct.setColor(color);
    }
    
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        Trouser        trouser = (Trouser) wrapper;

        // we use the ct.needsData() call to look if the Context Ressource has meaningful content
        // to display. This may be handled differently depending on the case.
        if (!ct.needsData()) {
            trouser.setColor(ct.getColor());
            trouser.setSize(ct.getSize());
            trouser.setFeature(ct.getFeature());
        }
    }
    
    public boolean needsData(Context context) throws Exception{
        return ct.needsData();
    }
    
    public boolean prerequisitesMet(Context context) throws Exception{
        return !cai.needsData();
    }
    
    public boolean isActive(Context context) throws Exception{
        Boolean adult = cai.getAdult();
        if (adult != null) {
            // Depending on the situation, this may or may not be the right thing to do:
            // The result of this code is that the current information in ct will be lost as
            // soon as cai.getAdult becomes false (again).  
            if (adult.booleanValue() == false) {
                ct.reset();
            }
            return adult.booleanValue();
        } else {
            return false;
        }
    }
    
    @Inject
    public void setContextTrouser(ContextTrouser ct) {
        this.ct = ct;
    }

    @Inject
    public void setContextAdultInfo(ContextAdultInfo cai) {
        this.cai = cai;
    }    
    
}// TrouserHandler
