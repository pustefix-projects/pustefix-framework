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

package de.schlund.pfixcore.util;

import org.apache.log4j.*;

public class Meminfo {
    private static long     last_used = -1l;
    private static Category CAT = Category.getInstance(Meminfo.class.getName());

    public static synchronized void print (String info) {
        long free, total, used_new_bg, used_new_ag;
        Runtime run = Runtime.getRuntime();
        free        = run.freeMemory();
        total       = run.totalMemory();
        used_new_bg = total - free;
        
        CAT.debug(",---------------------------------------------------------------");
        if (info != null) {
            CAT.debug("| "+info);
        }
        CAT.debug("| Meminfo (before GC): "+ free +" free, "+ total +" total => " + used_new_bg +" used.");
        run.gc();
        free        = run.freeMemory();
        total       = run.totalMemory();
        used_new_ag = total - free;
        CAT.debug("| Meminfo (after  GC): "+ free +" free, "+ total +" total => " + used_new_ag +" used.");
        long freed = (used_new_bg - used_new_ag);
        if (freed > 0) {
            CAT.debug("|       => "+ freed +" freed by GC.");
        } else if (freed < 0) {
            CAT.debug("| ????  => GC did COST "+ -freed +" ????");
        }
        if (last_used != -1) {
            CAT.debug("|       => "+ (used_new_ag - last_used) +" difference to last run.");
        }
        CAT.debug("`---------------------------------------------------------------\n");
        last_used = used_new_ag;
    }
}
