package org.pustefixframework.xmlgenerator.view;

import org.pustefixframework.extension.Extension;
import org.pustefixframework.resource.Resource;

/**
 * Interface implemented by view extensions contributing to according view extension points.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface ViewExtension extends Extension {

	public String getPartName();
	public Resource getResource();
	public String getModule();
	
}
