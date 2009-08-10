package org.pustefixframework.xmlgenerator.config.model;

import org.pustefixframework.resource.ResourceLoader;

public class SourceInfo {

	private String bundleSymbolicName;
	private ResourceLoader resourceLoader;
	
	public SourceInfo(String bundleSymbolicName, ResourceLoader resourceLoader) {
		this.bundleSymbolicName = bundleSymbolicName;
		this.resourceLoader = resourceLoader;
	}
	
	public String getBundleSymbolicName() {
		return bundleSymbolicName;
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
}
