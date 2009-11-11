package org.pustefixframework.resource.internal;

public interface DynamicIncludeModuleFilter {

    public String getApplication();
    public boolean accept(DynamicIncludeInfo info);
    
}
