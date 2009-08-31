package org.pustefixframework.osgi.logging;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Bridges the OSGi LogService to commons-logging.
 * 
 * 
 * @author mleidig@schlund.de
 *
 */
public class LogServiceBridge implements BundleActivator, LogListener {

	private LogServiceTracker serviceTracker;
	
	public void start(BundleContext bundleContext) throws Exception {
		serviceTracker = new LogServiceTracker(bundleContext);
		serviceTracker.open();
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		serviceTracker.close();
	}
		
	public void logged(LogEntry entry) {
		Log logger = LogFactory.getLog(entry.getBundle().getSymbolicName());
		Throwable cause = entry.getException();
		String msg = entry.getMessage();
		if(entry.getLevel() == LogService.LOG_ERROR) {
			if(cause == null) logger.error(msg);
			else logger.error(msg, cause);
		} else if(entry.getLevel() == LogService.LOG_WARNING) {
			if(cause == null) logger.warn(msg);
			else logger.warn(msg, cause);
		} else if(entry.getLevel() == LogService.LOG_INFO) {
			if(cause == null) logger.info(msg);
			else logger.info(msg, cause);
		} else if(entry.getLevel() == LogService.LOG_DEBUG) {
			if(cause == null) logger.debug(msg);
			else logger.debug(msg, cause);
		}
	}
		
	
	private class LogServiceTracker extends ServiceTracker {
		
		LogServiceTracker(BundleContext bundleContext) {
			super(bundleContext, LogReaderService.class.getName(), null);
		}
		
		@Override
		public Object addingService(ServiceReference reference) {
			LogReaderService service = (LogReaderService)super.addingService(reference);
			service.addLogListener(LogServiceBridge.this);
			return service;
		}
			
		@Override
		public void removedService(ServiceReference reference, Object service) {
			((LogReaderService)service).removeLogListener(LogServiceBridge.this);
			super.removedService(reference, service);
		}
		
	}
	
}
