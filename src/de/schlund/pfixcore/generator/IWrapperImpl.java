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
import java.net.*;
import java.text.*;
import java.util.*;
import org.apache.log4j.*;

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
    private   Category    CAT      = Category.getInstance(this.getClass().getName());
    private   String      logdir   = null; 
    private   String      pagename = null;
    private   String      visitid  = null;
    
    protected HashMap     params   = null; // single static parameters (of the form PREFIX.NAME)
    protected HashMap     errors   = null; // errors on single parameters
    protected HashMap     idxprms  = null; // array like indexed parameters (of the form PREFIX.NAME.INDEX)
    protected IHandler    handler  = null; // Make sure that you set the handler in the
                                          // constructor of a derived class
    
    public void initLogging(String logdir, String pagename, String visitid) {
        CAT.debug("*** Logging input for " + prefix + " into " + logdir + " " + pagename + " " + visitid + " ***");
        this.logdir   = logdir;
        this.pagename = pagename;
        this.visitid  = visitid;
    }

    public void tryErrorLogging() throws IOException {
        if (logdir != null && pagename != null && visitid != null) {
            File                log    = new File(logdir + "/" + pagename + "#" + prefix);
            Writer              out    = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(log, true)));
            IWrapperParamInfo[] errors = gimmeAllParamInfosWithErrors();
            if (errors != null && errors.length > 0) {
                StringBuffer buff = getLogBuffer("ERRORS");
                for (int j = 0; j < errors.length; j++) {
                    IWrapperParamInfo param  = errors[j];
                    StatusCode[]      scodes = param.getStatusCodes();
                    if (scodes != null) {
                        appendErrorInfoLog(param, buff);
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
                appendParamInfoLog((IWrapperParamInfo) iter.next(), buff);
            }
            for (Iterator iter = idxprms.values().iterator(); iter.hasNext();) {
                IWrapperIndexedParam pindex  = (IWrapperIndexedParam) iter.next();
                IWrapperParamInfo[] pinfoarr = pindex.getAllParamInfos();
                for (int i = 0; i < pinfoarr.length; i++) {
                    appendParamInfoLog((IWrapperParamInfo) iter.next(), buff);
                }
            }
            out.write(buff.toString() + "\n");
            out.flush();
        }
    }

    public final void init(String prefix) throws Exception {
        params  = new HashMap();
        errors  = new HashMap();
        idxprms = new HashMap();
        this.prefix = prefix;
        registerParamInfos();
    }

    public final void load(RequestData req) throws Exception {
        this.req = req;

        for (Iterator i = params.values().iterator(); i.hasNext();) {
            IWrapperParamInfo pinfo = (IWrapperParamInfo) i.next();
            if (CAT.isDebugEnabled()) {
                CAT.debug("===> Doing init for ParamInfo: " + pinfo.getName());
            }
            pinfo.initValueFromRequest(prefix, req);
            if (pinfo.errorHappened()) {
                if (CAT.isDebugEnabled()) {
                    CAT.debug("*** ERROR happened for ParamInfo: " + pinfo.getName());
                }
                synchronized (errors) {
                    errors.put(pinfo.getName(), pinfo);
                }
            }
        }
        for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
            IWrapperIndexedParam pindex = (IWrapperIndexedParam) i.next();
            if (CAT.isDebugEnabled()) {
                CAT.debug("===> Doing init for IndexedParam: " + pindex.getName());
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

    protected final Integer gimmeOrder() {
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

    public final IWrapperParamInfo[] gimmeAllParamInfos() {
        TreeSet retpar = new TreeSet();
        synchronized (params) {
            retpar.addAll(params.values());
        }
        synchronized (idxprms) {
            for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
                IWrapperIndexedParam pindex = (IWrapperIndexedParam) i.next();
                retpar.addAll(Arrays.asList(pindex.getAllParamInfos()));
            }
        }
        return (IWrapperParamInfo[]) retpar.toArray(new IWrapperParamInfo[] {
        });
    }

    public final IWrapperParamInfo[] gimmeAllParamInfosWithErrors() {
        TreeSet retpar = new TreeSet();
        synchronized (errors) {
            retpar.addAll(errors.values());
        }
        synchronized (idxprms) {
            for (Iterator i = idxprms.values().iterator(); i.hasNext();) {
                IWrapperIndexedParam pindex = (IWrapperIndexedParam) i.next();
                retpar.addAll(Arrays.asList(pindex.getAllParamInfosWithErrors()));
            }
        }
        return (IWrapperParamInfo[]) retpar.toArray(new IWrapperParamInfo[] {});
    }

//     public final Object[] getParamValueByName(String key) {
//         IWrapperParamInfo pinfo = gimmeParamInfoForKey(key);
//         if (pinfo != null) {
//             return pinfo.getValueArr();
//         }
//         return null;
//     }
    
//     public final Object[] getIndexedParamValueByNameAndIndex(String key, String index) {
//         IWrapperIndexedParam pindex = gimmeIndexedParamForKey(key);
//         if (pindex != null) {
//             IWrapperParamInfo pinfo = pindex.getParamInfoForIndex(index);
//             if (pinfo != null) {
//                 return pinfo.getValueArr();
//             }
//         }
//         return null;
//     }

    protected final IWrapperParamInfo gimmeParamInfoForKey(String key) {
        synchronized (params) {
            return (IWrapperParamInfo) params.get(key);
        }
    }

    protected final IWrapperIndexedParam gimmeIndexedParamForKey(String key) {
        synchronized (idxprms) {
            return (IWrapperIndexedParam) idxprms.get(key);
        }
    }

    protected void registerParamInfos() {
        // DO NOTHING.
        // This could be abstract, but descendents should be able to call
        // super.registerNeededValues() without worrying.
    }

    public final int compareTo(Object inobj) {
        IWrapperImpl in = (IWrapperImpl) inobj;
        return (gimmeOrder().compareTo(in.gimmeOrder()));
    }

    private StringBuffer getLogBuffer(String init) {
        StringBuffer buff = new StringBuffer(256);
        long         now  = System.currentTimeMillis();
        DateFormat   fmt  = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        buff.append(fmt.format(new Date(now)) + "|" + visitid + "|" + init);
        return buff;
    }

    
    private void appendParamInfoLog(IWrapperParamInfo pinfo, StringBuffer buff) {
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
    
    private void appendErrorInfoLog(IWrapperParamInfo pinfo, StringBuffer buff) {
        String       name   = pinfo.getName(); 
        StatusCode[] scodes = pinfo.getStatusCodes();
        if (scodes != null) {
            buff.append("|" + name + ":");
            for (int i = 0; i < scodes.length; i++) {
                StatusCode code = scodes[i];
                buff.append(code.getStatusCodeWithDomain());
                if (i < (scodes.length - 1)) {
                    buff.append(";");
                }
            }
        }
    }

} // IWrapperImpl
