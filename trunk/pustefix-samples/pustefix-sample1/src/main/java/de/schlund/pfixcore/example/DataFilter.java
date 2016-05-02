package de.schlund.pfixcore.example;

import org.pustefixframework.web.mvc.filter.Filter;
import org.pustefixframework.web.mvc.filter.Property;

public class DataFilter {

    //store values in nested class to support binding
    //of parameters with prefixed names, e.g. "filter.enabled"
    private Values filter = new Values();
    
    public Values getFilter() {
        return filter;
    }
    
    public Filter getPropertyFilter() {
        if(filter.getEnabled() != null) {
            return new Property("enabled", filter.getEnabled());
        }
        return null;
    }
   
    public class Values {
        
        private Boolean enabled;

        public Boolean getEnabled() {
            return enabled;
        }
        
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
        
    }
    
}
