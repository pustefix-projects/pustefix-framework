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
 * Describe class <code>IWrapperParam</code> here.
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class IWrapperParam implements IWrapperParamCheck, IWrapperParamDefinition, Comparable {

    private static final String TYPE_OPTIONAL  = "optional";
    private static final String TYPE_MANDATORY = "mandatory";
    private static final String TYPE_MULTIPLE  = "multiple";
    private static final String TYPE_SINGLE    = "single";
    private String              name;
    private String              type;
    private boolean             trim;
    private boolean             optional;
    private boolean             multiple;
    private String[]            stringval      = null;
    private Object[]            value          = null;
    private RequestParam[]      defaultval     = null;
    private IWrapperParamCaster caster;
    private ArrayList           precheck       = new ArrayList();
    private ArrayList           postcheck      = new ArrayList();
    private HashSet             scodeinfos     = new HashSet();
    private Logger              LOG            = Logger.getLogger(this.getClass());
    private StatusCodeInfo      missing        = new StatusCodeInfo(StatusCodeLib.PFIXCORE_GENERATOR_MISSING_PARAM, null, null);  
    private boolean             inrequest      = false;
    
    public IWrapperParam(String name, boolean multiple, boolean optional, RequestParam[] defaultval, String type, boolean trim) {
        this.type       = type;
        this.name       = name;
        this.optional   = optional;
        this.multiple   = multiple;
        this.caster     = null;
        this.defaultval = defaultval;
        this.trim = trim;
    }
    
    @Deprecated
    public IWrapperParam(String name, boolean multiple, boolean optional, RequestParam[] defaultval, String type) {
        this(name,multiple,optional,defaultval,type,true);
    }
    

    public void setValue(Object[] value) {
        this.value = value;
    }
    
    public String getOccurance() {
        return optional ? TYPE_OPTIONAL : TYPE_MANDATORY;
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

    public void setCustomSCode(String scode) {
        missing = new StatusCodeInfo(StatusCodeLib.getStatusCodeByName(scode), null, null);
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

    public boolean suppliedInRequest() {
        return inrequest;
    }
    
    public boolean errorHappened() {
        return !scodeinfos.isEmpty();
    }

    public StatusCodeInfo[] getStatusCodeInfos() {
        return (StatusCodeInfo[]) scodeinfos.toArray(new StatusCodeInfo[] {});
    }
    
    public void addSCode(StatusCode scode, String[] args, String level) {
        StatusCodeInfo scinfo = new StatusCodeInfo(scode, args, level);
        scodeinfos.add(scinfo);
    }
    
    public String getType() { return type; }

    public String getName() { return name; }
    
    public Object getValue() {
        if (value != null && value.length > 0) {
            return value[0];
        } else {
            return null;
        }
    }

    public Object[] getValueArr() { return value; }

    public String[] getStringValue() { return stringval; }

    public void setStringValue(Object[] values) {
        if (values != null) {
            stringval = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                if (values[i] == null) {
                    stringval[i] = null;
                } else if (values[i] instanceof RequestParam) {
                    stringval[i] = ((RequestParam) values[i]).getValue();
                } else {
                    stringval[i] = values[i].toString();
                }
            }
        } else {
            stringval = null;
        }
    }

    public void initValueFromRequest(String prefix, RequestData reqdata) {
        String            thename = prefix + "." + name;
        RequestParam[]    rparamv = reqdata.getParameters(thename);
        
        LOG.debug(">>> [" + thename + "] Optional: " + optional);
        
        if (rparamv != null) {
            inrequest = true;
            ArrayList in  = new ArrayList(Arrays.asList(rparamv));
            ArrayList out = new ArrayList();
            for (Iterator i = in.iterator(); i.hasNext(); ) {
                RequestParam val = (RequestParam) i.next();
                LOG.debug(">>> [" + thename + "] Input: >" + val + "<");
                if (val.getType().equals(RequestParamType.SIMPLE) || val.getType().equals(RequestParamType.FIELDDATA)) {
                    String tmp = val.getValue().trim();
                    if (!tmp.equals("")) {
                        if(trim) val.setValue(tmp);
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
                    LOG.error("RequestParam " + thename + " is of unknown type: " + val.getType());
                }
            }
            if (out.size() > 0) {
                rparamv = (RequestParam[]) out.toArray(new RequestParam[] {});
            } else {
                LOG.debug(">>> [" + thename + "] Outlist empty, setting InputArray to null!!!");
                rparamv = null;
            }
        } else {
            inrequest = false;
            LOG.debug(">>> [" + thename + "] InputArray is null!!!");
        }
        
        setStringValue(rparamv);

        // Default values are _not_ echoed back to the UI, so we set them only AFTER
        // we set the normalized string values.
        if (rparamv == null && defaultval != null) {
            LOG.debug(">>> Param '" + name + "' is empty, but using supplied default value.");
            rparamv = defaultval;
        }

        if (rparamv == null && !optional) {
            synchronized (scodeinfos) {
                scodeinfos.add(missing);
            }
        } else if (rparamv != null) {
            for (Iterator i = precheck.iterator(); i.hasNext(); ) {
                IWrapperParamPreCheck pre = (IWrapperParamPreCheck) i.next();
                pre.check(rparamv);
                if (pre.errorHappened()) {
                    synchronized (scodeinfos) {
                        scodeinfos.addAll(Arrays.asList(pre.getStatusCodeInfos()));
                    }
                }
            }
            if (!errorHappened()) {
                // Now we try to cast the RequestParam according to it's classname
                if (caster != null) {
                    caster.castValue(rparamv);
                }
                if (caster != null && caster.errorHappened()) {
                    synchronized (scodeinfos) {
                        scodeinfos.addAll(Arrays.asList(caster.getStatusCodeInfos()));
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
                            synchronized (scodeinfos) {
                                scodeinfos.addAll(Arrays.asList(post.getStatusCodeInfos()));
                            }
                        }
                    }
                    if (!errorHappened()) {
                        setValue(tmp);
                    }
                }
            }
        } else {
            setValue(null);
        }
    }

    public int compareTo(Object inobj) {
        IWrapperParamDefinition in = (IWrapperParamDefinition) inobj;
        return name.compareTo(in.getName());
    }
}
