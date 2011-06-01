package de.schlund.pfixxml.resources;

import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixxml.resources.internal.Filter;
import de.schlund.pfixxml.resources.internal.FrameworkUtil;
import de.schlund.pfixxml.resources.internal.InvalidSyntaxException;

public class ModuleFilter {

    private Filter filter;
    
    public ModuleFilter(String filterExpression) {
        try {
            filter = FrameworkUtil.createFilter(filterExpression);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Illegal filter expression: " + filterExpression, e);
        }
    }
    
    public boolean accept(ModuleDescriptor module) {
        return filter.match(module.getFilterAttributes());
    }
    
    @Override
    public String toString() {
        return "ModuleFilter " + filter.toString();
    }
    
}
