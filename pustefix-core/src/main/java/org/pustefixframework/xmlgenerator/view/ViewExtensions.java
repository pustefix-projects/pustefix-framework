package org.pustefixframework.xmlgenerator.view;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Class holding references to a bundle's ViewExtensions and responsible
 * for destroying them when it's destroyed itself.
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensions implements DisposableBean {

private final Log logger = LogFactory.getLog(ViewExtensions.class);
	
	private Collection<ViewExtensionImpl> extensions;

	public void setExtensions(Collection<ViewExtensionImpl> extensions) {
		this.extensions = extensions;
	}
	
	public void destroy() throws Exception {
		if(extensions != null) {
			if(logger.isDebugEnabled()) logger.debug("Destroying " + extensions.size() + " extensions");
			for(ViewExtensionImpl extension:extensions) {
				extension.destroy();
			}
		}
		
	}
	
}
