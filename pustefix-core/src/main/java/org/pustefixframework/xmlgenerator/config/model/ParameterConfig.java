package org.pustefixframework.xmlgenerator.config.model;

import java.util.List;

/**
 * Interface implemented by model classes supporting parameter configuration.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface ParameterConfig {

	public List<Parameter> getParameters();
	public void addParameter(String name, String value);
	
}
