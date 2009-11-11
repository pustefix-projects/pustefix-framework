package org.pustefixframework.resource.internal;

import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

public class DynamicIncludeModuleFilterImpl implements DynamicIncludeModuleFilter {

    private String application;
    private Filter filter;
    
    public DynamicIncludeModuleFilterImpl(String application, String filterExpression) {
        this.application = application;
        try {
            filter = FrameworkUtil.createFilter(filterExpression);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Illegal filter expression: " + filterExpression, e);
        }
    }
    
    public String getApplication() {
        return application;
    }
    
    public boolean accept(DynamicIncludeInfo info) {
        return filter.match(info.getFilterAttributes());
    }
    
    @Override
    public String toString() {
        return "ModuleFilter " + filter.toString();
    }
    
}
