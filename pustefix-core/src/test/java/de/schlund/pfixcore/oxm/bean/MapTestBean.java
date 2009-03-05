package de.schlund.pfixcore.oxm.bean;

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixcore.oxm.impl.annotation.MapSerializer;

/**
 * Test bean for MapSerializer
 * 
 * @author Stephan Schmidt <schst@stubbles.net>
 */
public class MapTestBean {
    
    public Map<String, String> myMap = new HashMap<String, String>();
    
    @MapSerializer(elementName = "element")
    public Map<String, String> annoMap = new HashMap<String, String>();
    
    public MapTestBean() {
        this.myMap.put("one", "foo");
        this.myMap.put("two", "bar");

        this.annoMap.put("one", "foo");
        this.annoMap.put("two", "bar");
    }
}