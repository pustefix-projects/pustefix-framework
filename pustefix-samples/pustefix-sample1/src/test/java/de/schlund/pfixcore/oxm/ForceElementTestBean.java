package de.schlund.pfixcore.oxm;

import java.util.Date;

import de.schlund.pfixcore.beans.Alias;
import de.schlund.pfixcore.oxm.impl.annotation.DateSerializer;
import de.schlund.pfixcore.oxm.impl.annotation.ForceElementSerializer;

/**
 * Simple test bean that tests the 
 * ForceElementSerializer
 *  
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class ForceElementTestBean {
    
    @ForceElementSerializer
    public String foo = "foo";

    @ForceElementSerializer
    @DateSerializer("yyyy-MM-dd HH:mm:ss")
    @Alias("openingDate")
    public Date date = new Date(1204479792269l);
    
    @ForceElementSerializer
    @Alias("baz")
    public String getBar() {
        return "bar";
    }
}