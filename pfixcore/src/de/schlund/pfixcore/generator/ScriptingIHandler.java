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

package de.schlund.pfixcore.generator;

import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingIHandler implements IHandler{
  
    /**
     * 
     */
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
    }
    
    
    /**
     * 
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
    }
    
    
    /**
     * 
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }
    
    /**
     * 
     */
    public boolean isActive(Context context) throws Exception {
        return true;
    }
    
    
    /**
     * 
     */
    public boolean needsData(Context context) throws Exception {
        return true;
    }
}
