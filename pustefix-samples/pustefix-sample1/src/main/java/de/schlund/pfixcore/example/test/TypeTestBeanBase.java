package de.schlund.pfixcore.example.test;

import de.schlund.pfixcore.generator.annotation.IWrapper;

@IWrapper(ihandler = TypeTestBeanDummyHandler.class)
public class TypeTestBeanBase extends AbstractTypeTestBean {
    
    private boolean enabled;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
