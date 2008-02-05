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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;


/**
 * 
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class ScriptingUtil {
    
    private final static Logger LOG = Logger.getLogger(ScriptingUtil.class);
    
  
    /**
     * keyed by the path as String, if it's a resource (starts with '/')
     * value is a String, if it's a source (script stored in file), value is
     * an Object array, consisting of two elements, with the first being the lastMod
     * (as Long object) time of the file, when it was stored in the cache and the 
     * second the script as a String object.
     */
    private static final Map scriptCache = new Hashtable();
      
    
    /**
     * @return never null
     */
    public static boolean isCachedCurrent(String path) throws IOException {
        return path.startsWith("/") ? isCachedResourceCurrent(path)
                                    : isCachedFileCurrent(path) ;
    }
    
    
    /**
     * @return never null
     */
    public static String getScript(String path) throws IOException {
        return path.startsWith("/") ? getClasspathResource(path) : getFileSource(path);
    }

    
    /**
     * 
     */
    protected static boolean exec(BSFEngine engine, String methodName, Object[] args) throws Exception {
        Boolean bool = (Boolean) engine.call(null, methodName, args);
        
        if ( bool == null )
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                   methodName+"() returned null Boolean value!");
        
        return bool.booleanValue();
    }    
    
    
    /**
     */
    protected static String scriptName(String path) {
        int indexBegin = path.lastIndexOf("/") != -1 ? path.lastIndexOf("/") : "script:".length();
        return path.substring(indexBegin, path.lastIndexOf(".")); 
    }

    
    // ============ private Helper methods ============
    
    

    /**
     * @return never null
     */
    private static String getClasspathResource(String path) throws IOException {
      
        String script = null;
        
        if ( isCachedResourceCurrent(path) ) {
          
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
    
    
    /**
     */
    private static String getFileSource(String relativePath) throws IOException {
        String script = null;
        
        if ( isCachedFileCurrent(relativePath) ) {
          
            LOG.debug("Returning Script from cache for path '"+relativePath);
            script = (String) ((Object[]) scriptCache.get(relativePath))[1];
            
        } else {
          
            LOG.debug("Fetching Script for path '"+relativePath+"' from file");
            
            FileResource file = ResourceUtil.getFileResourceFromDocroot(relativePath);
            script = copy(file.getInputStream());
            
            scriptCache.put(relativePath, new Object[] {new Long(file.lastModified()), script});
            
        }
        
        return script;
    }      
    
    
    
    /**
     * 
     */
    private static boolean isCachedFileCurrent(String path) {
        boolean isCurrent = false;
      
        if ( scriptCache.containsKey(path) ) {
            
            Object[] cacheEntry = (Object[]) scriptCache.get(path);
            long lastMod = ((Long) cacheEntry[0]).longValue();
            
            FileResource file = ResourceUtil.getFileResourceFromDocroot(path);
            if ( file.lastModified() <= lastMod ) 
                isCurrent = true;
        } 
        
        return isCurrent;
    }
    
     
    /**
     */
    private static boolean isCachedResourceCurrent(String path) {
        return scriptCache.containsKey(path);
    }
    
     
    /**
     */
    private static String copy(InputStream input) throws IOException {
        
        InputStreamReader reader = new InputStreamReader(input);
        StringWriter output = new StringWriter();
        
        char[] buffer = new char[4096];
        int n = 0;
        while ( -1 != (n = reader.read(buffer)) ) {
            output.write(buffer, 0, n);
        }
        
        return output.toString();
    }    
}
