/*
 * Created on 04.04.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.schlund.pfixcore.webservice.jsonws;

import java.util.List;
import java.util.Map;

public class TestBean {

    private int value;
    private String text;
    private List<String> strList;
    private List<List<Integer>> intList;
    private Map<String,String> strMap;
    private Map<String,Map<String,Integer>> mapMap;
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value=value;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text=text;
    }
    
    public List<String> getStrList() {
        return strList;
    }
    
    public void setStrList(List<String> strList) {
        this.strList=strList;
    }
    
    public List<List<Integer>> getIntList() {
        return intList;
    }
    
    public void setIntList(List<List<Integer>> intList) {
        this.intList=intList;
    }
    
    public Map<String,String> getStrMap() {
        return strMap;
    }
    
    public void setStrMap(Map<String,String> strMap) {
        this.strMap=strMap;
    }
    
    public Map<String,Map<String,Integer>> getMapMap() {
        return mapMap;
    }
    
    public void setMapMap(Map<String,Map<String,Integer>> mapMap) {
        this.mapMap=mapMap;
    }
    
}
