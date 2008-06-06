/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package de.schlund.pfixcore.webservice.jsonws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mleidig@schlund.de
 */
public class TestBean {
    private int value;
    private String text;
    private List<String> strList;
    private List<List<Integer>> intList;
    private Map<String,String> strMap;
    private Map<String,Map<String,Integer>> mapMap;
    private HashMap<String,Integer> intMap;
    
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
    
    public HashMap<String,Integer> getIntMap() {
        return intMap;
    }
    
    public void setIntMap(HashMap<String,Integer> intMap) {
        this.intMap=intMap;
    }
    
}
