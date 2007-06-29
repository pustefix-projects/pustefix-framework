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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

/**
 * IWrapperImpl.java
 *
 *
 * Created: Wed Aug  8 14:19:39 2001
 *
 * @author <a href="mailto: "Jens Lautenbacher</a>
 *
 *
 */

public abstract class IWrapperImpl implements IWrapper {
    protected RequestData req;
    protected String      prefix   = "__undef";
    protected Integer     order    = new Integer(0);
    private   Logger      LOG      = Logger.getLogger(this.getClass());
    private   FileResource logdir  = null; 
    private   String      pagename = null;
    private   String      visitid  = null;
    
    protected HashMap<String, IWrapperParam>     params   = null; // single static parameters (of the form PREFIX.NAME)
    protected HashMap<String, IWrapperParam>     errors   = null; // errors on single parameters
    protected HashMap<String, IWrapperIndexedParam>     idxprms  = null; // array like indexed parameters (of the form PREFIX.NAME.INDEX)
    protected IHandler    handler  = null; // Make sure that you set the handler in the
                                           // constructor of a derived class
    
    public void initLogging(FileResource logdir, String pagename, String visitid) {
        LOG.debug("*** Logging input for " + prefix + " into " + logdir + " " + pagename + " " + visitid + " ***");
        this.logdir   = logdir;
        this.pagename = pagename;
        this.visitid  = visitid;
    }

    public void tryErrorLogging() throws IOException {
        if (logdir != null && pagename != null && visitid != null) {
            FileResource    log    = ResourceUtil.getFileResource(logdir, pagename + "#" + prefix);
            Writer          out    = new OutputStreamWriter(new BufferedOutputStream(log.getOutputStream()));
            IWrapperParam[] tmperrors = gimmeAllParamsWithErrors();
            if (tmperrors != null && tmperrors.length > 0) {
                StringBuffer buff = getLogBuffer("ERRORS");
                for (int j = 0; j < tmperrors.length; j++) {
                    IWrapperParam    param  = tmperrors[j];
                    StatusCodeInfo[] scodes = param.getStatusCodeInfos();
                    if (scodes != null) {
                        appendErrorLog(param, buff);
                    }
                }
                out.write(buff.toString() + "\n");
                out.flush();
            }
        }
    }

