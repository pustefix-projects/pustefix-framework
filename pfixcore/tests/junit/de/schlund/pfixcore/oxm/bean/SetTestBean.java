package de.schlund.pfixcore.oxm.bean;

import java.util.HashSet;
import java.util.Set;

import de.schlund.pfixcore.oxm.impl.annotation.SetSerializer;

/**
 * Test bean for SetSerializer
 * 
 * @author Dunja Fehrenbach <dunja.fehrenbach@1und1.de>
 */
public class SetTestBean {
    
    public Set<String> mySet = new HashSet<String>();
    
    @SetSerializer(elementName = "element")
    public Set<String> annoSet = new HashSet<String>();
    
    public SetTestBean() {
        this.annoSet.add("foo");
        this.annoSet.add("bar");
        
        this.mySet.add("foo");
        this.mySet.add("bar");
    }
}