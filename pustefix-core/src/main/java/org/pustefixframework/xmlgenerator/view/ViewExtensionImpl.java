package org.pustefixframework.xmlgenerator.view;

import org.pustefixframework.extension.support.AbstractExtension;
import org.pustefixframework.resource.Resource;

/**
 * View extension implementation, which returns the Resource and part name it's contributing
 * to an extension point.
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensionImpl extends AbstractExtension<ViewExtensionPoint,ViewExtensionImpl> implements ViewExtension {

	private String partName;
	private Resource resource;
	
	public String getPartName() {
		return partName;
	}
	
	public void setPartName(String partName) {
		this.partName = partName;
	}
	
	public String getModule() {
		return bundleContext.getBundle().getSymbolicName();
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
}
