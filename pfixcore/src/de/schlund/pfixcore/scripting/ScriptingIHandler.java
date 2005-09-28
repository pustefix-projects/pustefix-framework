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

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.PathFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Hashtable;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFEngine;
import org.apache.log4j.Logger;

/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingIHandler implements IHandler{
  
    private static Logger LOG = Logger.getLogger(ScriptingIHandler.class);
  
  
    /**
     * keyed by the path as String, if it's a resource (starts with File.pathSeparator)
     * value is a String, if it's a source (script stored in file), value is
     * an Object array, consisting of two elements, with the first being the lastMod
     * (as Long object) time of the file, when it was stored in the cache and the 
     * second the script as a String object.
     */
    private static final Map scriptCache = new Hashtable();
  
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
        return exec("prerequisitesMet", context);
    }
    
    
    /**
     */
    public boolean isActive(Context context) throws Exception {
        init();
        return exec("isActive", context);
    }
    
    
    /**
     */
    public boolean needsData(Context context) throws Exception {
        init();
        return exec("needsData", context);
    }
    
    
    /**
     * 
     */
    protected void init() throws BSFException, IOException {
      
        if ( !init ) {
            LOG.debug("Initializing ScriptingIHandler for path: "+path);
            
            String lang = BSFManager.getLangFromFilename(path);
      
            String script = path.startsWith("/") ? getResource(path) : getSource(path);
            
            BSFManager manager = new BSFManager();
            manager.declareBean("LOG", LOG, Logger.class);
            manager.exec(lang, path, 0, 0, script);
            
            bsfEngine = manager.loadScriptingEngine(lang);
            
            init = true;
        }
            
    }
    
    
    /**
     */
    protected String getSource(String path) throws IOException {
      
        String script = null;
        File file = PathFactory.getInstance().createPath(path).resolve();
        
        if ( scriptCache.containsKey(path) ) {
            Object[] cacheEntry = (Object[]) scriptCache.get(path);
            
            long lastMod = ((Long) cacheEntry[0]).longValue();
            if ( file.lastModified() <= lastMod ) {
                LOG.debug("Returning Script from cache for path '"+path+"', lastMod: "+lastMod);
                script = (String) cacheEntry[1];
            }
        } 
        
        if ( script == null ) {
            LOG.debug("Fetching Script for path '"+path+"' from file");
            script = copy(new FileInputStream(file));
            scriptCache.put(path, new Object[] {new Long(file.lastModified()), script});
        }
        
        return script;
    }    
        
        
    /**
     * @return never null
     */
    protected String getResource(String path) throws IOException {
      
        String script = null;
        
        if ( scriptCache.containsKey(path) ) {
          
            LOG.debug("Returning Script from cache for path '"+path);
            script = (String) scriptCache.get(path); 
            
        } else {
          
            LOG.debug("Fetching Script from classpath for path '"+path);
          
            InputStream stream = path.getClass().getResourceAsStream(path);
            if ( stream == null )
                throw new IOException("No Resource found for '"+path+"'");
              
            script = copy(stream);
            scriptCache.put(path, script);
        }
        
        return script;
    }
    
    
    // ============ private Helper methods ============
    
    
    /**
     * 
     */
    private boolean exec(String methodName, Context context) throws Exception {
        Boolean bool = null;
        try {
            bool = (Boolean) bsfEngine.call(null, methodName, new Object[] {context});
        } catch (ClassCastException ex) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                   scriptName(path)+".methodName() returned no boolean value! "+ex.getMessage());
        } 
        
        if ( bool == null )
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                   scriptName(path)+"."+methodName+"() returned null Boolean value!");
        
        return bool.booleanValue();
    }
    
     
    /**
     */
    private String copy(InputStream input) throws IOException {
        
        InputStreamReader reader = new InputStreamReader(input);
        StringWriter output = new StringWriter();
        
        char[] buffer = new char[4096];
        int n = 0;
        while ( -1 != (n = reader.read(buffer)) ) {
            output.write(buffer, 0, n);
        }
        
        return output.toString();
    }
    
    
    /**
     */
    private String scriptName(String path) {
        int indexDot = path.lastIndexOf(".");
        int indexBegin = path.lastIndexOf("/") != -1 ? path.lastIndexOf("/") : "script:".length();
        
        return path.substring(indexBegin, indexDot); 
    }
    
}
