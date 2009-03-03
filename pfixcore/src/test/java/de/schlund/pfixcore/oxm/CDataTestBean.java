package de.schlund.pfixcore.oxm;

import de.schlund.pfixcore.oxm.impl.annotation.ForceElementSerializer;

/**
 * Simple test bean to test the serialization
 * of cdata sections
 * 
 * @author  Stephan Schmidt <schst@stubbles.net>
 */
public class CDataTestBean {

    @ForceElementSerializer
    public String getCData() {
        return "This is <strong>cdata</strong>.";
    }
}