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

package de.schlund.pfixxml.exceptionprocessor.monitor;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.exceptionprocessor.ExceptionConfig;
import de.schlund.pfixxml.exceptionprocessor.ExceptionProcessor;

/**
 * ExceptionProcessor implementation wrapping another ExceptionProcessor instance to
 * collect exceptions and making them available via JMX.
 *
 */
public class MonitoringExceptionProcessor implements ExceptionProcessor, ErrorMonitoringMXBean {

    private final static Logger LOG = Logger.getLogger(MonitoringExceptionProcessor.class);
    
    private ExceptionProcessor delegate;
    private TimedList<ErrorMessage> errors = new TimedList<ErrorMessage>(1000, 1000 * 60 * 30);
    
    public MonitoringExceptionProcessor(ExceptionProcessor delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void processException(Throwable exception, ExceptionConfig exConfig, PfixServletRequest pfixReq, ServletContext servletContext,
            HttpServletRequest req, HttpServletResponse res, Properties properties) throws IOException, ServletException {
    
        if(delegate != null) {
            delegate.processException(exception, exConfig, pfixReq, servletContext, req, res, properties);
        }
        
        String serverName = "-";
        if(req != null) {
            serverName = req.getServerName();
        }
        String throwableType = "-";
        String throwableMsg = "-";
        if(exception != null) {
            throwableType = exception.getClass().getName();
            throwableMsg = exception.getMessage();
        }
        ErrorMessage msg = new ErrorMessage(new Date(System.currentTimeMillis()), serverName, throwableType, throwableMsg);
        errors.add(msg);
        
    }
    
    @Override
    public List<ErrorMessage> getLastExceptions() {
        return errors.get(3);
    }

    @Override
    public List<ErrorMessage> getExceptions(long from, long to) {
        if(to == 0) {
            to = Long.MAX_VALUE;
        }
        return errors.get(from, to);
    }
    
    public void registerJMX(String projectName) throws MalformedObjectNameException, MBeanRegistrationException,
            InstanceNotFoundException, InstanceAlreadyExistsException, NotCompliantMBeanException {

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("Pustefix:type=ErrorMonitoring,project=" + projectName);
        if (server.isRegistered(objectName)) {
            LOG.warn("Error monitoring MBean already registered.");
        } else {
            server.registerMBean(this, objectName);
            LOG.info("Registered error monitoring MBean.");
        }
    }

    public void unregisterJMX(String projectName) throws MalformedObjectNameException, MBeanRegistrationException,
            InstanceNotFoundException {

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("Pustefix:type=ErrorMonitoring,project=" + projectName);
        if (server.isRegistered(objectName)) {
            server.unregisterMBean(objectName);
            LOG.info("Unregistered error monitoring MBean.");
        } else {
            LOG.warn("No error monitoring MBean registered.");
        }
    }
    
}
