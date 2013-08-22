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

package de.schlund.pfixxml.exceptionprocessor;

import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.schlund.pfixxml.exceptionprocessor.monitor.MonitoringExceptionProcessor;

/**
 * Manages ExceptionProcessor configurations. Configurations can
 * be retrieved by Exception type.
 * Adds JMX monitoring capability to the default ExceptionProcessor.
 *
 */
public class ExceptionProcessingConfiguration implements InitializingBean, DisposableBean{

    private Logger LOG = Logger.getLogger(ExceptionProcessingConfiguration.class);
    
    private Map<Class<?>, ExceptionConfig> exceptionConfigs;
    private MonitoringExceptionProcessor monitoringProcessor;
    private String projectName;
    
    public void setExceptionConfigs(Map<Class<?>,ExceptionConfig> exceptionConfigs) {
        this.exceptionConfigs = exceptionConfigs;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //wrap main ExceptionProcessor for java.lang.Throwable into MonitoringExceptionProcessor
        //to make information available via JMX for internal and external monitoring
        ExceptionConfig config = getExceptionConfigForThrowable(Throwable.class);
        if(config == null) {
            config = new ExceptionConfig();
            config.setType("java.lang.Throwable");
            monitoringProcessor = new MonitoringExceptionProcessor(null);
            config.setProcessor(monitoringProcessor);
            exceptionConfigs.put(java.lang.Throwable.class, config);
        } else {
            ExceptionProcessor proc = config.getProcessor();
            monitoringProcessor = new MonitoringExceptionProcessor(proc);
            config.setProcessor(monitoringProcessor);
        }
        monitoringProcessor.registerJMX(projectName);
    }
    
    @Override
    public void destroy() throws Exception {
        monitoringProcessor.unregisterJMX(projectName);
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
