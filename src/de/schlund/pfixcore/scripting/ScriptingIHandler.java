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

import java.io.IOException;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 *
 */
public class ScriptingIHandler implements IHandler{

    // ****
    // NOTE:
    // ****
    //
    // This IHandler has instance variables because it is created new each time and not
    // controlled by the IHandlerFactory. This may change soon, but for now it works OK.
    
    private final static Logger LOG = Logger.getLogger(ScriptingIHandler.class);
  
    /**
     */
    private BSFEngine bsfEngine = null; 
    
    /**
     */
    private String path = null;
    
    /**
     */
    private boolean init = false;
    
    
    /**
     * @exception IllegalArgumentException if the provided path is <code>null</code>,
     */
    public ScriptingIHandler(String path) {
        if ( path == null )
            throw new IllegalArgumentException("Parameter 'path' is not allowed to be null");
        
        this.path = path;
    }
    
    
    /**
     */
    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        init();
        bsfEngine.call(null, "handleSubmittedData", new Object[]{context, wrapper});
    }
    
    
    /**
     */
    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        init();
        bsfEngine.call(null, "retrieveCurrentStatus", new Object[]{context, wrapper});
    }
    
    
    /**
     */
    public boolean prerequisitesMet(Context context) throws Exception {
        init();
        return ScriptingUtil.exec(bsfEngine, "prerequisitesMet", new Object[] {context});
    }
    
    
    /**
     */
    public boolean isActive(Context context) throws Exception {
        init();
        return ScriptingUtil.exec(bsfEngine, "isActive", new Object[] {context});
    }
    
    
    /**
     */
    public boolean needsData(Context context) throws Exception {
        init();
        return ScriptingUtil.exec(bsfEngine, "needsData", new Object[] {context});
    }
    
    
    /**
     * 
     */
    protected void init() throws BSFException, IOException {
      
        if ( !init ) {
            LOG.debug("Initializing ScriptingIHandler for path: "+path);
            
            String lang = BSFManager.getLangFromFilename(path);
      
            BSFManager manager = new BSFManager();
            manager.declareBean("LOG", LOG, Logger.class);
            manager.exec(lang, path, 0, 0, ScriptingUtil.getScript(path));
            
            bsfEngine = manager.loadScriptingEngine(lang);
            
            init = true;
        }
            
    }
    
}
