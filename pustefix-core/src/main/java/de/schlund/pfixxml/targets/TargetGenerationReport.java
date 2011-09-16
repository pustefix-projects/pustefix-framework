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
package de.schlund.pfixxml.targets;

import java.util.ArrayList;

public class TargetGenerationReport {
    
    private ArrayList<Exception> errors = new ArrayList<Exception>();
    
    public TargetGenerationReport() {
    }
    
    public void addError(Exception e) {
        errors.add(e);
    }
    
    public boolean hasError() {
        return !errors.isEmpty();
    }
 
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(255);
        String prod_break = "'============================================================================================'\n";
        String ex_break = "|----------------------------------------------------------------------------------\n";
        if (errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            sb.append(prod_break);
            sb.append("| No exceptions\n");
            sb.append(prod_break);
            return sb.toString();
        }
        buf.append(prod_break);
        buf.append("| Exceptions: ").append("\n");
        for (int i = 0; i < errors.size(); i++) {
            TargetGenerationException tgex = (TargetGenerationException) errors.get(i);
                String str = tgex.toStringRepresentation();
                buf.append(str);
                if (errors.size() - 1 > i)
                    buf.append(ex_break);
            }
            buf.append(prod_break);
        
        return buf.toString();
    }
}