package org.pustefixframework.resource.internal;

import java.util.Dictionary;

public interface DynamicIncludeInfo {

	public String getModuleName();
	public int getDynamicSearchLevel();
	public boolean overridesResource(String module, String path);
	public Dictionary<String,String> getFilterAttributes();
	
}
