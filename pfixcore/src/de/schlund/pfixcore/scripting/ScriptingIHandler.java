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

package de.schlund.pfixcore.scripting;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.scripting.ScriptingManager;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.log4j.Logger;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingIHandler implements IHandler{
  
    private static Logger LOG = Logger.getLogger(ScriptingIHandler.class);

    private BSFEngine engine = null;
    
    /**
     */
    private Script script = null;
    
    
    /**
     * @exception IllegalArgumentException if script is <code>null</code> 
     */
    public ScriptingIHandler(Script script) {
        if ( script == null ) 
            throw new IllegalArgumentException("Parameter 'script' is not allowed to be null");
        
        this.script = script;
        
        try {
            // preload script
            engine = ScriptingManager.getInstance().executeScript(script);
        } catch (BSFException ex) {
            LOG.warn("Exception in ScriptableHandler's constructor", ex);
            throw new IllegalArgumentException("Exception while initializing ScriptableHandler: "+ex.getMessage());
        }
    }
  
    
    /**
     * 
     */
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        engine.call(null, "handleSubmittedData", new Object[] {context, wrapper});
    }
    
    
    /**
     * 
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        engine.call(null, "retrieveCurrentStatus", new Object[] {context, wrapper});
    }
    
    /**
     * 
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        return exec("prerequisitesMet", context);
    }
    
    /**
     * 
     */
    public boolean isActive(Context context) throws Exception {
        return exec("isActive", context);
    }
    
    
    /**
     * 
     */
    public boolean needsData(Context context) throws Exception {
        return exec("needsData", context);
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
     private boolean exec(String methodName, Context context) throws Exception {
        Boolean bool = null;
        try {
            bool = (Boolean) engine.call(null, methodName, new Object[] {context});
        } catch (ClassCastException ex) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                   script+".methodName() returned no boolean value! "+ex.getMessage());
        } 
        
        if ( bool == null )
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                   script.getName()+"."+methodName+"() returned null Boolean value!");
        
        return bool.booleanValue();
     }
}
