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
package de.schlund.pfixxml.targets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TargetGenerationReport {
    private final HashMap hash;

    public TargetGenerationReport() {
        this.hash = new HashMap();
    }
    
    public void addError(Exception e, String project) {
        if (hash.get(project) == null) {
            ArrayList list = new ArrayList();
            list.add(e);
            hash.put(project, list);
        } else {
            ((ArrayList) hash.get(project)).add(e);
        }
    }
 
    public String toString() {
        StringBuffer buf = new StringBuffer(255);
        String prod_break = "'============================================================================================'\n";
        String ex_break = "|----------------------------------------------------------------------------------\n";
        if (hash.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            sb.append(prod_break);
            sb.append("| No exceptions\n");
            sb.append(prod_break);
            return sb.toString();
        }
        Iterator iter = hash.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            buf.append(prod_break);
            buf.append("| Project: ").append((String) key).append("\n");
            ArrayList exs = (ArrayList) hash.get(key);
            buf.append("| Exceptions: ").append("\n");
            for (int i = 0; i < exs.size(); i++) {
                TargetGenerationException tgex = (TargetGenerationException) exs
                        .get(i);
                String str = tgex.toStringRepresentation();
                buf.append(str);
                if (exs.size() - 1 > i)
                    buf.append(ex_break);
            }
            buf.append(prod_break);
        }
        return buf.toString();
    }
}