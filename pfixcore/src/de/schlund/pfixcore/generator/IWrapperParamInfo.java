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

import de.schlund.pfixxml.*;
import de.schlund.util.statuscodes.*;
import java.io.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * Describe class <code>IWrapperParamInfo</code> here.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IWrapperParamInfo implements IWrapperParamCheck, Comparable {
    private String              name;
    private boolean             optional;
    private String[]            stringval  = null;
    private Object[]            value      = null;
    private RequestParam[]      defaultval = null;
    private IWrapperParamCaster caster;
    private ArrayList           precheck   = new ArrayList();
    private ArrayList           postcheck  = new ArrayList();
    private HashSet             scodes     = new HashSet();
    private HashMap             scodesargs = new HashMap();
    private Category            CAT        = Category.getInstance(this.getClass().getName());
    private StatusCode          missing    = StatusCodeFactory.getInstance().getStatusCode("pfixcore.generator.MISSING_PARAM");
    
    public IWrapperParamInfo(String name, boolean optional, RequestParam[] defaultval) {
        this.name       = name;
        this.optional   = optional;
        this.caster     = null;
        this.defaultval = defaultval;
    }

    public void setCustomSCode(String scode) {
        missing = StatusCodeFactory.getInstance().getStatusCode(scode);
    }

    public void setParamCaster(IWrapperParamCaster caster) {
        this.caster = caster;
    }

    public void addPreChecker(IWrapperParamPreCheck check) {
        precheck.add(check);
    }
    
    public void addPostChecker(IWrapperParamPostCheck check) {
        postcheck.add(check);
    }

    public boolean errorHappened() {
        return !scodes.isEmpty();
    }

    public String[] getArgsForStatusCode(StatusCode scode) {
        return (String[]) scodesargs.get(scode);
    }
    
    public StatusCode[] getStatusCodes() {
        return (StatusCode[]) scodes.toArray(new StatusCode[] {}); 
    }

    public void addSCode(StatusCode scode) {
        addSCode(scode, null);
    }
    
    public void addSCode(StatusCode scode, String[] args) {
        synchronized (scodes) {
            scodes.add(scode);
        }
        if (args != null) {
            synchronized (scodesargs) {
                scodesargs.put(scode, args);
            }
        }
    }
    
    public String   getName() { return name; }
    
    public Object   getValue() {
        if (value != null && value.length > 0) {
            return value[0];
        } else {
            return null;
        }
    }

    public Object[] getValueArr() { return value; }

    public String[] getStringValue() { return stringval; }

    public void setStringValue(String[] v) { stringval = v; }

    public void setSimpleObjectValue(Object[] values) {
        if (values != null) {
            stringval = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                stringval[i] = values[i].toString();
            }
        } else {
            stringval = null;
        }
    }
    
    private void setStringValue(RequestParam[] values) {
        if (values != null) {
            stringval = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                stringval[i] = values[i].getValue();
            }

        } else {
            stringval = null;
        }
    }

    public void initValueFromRequest(String prefix, RequestData reqdata) {
        String            thename = prefix + "." + name;
        RequestParam[]    rparamv = reqdata.getParameters(thename);
        
        CAT.debug(">>> [" + thename + "] Optional: " + optional);
        
        if (rparamv != null) {
            ArrayList in  = new ArrayList(Arrays.asList(rparamv));
            ArrayList out = new ArrayList();
            for (Iterator i = in.iterator(); i.hasNext(); ) {
                RequestParam val = (RequestParam) i.next();
                CAT.debug(">>> [" + thename + "] Input: >" + val + "<");
                if (val.getType().equals(RequestParamType.SIMPLE) || val.getType().equals(RequestParamType.FIELDDATA)) {
                    String tmp = val.getValue().trim();
                    if (!tmp.equals("")) {
                        val.setValue(tmp);
                        out.add(val);
                    }
                } else if (val.getType().equals(RequestParamType.FILEDATA)) {
                    String fname = val.getValue();
                    if (fname != null) {
                        File tmpfile = new File(fname);
                        if (tmpfile.exists() && tmpfile.canRead() && tmpfile.isFile()) {
                            out.add(val);
                        }
                    }
                } else {
                    CAT.error("RequestParam " + thename + " is of unknown type: " + val.getType());
                }
            }
            if (out.size() > 0) {
                rparamv = (RequestParam[]) out.toArray(new RequestParam[] {});
            } else {
                CAT.debug(">>> [" + thename + "] Outlist empty, setting InputArray to null!!!");
                rparamv = null;
            }
        } else {
            CAT.debug(">>> [" + thename + "] InputArray is null!!!");
        }
        
        setStringValue(rparamv);

        // Default values are _not_ echoed back to the UI, so we set them only AFTER
        // we set the normalized string values.
        if (rparamv == null && defaultval != null) {
            CAT.debug(">>> Param '" + name + "' is empty, but using supplied default value.");
            rparamv = defaultval;
        }

        if (rparamv == null && !optional) {
            synchronized (scodes) {
                scodes.add(missing);
            }
        } else if (rparamv != null) {
            for (Iterator i = precheck.iterator(); i.hasNext(); ) {
                IWrapperParamPreCheck pre = (IWrapperParamPreCheck) i.next();
                pre.check(rparamv);
                if (pre.errorHappened()) {
                    synchronized (scodes) {
                        scodes.addAll(Arrays.asList(pre.getStatusCodes()));
                    }
                }
            }
            if (!errorHappened()) {
                // Now we try to cast the RequestParam according to it's classname
                if (caster != null) {
                    caster.castValue(rparamv);
                }
                if (caster != null && caster.errorHappened()) {
                    synchronized (scodes) {
                        scodes.addAll(Arrays.asList(caster.getStatusCodes()));
                    }
                } else {
                    Object[] tmp;
                    if (caster != null) {
                        tmp = caster.getValue();
                    } else {
                        // Build up an Array with the String values of the rparamv
                        tmp = new String[rparamv.length];
                        for (int i = 0; i < rparamv.length; i++) {
                            tmp[i] = rparamv[i].getValue();
                        }
                    }
                    for (Iterator i = postcheck.iterator(); i.hasNext(); ) {
                        IWrapperParamPostCheck post = (IWrapperParamPostCheck) i.next();
                        post.check(tmp);
                        if (post.errorHappened()) {
                            synchronized (scodes) {
                                scodes.addAll(Arrays.asList(post.getStatusCodes()));
                            }
                        }
                    }
                    if (!errorHappened()) {
                        value = tmp;
                    }
                }
            }
        } else {
            value = null;
        }
    }

    public int compareTo(Object inobj) {
        IWrapperParamInfo in = (IWrapperParamInfo) inobj;
        return name.compareTo(in.getName());
    }
}
