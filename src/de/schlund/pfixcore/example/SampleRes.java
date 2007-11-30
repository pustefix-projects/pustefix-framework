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

import de.schlund.pfixcore.workflow.Context;



/**
 * SampleRes.java
 *
 *
 * Created: Thu Nov 29 23:50:06 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public abstract class SampleRes {
    private final static String AINFO   = "de.schlund.pfixcore.example.ContextAdultInfo";
    private final static String TROUSER = "de.schlund.pfixcore.example.ContextTrouser";
    private final static String TSHIRT  = "de.schlund.pfixcore.example.ContextTShirt";
    private final static String COUNTER = "de.schlund.pfixcore.example.ContextCounter";
    private final static String PIC     = "de.schlund.pfixcore.example.ContextTogglePic";
    
    public static ContextAdultInfo getContextAdultInfo(Context context) {
        return (ContextAdultInfo) context.getContextResourceManager().getResource(AINFO);
    }
    
    public static ContextTrouser getContextTrouser(Context context) {
        return (ContextTrouser) context.getContextResourceManager().getResource(TROUSER);
    }

    public static ContextTShirt getContextTShirt(Context context) {
        return (ContextTShirt) context.getContextResourceManager().getResource(TSHIRT);
    }

    public static ContextCounter getContextCounter(Context context) {
        return (ContextCounter) context.getContextResourceManager().getResource(COUNTER);
    }

    public static ContextTogglePic getContextTogglePic(Context context) {
        return (ContextTogglePic) context.getContextResourceManager().getResource(PIC);
    }

}// SampleRes
