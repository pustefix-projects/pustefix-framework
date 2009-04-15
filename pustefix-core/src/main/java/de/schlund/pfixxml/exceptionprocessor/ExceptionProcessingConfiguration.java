package de.schlund.pfixxml.exceptionprocessor;

import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ExceptionProcessingConfiguration {

    private Logger LOG = Logger.getLogger(ExceptionProcessingConfiguration.class);
    
    private Map<Class<?>, ExceptionConfig> exceptionConfigs;
    
    public void setExceptionConfigs(Map<Class<?>,ExceptionConfig> exceptionConfigs) {
        this.exceptionConfigs = exceptionConfigs;
    }
    
    /**
     * 
     * @return null if no processor is responsible for the passed throwable
     * @throws ServletException
     * @throws ClassNotFoundException
     */
    public ExceptionConfig getExceptionConfigForThrowable(Class<? extends Throwable> clazz) throws ServletException {
        ExceptionConfig exConf=null;
        if(clazz!=null) {
            exConf=exceptionConfigs.get(clazz);
            if(exConf==null) {
                if(clazz.getSuperclass() == Object.class) {
                    LOG.warn("No exception processor, page or forward configured for " + clazz.getName()); 
                } else {
                    exConf=getExceptionConfigForThrowable(clazz.getSuperclass().asSubclass(Throwable.class));
                }
            }
        }
        return exConf;
    }
    
}
