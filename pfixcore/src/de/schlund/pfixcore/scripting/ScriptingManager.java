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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.PathFactory;

import org.apache.log4j.Logger;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingManager {

    private static Logger LOG = Logger.getLogger(ScriptingManager.class);

    private static ScriptingManager instance = new ScriptingManager();
    
    
    
    /**
     * 
     */
    private ScriptingManager() { }


    /**
     * 
     */    
    public static ScriptingManager getInstance() {
        return instance;
    }


    /**
     * 
     */    
    public void init(Properties props) throws Exception {
        
    }
    
    
    /**
     
     * @return never <code>null</code>
     */    
    public BSFManager newManager() throws BSFException {
        return new BSFManager();
    }    
    
    
    /**
     * Creates a new BSFManager and 
     * 
     * @param context not allowed to be <code>null</code>
     * @param script not allowed to be <code>null</code>
     * @return never <code>null</code>
     * @exception NullPointerException if any of the parameters is <code>null</code>
     */    
    public BSFEngine newEngine(String language) throws BSFException {
        return newManager().loadScriptingEngine(language);
    }    
    
    
    /**
     * Creates a new <code>BSFEngine</code> and executes the specified script 
     * with it.
     * <br/>
     * It can be used for preloading a Script. It effectively calls 
     * <code>BSFEngine.exec()</code> on the script code specified in the 
     * provided <code>Script</code> object, using {@link #getSource(Script) getSource()}
     * to get the executable Script code of the specified <code>Script</code>-object.
     * 
     * @param context the Context of this request
     * @param script the script to be executed, not allowed to be <code>null</code>
     * @return the instance of BSFEngine that was created for and used for the
     * execution of the specified script
     * @exception BSFException if the script execution throws an Exception
     * @exception IllegalArgumentException if the provided script parameter doesn't
     * specify any script code to execute.
     */    
    public BSFEngine executeScript(Script script) throws BSFException {
        try {
            BSFEngine engine = newEngine(script.getLanguage());
            engine.exec(script.getName(), 0, 0, getSource(script));
            return engine;
        } catch (IOException ex) {
            throw new BSFException(BSFException.REASON_IO_ERROR, ex.getMessage());
        }
    }
    
    
    /**
     * Get the source defined within the specified <code>Script</code> object,
     * whether it's defined as inlined String, as a file source, or as resource
     * on the classpath as <code>String</code>.
     * <br/>
     * The following order/precedence is used when determining which source to 
     * to return:
     * <ol>
     * <li>inlined script code</li>
     * <li>external script defined via <code>@src</code></li>
     * <li>external script defined via <code>@resource</code></li>
     * </ol>     
     * 
     * @return a <code>String</code>-object containing the source that's
     * represented by this <code>script</code>-object; never <code>null</code> 
     */
    public Object getSource(Script script) throws IOException {
        LOG.debug("Getting source for Script "+script.getName());
        
        // TODO implement caching
        
        String scriptSource = null;
      
        
        if ( isNotNullOrEmpty(script.getScript()) ) {
            // script contains the script code inlined
            scriptSource = script.getScript();
            LOG.debug("Script has inlined Source !!!");
            
        } else if ( isNotNullOrEmpty(script.getSource()) ) {
            
            // source specifies a path to a source file relative to docroot
            File sourceFile = PathFactory.getInstance().createPath(script.getSource()).resolve();
            LOG.debug("Getting from external source: "+sourceFile);
            scriptSource = copy(new FileInputStream(sourceFile));
            
        } else if ( isNotNullOrEmpty(script.getResource()) ) {
          
            // resource specifies a resource inside the classpath
            LOG.debug("Getting from resource on classpath: "+script.getResource());
            InputStream stream = getClass().getResourceAsStream(script.getResource());
            if ( stream != null )
                scriptSource = copy(stream);
            
        } 
        
        
        if ( scriptSource == null ) 
            throw new IllegalArgumentException("Parameter 'script' doesn't contain "+
                                               "any script code to execute! Script: "+script);
        else 
            return scriptSource;
    }
    
    
    /**
     * 
     */
    private String copy(InputStream in) throws IOException {
      
        ByteArrayOutputStream bai = new ByteArrayOutputStream();
    
        byte[] buf = new byte[512];
        int len;
        while( (len = in.read(buf)) != -1 )
            bai.write(buf, 0, len);        
        
        return bai.toString();
    }
    
    
    /**
     * 
     */
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !"".equals(str);
    }
    
}
