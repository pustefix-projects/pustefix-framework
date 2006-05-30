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

import de.schlund.pfixcore.util.StateUtil;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingState implements State {
  
    public  static final String                PROP_SCRIPT_PATH = "SCRIPTINGSTATE_SRC_PATH";
    private static final Logger                LOG              = Logger.getLogger(ScriptingState.class);
    private static final Map<String,BSFEngine> bsfenginemap     = new ConcurrentHashMap<String, BSFEngine>();
    
    /**
     * 
     */
    public boolean isAccessible(Context context, PfixServletRequest preq) throws Exception {
        BSFEngine bsfengine = init(context);
        return ScriptingUtil.exec(bsfengine, "isAccessible", new Object[] {context, preq});
    }
    
    
    /**
     * 
     */
    public boolean needsData(Context context, PfixServletRequest preq) throws Exception {
        BSFEngine bsfengine = init(context);
        return ScriptingUtil.exec(bsfengine, "needsData", new Object[] {context, preq});
    }
    
    
    /**
     * 
     */
    public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
        BSFEngine bsfengine = init(context);
        return (ResultDocument) bsfengine.call(null, "getDocument", new Object[]{context, preq});
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * @exception IllegalStateException if the properties for the current page
     * request don't contain a property, that denotes the 
     */
    protected BSFEngine init(Context context) throws BSFException, IOException {
      
        String    path      = getScriptPath(context);
        String    key       = path + "@" + context.getName();
        BSFEngine bsfengine = bsfenginemap.get(key);
        
        if (bsfengine == null || !ScriptingUtil.isCachedCurrent(path)) {
      
            LOG.debug("Initializing ScriptingState with script path: " + path);
            
            String lang = BSFManager.getLangFromFilename(path);
      
            BSFManager manager = new BSFManager();
            manager.declareBean("LOG", LOG, Logger.class);
            manager.exec(lang, path, 0, 0, ScriptingUtil.getScript(path));
            
            bsfengine = manager.loadScriptingEngine(lang);
        }
        bsfenginemap.put(key, bsfengine);
        return bsfengine;
    }
    
    
    /**
     * 
     */
    public String getScriptPath(Context context) {
        Properties props = context.getPropertiesForCurrentPageRequest();
        String     path  = props.getProperty(ScriptingState.PROP_SCRIPT_PATH);
        
        if (path == null) {
            String pr = context.getCurrentPageRequest().getName();
            throw new IllegalArgumentException("ScriptingState for " + pr + " has no script path specified");
        } else {
            return path;
        }
    }
    
    
}
