package de.schlund.pfixcore.webservice.fault;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.webservice.ServiceRequest;
import de.schlund.pfixcore.webservice.ServiceResponse;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.PfixServletRequestImpl;
import de.schlund.pfixxml.config.XMLPropertiesUtil;
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
            XMLPropertiesUtil.loadPropertiesFromXMLFile(configFile,exProcProps);
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
