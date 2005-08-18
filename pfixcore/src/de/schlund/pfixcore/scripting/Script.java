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

/**
 *  
 * Created: Fri Aug 12 17:01:33 CEST 2005 @667 /Internet Time/
 * @author Benjamin Reitzammer <benjamin@schlund.de>
 */
public class Script {
    
    private String name     = null;  
    private String script   = null;  
    private String language = null;
    private String resource = null;
    private String source   = null;


    /**
     * 
     */
    public Script(String name, String language, String script, String resource, String source) {
        this.name     = name;
        this.language = language;
        this.script   = script;
        this.resource = resource;
        this.source   = source;
    }
    
    
    /** Returns the value of script.
     */
    public String getName() {
        return name;
    }    
    
    
    /** Returns the value of script.
     */
    public String getScript() {
        return script;
    }

          
    /** Returns the value of language.
     */
    public String getLanguage() {
        return language;
    }    
          
    
    /** Returns the value of language.
     */
    public String getSource() {
        return source;
    }    
          
    
    /** Returns the value of language.
     */
    public String getResource() {
        return resource;
    }
    
    
    /**
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(name);
        sb.append("\nScript: ").append(script).append(", Source: ").append(source);
        sb.append(", Resource: ").append(resource);
        return sb.toString();
    }

}
