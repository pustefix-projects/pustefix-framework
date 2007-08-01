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

import java.util.*;
import de.schlund.util.statuscodes.*;
import org.apache.log4j.*;

/**
 * IWrapperIndexedParam.java
 *
 *
 * Created: Mon Aug 20 18:45:30 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IWrapperIndexedParam implements IWrapperParamDefinition, Comparable {

    private static final String TYPE_INDEXED  = "indexed";
    private static final String TYPE_MULTIPLE = "multiple";
    private static final String TYPE_SINGLE   = "single";
    private String              name;
    private String              type;
    private boolean             trim;
    private boolean             multiple;
    private IWrapperParamCaster caster;
    private ArrayList           precheck      = new ArrayList();
    private ArrayList           postcheck     = new ArrayList();
    private HashMap             params        = new HashMap();
    private HashMap             errors        = new HashMap();
    private String              prefix;
    private Logger              LOG           = Logger.getLogger(this.getClass());
    
    public IWrapperIndexedParam(String name, boolean multiple, String type, boolean trim) {
        this.type     = type;
        this.name     = name;
        this.caster   = null;
        this.multiple = multiple;
        this.trim       = trim;
    }
    
    @Deprecated
    public IWrapperIndexedParam(String name, boolean multiple, String type) {
        this(name,multiple,type,true);
    }

    public String getOccurance() {
        return TYPE_INDEXED;
    }

    public String getFrequency() {
        return multiple ? TYPE_MULTIPLE : TYPE_SINGLE;
    }
    
    public IWrapperParamCaster getCaster() {
        return caster;
    }

    public IWrapperParamPreCheck[] getPreChecks() {
        return (IWrapperParamPreCheck[]) precheck.toArray(new IWrapperParamPreCheck[]{});
    }

    public IWrapperParamPostCheck[] getPostChecks() {
        return (IWrapperParamPostCheck[]) postcheck.toArray(new IWrapperParamPostCheck[]{});
    }

    protected void initValueFromRequest(String prefix, RequestData req) {
        String wholename = prefix + "." + name;
        for (Iterator i = req.getParameterNames(); i.hasNext(); ) {
            String pname = (String) i.next();
            if (pname.startsWith(wholename + ".")) {
                // Now initialize the IWrapperParams
                String idx = pname.substring(wholename.length() + 1);
                LOG.debug("~~~ Found index: " + idx + " for IndexedParam " + name);
                IWrapperParam pinfo = new IWrapperParam(name + "." + idx, multiple, true, null, type, trim);
                pinfo.setParamCaster(caster);
                synchronized (params) {
                    params.put(pinfo.getName(), pinfo);
                }
                for (Iterator j = precheck.iterator(); j.hasNext(); ) {
                    pinfo.addPreChecker((IWrapperParamPreCheck) j.next());
                }
                for (Iterator j = postcheck.iterator(); j.hasNext(); ) {
                    pinfo.addPostChecker((IWrapperParamPostCheck) j.next());
                }
                pinfo.initValueFromRequest(prefix, req);
                if (pinfo.errorHappened()) {
                    LOG.debug("*** ERROR happened for Param: " + pinfo.getName());
                    synchronized (errors) {
                        errors.put(pinfo.getName(), pinfo);
                    }
                }
            }
        }
    }
    
    public String getType() { return type; }

    public String getName() { return name; }

    public void setParamCaster (IWrapperParamCaster caster) {
        this.caster = caster;
    }
    
    public void addPreChecker(IWrapperParamPreCheck check) {
        precheck.add(check);
    }
    
    public void addPostChecker(IWrapperParamPostCheck check) {
        postcheck.add(check);
    }

    public boolean errorHappened() {
        return !errors.isEmpty();
    }

    public IWrapperParam[] getAllParamsWithErrors() {
        synchronized (errors) {
            return (IWrapperParam[]) errors.values().toArray(new IWrapperParam[] {});
        }
    }

    public IWrapperParam[] getAllParams() {
        synchronized (params) {
            return (IWrapperParam[]) params.values().toArray(new IWrapperParam[] {});
        }
    }

    public IWrapperParam getParamForIndex(String idx) {
        String key = name + "." + idx;
        synchronized (params) {
            IWrapperParam pinfo = (IWrapperParam) params.get(key);
            if (pinfo == null) {
                pinfo = new IWrapperParam(key, multiple, true, null, type, trim);
                params.put(pinfo.getName(), pinfo);
            }
            return pinfo;
        }
    }
    
    
    public void addSCode(StatusCode scode, String[] args, String level, String idx) {
        IWrapperParam pinfo = getParamForIndex(idx);
        pinfo.addSCode(scode, args, level);
        synchronized (errors) {
            errors.put(pinfo.getName(), pinfo);
        }
    }
    
    public String[] getKeys() {
        String[] keys;
        synchronized (params) {
            keys = new String[params.size()];
            int j = 0;
            for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
                keys[j] = ((String) i.next()).substring(getName().length() + 1);
                j++;
            }
        }
        return keys;
    }
    
    public int compareTo(Object inobj) {
        IWrapperParamDefinition in = (IWrapperParamDefinition) inobj;
        return name.compareTo(in.getName());
    }
}// IWrapperIndexedParam
