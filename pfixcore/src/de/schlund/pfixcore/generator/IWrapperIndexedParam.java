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
import javax.servlet.http.*;
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

public class IWrapperIndexedParam {
    private String              name;
    private IWrapperParamCaster caster;
    private ArrayList           precheck  = new ArrayList();
    private ArrayList           postcheck = new ArrayList();
    private HashMap             params = new HashMap();
    private HashMap             errors = new HashMap();
    private String              prefix;
    private Category            CAT    = Category.getInstance(this.getClass().getName());
    
    public IWrapperIndexedParam(String name) {
        this.name     = name;
        this.caster   = null;
    }

    protected void initValueFromRequest(String prefix, RequestData req) {
        String wholename = prefix + "." + name;
        for (Iterator i = req.getParameterNames(); i.hasNext(); ) {
            String pname = (String) i.next();
            if (pname.startsWith(wholename)) {
                // Now initialize the IWrapperParamInfos
                String idx = pname.substring(wholename.length() + 1);
                CAT.debug("~~~ Found index: " + idx + " for IndexedParam " + name);
                IWrapperParamInfo pinfo = new IWrapperParamInfo(name + "." + idx, true, null);
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
                    CAT.debug("*** ERROR happened for ParamInfo: " + pinfo.getName());
                    synchronized (errors) {
                        errors.put(pinfo.getName(), pinfo);
                    }
                }
            }
        }
    }
    
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

    public IWrapperParamInfo[] getAllParamInfosWithErrors() {
        synchronized (errors) {
            return (IWrapperParamInfo[]) errors.values().toArray(new IWrapperParamInfo[] {});
        }
    }

    public IWrapperParamInfo[] getAllParamInfos() {
        synchronized (params) {
            return (IWrapperParamInfo[]) params.values().toArray(new IWrapperParamInfo[] {});
        }
    }

    public IWrapperParamInfo getParamInfoForIndex(String idx) {
        String key = name + "." + idx;
        synchronized (params) {
            IWrapperParamInfo pinfo = (IWrapperParamInfo) params.get(key);
            if (pinfo == null) {
                pinfo = new IWrapperParamInfo(key, true, null);
                params.put(pinfo.getName(), pinfo);
            }
            return pinfo;
        }
    }
    
    public void addSCode(StatusCode scode, String idx) {
        IWrapperParamInfo pinfo = getParamInfoForIndex(idx);
        pinfo.addSCode(scode);
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
    
}// IWrapperIndexedParam
