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


public final class Meminfo {

    //~ Instance/static variables ..................................................................

    private static long last_used = -1L;
    private static Logger LOG = Logger.getLogger(Meminfo.class);

    //~ Methods ....................................................................................

    public final static synchronized void print(String info) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        long    free;
        long    total;
        long    used_new_bg;
        long    used_new_ag;
        Runtime run = Runtime.getRuntime();
        free        = run.freeMemory();
        total       = run.totalMemory();
        used_new_bg = total - free;
        StringBuffer debugString = new StringBuffer(512);
        debugString.append("\n,---------------------------------------------------------------\n");
        if (info != null) {
            debugString.append("|").append(info).append("\n");
        }
        debugString.append("| Meminfo (before GC): ").append(free).append(" free, ").append(total).
        	append(" total => ").append(used_new_bg).append(" used.").append("\n");
        run.gc();
        free        = run.freeMemory();
        total       = run.totalMemory();
        used_new_ag = total - free;
        debugString.append("| Meminfo (after  GC): ").append(free).append(" free, ").append(total).
        	append(" total => ").append(used_new_ag).append(" used.").append("\n");
        long freed = (used_new_bg - used_new_ag);
        if (freed > 0) {
            debugString.append("|       => ").append(freed).append(" freed by GC.").append("\n");
        } else if (freed < 0) {
            debugString.append("| ????  => GC did COST ").append(-freed).append(" ????").
            	append("\n");
        }
        if (last_used != -1) {
            debugString.append("|       => ").append(used_new_ag - last_used).
            	append(" difference to last run.").append("\n");
        }
        debugString.append("`---------------------------------------------------------------\n");
        last_used = used_new_ag;
        LOG.debug(debugString.toString());
    }
}