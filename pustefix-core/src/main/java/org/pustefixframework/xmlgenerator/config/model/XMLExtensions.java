package org.pustefixframework.xmlgenerator.config.model;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

public class XMLExtensions implements DisposableBean {

	private final Log logger = LogFactory.getLog(XMLExtensions.class);
	
	private Collection<XMLExtension<?>> extensions;

	public void setExtensions(Collection<XMLExtension<?>> extensions) {
		this.extensions = extensions;
	}
	
	public void destroy() throws Exception {
		if(extensions != null) {
			if(logger.isDebugEnabled()) logger.debug("Destroying " + extensions.size() + " extensions");
			for(XMLExtension<?> extension:extensions) {
				extension.destroy();
			}
		}
		
	}
	
}
