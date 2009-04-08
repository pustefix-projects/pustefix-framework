package de.schlund.pfixcore.oxm.bean;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class CovariantBeanB extends CovariantBeanA {
    
    private String value;
    
    @Override
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

}