    public void tryParamLogging() throws IOException {
        if (logdir != null && pagename != null && visitid != null) {
            File         log  = new File(logdir + "/" + pagename + "#" + prefix);
            Writer       out  = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(log, true)));
            StringBuffer buff = getLogBuffer("VALUES");
            for (Iterator iter = params.values().iterator(); iter.hasNext(); ) {
                appendParamLog((IWrapperParam) iter.next(), buff);
            }
            for (Iterator iter = idxprms.values().iterator(); iter.hasNext();) {
                IWrapperIndexedParam pindex  = (IWrapperIndexedParam) iter.next();
                IWrapperParam[] pinfoarr = pindex.getAllParams();
                for (int i = 0; i < pinfoarr.length; i++) {
                    appendParamLog((IWrapperParam) iter.next(), buff);
                }
            }
            out.write(buff.toString() + "\n");
            out.flush();
        }
    }

    public final void init(String prefix) throws Exception {
        params  = new HashMap<String, IWrapperParam>();
        errors  = new HashMap<String, IWrapperParam>();
        idxprms = new HashMap<String, IWrapperIndexedParam>();
        this.prefix = prefix;
        registerParams();
    }

    public final void load(RequestData req) throws Exception {
        this.req = req;

        for (Iterator i = params.values().iterator(); i.hasNext();) {
            IWrapperParam pinfo = (IWrapperParam) i.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug("===> Doing init for Param: " + pinfo.getName());
            }
            pinfo.initValueFromRequest(prefix, req);
            if (pinfo.errorHappened()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("*** ERROR happened for Param: " + pinfo.getName());
                }
                synchronized (errors) {
                    errors.put(pinfo.getName(), pinfo);
                }
            }
        }
        for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
            IWrapperIndexedParam pindex = (IWrapperIndexedParam) i.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug("===> Doing init for IndexedParam: " + pindex.getName());
            }
            pindex.initValueFromRequest(prefix, req);
            // error handling happens inside the IWRapperIndexedParam...
        }
    }

    
    public final IHandler gimmeIHandler() {
        if (handler == null) {
            throw new RuntimeException(
                "ERROR: You need to define a IHandler for IWrapper "
                    + this.getClass().getName());
        }
        return handler;
    }

    public final String gimmePrefix() {
        return prefix;
    }

    public final void defineOrder(int order) {
        this.order = new Integer(order);
    }

    public final Integer gimmeOrder() {
        return order;
    }
    
    public final boolean errorHappened() {
        boolean noerr = errors.isEmpty();
        if (noerr) {
            synchronized (idxprms) {
                for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
                    IWrapperIndexedParam pindx = (IWrapperIndexedParam) i.next();
                    if (pindx.errorHappened()) {
                        noerr = false;
                        break;
                    }
                }
            }
        }
        return !noerr;
    }

    public final IWrapperParamDefinition[] gimmeAllParamDefinitions() {
        TreeSet retpar = new TreeSet();
        synchronized (params) {
            retpar.addAll(params.values());
        }
        synchronized (idxprms) {
            retpar.addAll(idxprms.values());
        }
        return (IWrapperParamDefinition[]) retpar.toArray(new IWrapperParamDefinition[] {});
    }
    
    
    public final IWrapperParam[] gimmeAllParams() {
        TreeSet retpar = new TreeSet();
        synchronized (params) {
            retpar.addAll(params.values());
        }
        synchronized (idxprms) {
            for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
                IWrapperIndexedParam pindex = (IWrapperIndexedParam) i.next();
                retpar.addAll(Arrays.asList(pindex.getAllParams()));
            }
        }
        return (IWrapperParam[]) retpar.toArray(new IWrapperParam[] {});
    }

    public final IWrapperParam[] gimmeAllParamsWithErrors() {
        TreeSet retpar = new TreeSet();
        synchronized (errors) {
            retpar.addAll(errors.values());
        }
        synchronized (idxprms) {
            for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
                IWrapperIndexedParam pindex = (IWrapperIndexedParam) i.next();
                retpar.addAll(Arrays.asList(pindex.getAllParamsWithErrors()));
            }
        }
        return (IWrapperParam[]) retpar.toArray(new IWrapperParam[] {});
    }

    public void addSCode(IWrapperParam param, de.schlund.util.statuscodes.StatusCode scode, String args[], String level) {
        param.addSCode(scode, args, level);
        synchronized (errors) {
            errors.put(param.getName(), param);
        }
    }

    protected final IWrapperParam gimmeParamForKey(String key) {
        synchronized (params) {
            return (IWrapperParam) params.get(key);
        }
    }

    protected final IWrapperIndexedParam gimmeIndexedParamForKey(String key) {
        synchronized (idxprms) {
            return (IWrapperIndexedParam) idxprms.get(key);
        }
    }

    protected void registerParams() {
        // DO NOTHING.
        // This could be abstract, but descendents should be able to call
        // super.registerParams() without worrying.
    }

    public final int compareTo(Object inobj) {
        IWrapper in = (IWrapper) inobj;
        return (gimmeOrder().compareTo(in.gimmeOrder()));
    }

    private StringBuffer getLogBuffer(String init) {
        StringBuffer buff = new StringBuffer(256);
        long         now  = System.currentTimeMillis();
        DateFormat   fmt  = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        buff.append(fmt.format(new Date(now)) + "|" + visitid + "|" + init);
        return buff;
    }

    
    private void appendParamLog(IWrapperParam pinfo, StringBuffer buff) {
        String   name  = pinfo.getName();
        String[] value = pinfo.getStringValue();
        buff.append("|" + name + "=");
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                buff.append(value[i]);
                if (i < (value.length - 1)) {
                    buff.append("&");
                }
            }
        } else {
            buff.append("NULL");
        }
    }
    
    private void appendErrorLog(IWrapperParam pinfo, StringBuffer buff) {
        String           name   = pinfo.getName(); 
        StatusCodeInfo[] scodes = pinfo.getStatusCodeInfos();
        if (scodes != null) {
            buff.append("|" + name + ":");
            for (int i = 0; i < scodes.length; i++) {
                buff.append(scodes[i]);
                if (i < (scodes.length - 1)) {
                    buff.append(";");
                }
            }
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(255);
        String name=getClass().getName();
        int ind=name.lastIndexOf('.');
        if(ind>-1) name=name.substring(ind+1);
        sb.append("*** All wrapper-data for "+name+" {\n");
        IWrapperParam[] params=gimmeAllParams();
        for(IWrapperParam param:params) {
            if(param.getFrequency().equals("single")) {
                sb.append(param.getName()+" = "+param.getValue()).append("\n");
            } else {
                if(param.getValueArr()==null) {
                    sb.append(param.getName()+"[] = NULL");
                } else {
                    Object[] values=param.getValueArr();
                    for(int i=0;i<values.length;i++) {
                        sb.append(param.getName()+"["+i+"] = "+values[i]).append("\n");
                    }
                }
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

} // IWrapperImpl
