/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.webservices.fault;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.config.generic.PropertyFileReader;
import org.pustefixframework.webservices.ServiceRequest;
import org.pustefixframework.webservices.ServiceResponse;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class ExceptionProcessorAdapter extends FaultHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -7046404948962289457L;

    final static Logger LOG=Logger.getLogger(ExceptionProcessorAdapter.class);
    
    private final static String PARAM_CONFIG="config";
    private final static String PROP_EXPROC="exception.java.lang.Throwable.processor";
    
    ExceptionProcessor exProc;
    Properties exProcProps;
    ExceptionConfig exConf;
    
    public ExceptionProcessorAdapter() {
        
    }
    
    @Override
    public void init() {
        String config=getParam(PARAM_CONFIG);
        if(config==null) throw new IllegalArgumentException("Parameter '"+PARAM_CONFIG+"' is missing.");
        FileResource configFile=ResourceUtil.getFileResourceFromDocroot(config);
        exProcProps=new Properties();
        try {
            PropertyFileReader.read(configFile,exProcProps);
        } catch(Exception x) {
            throw new RuntimeException("Can't load properties from "+configFile,x);
        }
        String procName=exProcProps.getProperty(PROP_EXPROC);
        if(procName!=null) {
            try {
                Class<?> clazz=Class.forName(procName);
                exProc=(ExceptionProcessor)clazz.newInstance();
                exConf=new ExceptionConfig();
                exConf.setPage("webservice");
                exConf.setProcessor(exProc);
                exConf.setType("java.lang.Throwable");
            } catch(Exception x) {
                throw new RuntimeException("Can't instantiate ExceptionProcessor.",x);
            }
        } else LOG.warn("No ExceptionProcessor for java.lang.Throwable found in properties!");
    }
    
    @Override
    public void handleFault(Fault fault) {
        if(isNotificationError(fault)) {
            if(!(fault.getThrowable() instanceof Exception)) {
                LOG.error("Throwable isn't Exception instance: "+fault.getThrowable().getClass().getName());
                return;
            }
            Exception ex=(Exception)fault.getThrowable();
            ServiceRequest srvReq=fault.getRequest();
            ServiceResponse srvRes=fault.getResponse();
            if(srvReq.getUnderlyingRequest() instanceof HttpServletRequest) {
                HttpServletRequest req=(HttpServletRequest)srvReq.getUnderlyingRequest();
                HttpServletResponse res=(HttpServletResponse)srvRes.getUnderlyingResponse();
                PfixServletRequest pfixReq=new PfixServletRequestImpl(req,new Properties());
                HttpSession session=req.getSession(false);
                if(session!=null) {
                    try {
                        exProc.processException(ex, exConf, pfixReq, session.getServletContext(), req, res, exProcProps);
                    } catch(Exception x) {
                        LOG.error("Can't process exception.",x);
                    }
                }
            }
        }
        if(isInternalServerError(fault)) fault.setThrowable(new InternalServerError());
    }
    
    public boolean isInternalServerError(Fault fault) {
        Throwable t=fault.getThrowable();
        if(t!=null && (t instanceof Error)) return true;
        return false;
    }
    
    public boolean isNotificationError(Fault fault) {
        return true;
    }
    
}
