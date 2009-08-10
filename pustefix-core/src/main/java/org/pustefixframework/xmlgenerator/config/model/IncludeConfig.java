package org.pustefixframework.xmlgenerator.config.model;

import java.net.URI;
import java.util.List;

/**
 * Interface implemented by model classes supporting include configuration.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface IncludeConfig {
	
	public void addInclude(URI uri);
	public List<IncludeDef> getIncludes();

}
