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

package de.schlund.pfixxml.targets;
import java.util.*;
import org.apache.log4j.*;

/**
 * TargetImpl.java
 *
 *
 * Created: Mon Jul 23 16:24:53 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public abstract class TargetImpl implements TargetRW, Comparable {
    // set in from constructor 
    protected TargetType           type;
    protected TargetGenerator      generator;
    protected String               targetkey;

    // only needed to init in constructor for virtual targets
    protected AuxDependencyManager auxdepmanager = null;
    protected TreeMap              params        = null;
    
    protected Target               xmlsource     = null;
    protected Target               xslsource     = null;

    protected Category CAT  = Category.getInstance(this.getClass().getName());
    protected Category TREE = Category.getInstance(this.getClass().getName() + ".TREE");

    // Target interface
    
    public TargetType getType() {
        return type;
    }
    
    public String getTargetKey() {
        return targetkey;
    }
    
    public AuxDependencyManager getAuxDependencyManager() {
        return auxdepmanager;
    }

    public TargetGenerator getTargetGenerator() {
        return generator;
    }

    public Target getXMLSource() {
        return xmlsource;
    }

    public Target getXSLSource() {
        return xslsource;
    }

    public TreeMap getParams() {
        if (params == null) {
            return null;
        } else {
            synchronized(params) {
                return new TreeMap(params);
            }
        }
    }

    public Object getValue() throws Exception {
        getModTimeMaybeUpdate();
        return getCurrValue();
    }

    public abstract void    addPageInfo(PageInfo info);
    public abstract TreeSet getPageInfos();
    public abstract void    setXMLSource(Target source);
    public abstract void    setXSLSource(Target source);
    public abstract void    addParam(String key, String val);
    public abstract void    storeValue(Object obj);
    
    public abstract boolean needsUpdate() throws Exception;
    public abstract long    getModTime();
    public abstract String  toString();

    public Object getCurrValue() throws Exception {
        Object obj = getValueFromSPCache(); 
        if (obj == null) {
            synchronized (this) {
                obj = getValueFromSPCache();
                if (obj == null) {
                    obj = getValueFromDiscCache();
                    // Caution! setCacheValue is not guaranteed to store anything at all, so it's NOT
                    // guaranteed that the sequence
                    //           storeValue(tmp); tmp2 = getValueFromSPCache; return tmp2
                    // will return a tmp2 == tmp.
                    // Example: A NullCache will silently ignore all store requests, so a call to this method
                    //          will always trigger getValueFromDiscCache().
                    storeValue(obj);
                }
            }
        }
        return obj;
    }

    // comparable interface
    
    public int compareTo(Object inobj) {
        Target in = (Target) inobj;
        if (getTargetGenerator().getConfigname().compareTo(in.getTargetGenerator().getConfigname()) != 0) {  
            return getTargetGenerator().getConfigname().compareTo(in.getTargetGenerator().getConfigname());
        } else {
            return getTargetKey().compareTo(in.getTargetKey());
        }
    }

    //
    // implementation
    //
    
    protected abstract Object getValueFromSPCache();
    protected abstract Object getValueFromDiscCache() throws Exception;
    protected abstract long   getModTimeMaybeUpdate() throws Exception;
    protected abstract void   setModTime(long mtime);

    
}// TargetImpl
