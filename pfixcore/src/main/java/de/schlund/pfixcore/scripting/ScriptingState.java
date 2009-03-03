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

import javax.servlet.http.HttpServletRequest;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;
import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.workflow.ConfigurableState;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

/**
 * This class creates a <code>BSFEngine</code>-instance for every request, to
 * make the use of instance properties, crossing request-boundaries, 
 * inside the script impossible.
 *
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingState implements ConfigurableState {
  
    private static final String   REQATTR_BSFENG   = "SCRIPTINGSTATE_REQUEST_BSFENGINE";
    private static final Logger   LOG              = Logger.getLogger(ScriptingState.class);
    private String scriptPath = null;
    private StateConfig config;
    
    
    /**
     * 
     */
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        return ScriptingUtil.exec( getEngine(context, preq), "isAccessible", new Object[] {context, preq} );
    }
    
    
    /**
     * 
     */
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        return ScriptingUtil.exec( getEngine(context, preq), "needsData", new Object[] {context, preq} );
    }
    
    
    /**
     * 
     */
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        return (ResultDocument) getEngine(context, preq).call( null, "getDocument", new Object[]{context, preq} );
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * @exception IllegalStateException if the properties for the current page
     * request don't contain a property, that denotes the 
     */
    protected BSFEngine getEngine(Context context, PfixServletRequest preq) throws BSFException, IOException {
      
        String    path      = scriptPath;
        BSFEngine bsfengine = null;
        
        HttpServletRequest req = preq.getRequest();
        
        bsfengine = (BSFEngine) req.getAttribute(REQATTR_BSFENG);
        
        // load the BSFEngine only once per request
        if ( bsfengine == null || !ScriptingUtil.isCachedCurrent(path) ) {
      
            LOG.debug("Initializing ScriptingState with script path: " + path);
            
            String lang = BSFManager.getLangFromFilename(path);
      
            BSFManager manager = new BSFManager();
            manager.declareBean("LOG", LOG, Logger.class);
            manager.declareBean("stateConfig", this.config, StateConfig.class);
            manager.exec(lang, path, 0, 0, ScriptingUtil.getScript(path));
            
            bsfengine = manager.loadScriptingEngine(lang);
            
            req.setAttribute(REQATTR_BSFENG, bsfengine);
        }
        
        return bsfengine;
    }
    
    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }


    public void setConfig(StateConfig config) {
        this.config = config;
    }
    
}
