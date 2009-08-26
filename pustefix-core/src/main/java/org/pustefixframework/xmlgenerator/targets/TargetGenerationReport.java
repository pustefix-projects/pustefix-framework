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
package org.pustefixframework.xmlgenerator.targets;

import java.util.ArrayList;

public class TargetGenerationReport {
	
    private final ArrayList<Exception> exceptions;
    private boolean hasError;
    
    public TargetGenerationReport() {
        this.exceptions = new ArrayList<Exception>();
    }
    
    public void addError(Exception e) {
    	exceptions.add(e);
    	hasError = true;
    }
    
    public boolean hasError() {
        return hasError;
    }
 
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
        String prod_break = "'============================================================================================'\n";
        String ex_break = "|----------------------------------------------------------------------------------\n";
        if (exceptions.isEmpty()) {
            sb.append(prod_break);
            sb.append("| No exceptions\n");
            sb.append(prod_break);
        } else {
        	sb.append(prod_break);
            sb.append("| Exceptions: ").append("\n");
            for (int i = 0; i < exceptions.size(); i++) {
                TargetGenerationException tgex = (TargetGenerationException)exceptions.get(i);
                String str = tgex.toStringRepresentation();
                sb.append(str);
                if (exceptions.size() - 1 > i) sb.append(ex_break);
            }
           sb.append(prod_break);
        }
        return sb.toString();
    }
}