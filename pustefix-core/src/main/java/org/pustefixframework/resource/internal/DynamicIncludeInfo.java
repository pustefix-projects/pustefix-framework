package org.pustefixframework.resource.internal;

public interface DynamicIncludeInfo {

	public String getModuleName();
	public int getDynamicSearchLevel();
	public boolean overridesResource(String module, String path);
	
}
