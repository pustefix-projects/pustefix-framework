/*
 * de.schlund.pfixcore.example.ComplexData
 */
package de.schlund.pfixcore.example;

import java.util.Date;
import java.util.Vector;

/**
 * ComplexData.java 
 * 
 * Created: 28.06.2004
 * 
 * @author mleidig
 */
public class ComplexData implements java.io.Serializable {
    
    String name;
    Date date;
    int intVal;
    float floatVal;
    ComplexData parent;
    ComplexData[] children;
    //Vector data;
    
    public ComplexData() {}
    
    public ComplexData(String name,Date date,int intVal,float floatVal) {
        this.name=name;
        this.date=date;
        this.intVal=intVal;
        this.floatVal=floatVal;
        //this.data=data;
    }
    
    /**
    public ComplexData(String name,Date date,int intVal,float floatVal,Vector data) {
        this.name=name;
        this.date=date;
        this.intVal=intVal;
        this.floatVal=floatVal;
        this.data=data;
    }
    */

    public String getName() {
        return name;
    }
    
    public Date getDate() {
        return date;
    }
    
    public int getIntVal() {
        return intVal;
    }
    
    
    public float getFloatVal() {
        return floatVal;
    }
    
    public ComplexData getParent() {
        return parent;
    }
    
    public void setParent(ComplexData parent) {
        this.parent=parent;
    }
    
    public ComplexData[] getChildren() {
        return children;
    }
    
    public void setChildren(ComplexData[] children) {
        this.children=children;
    }
    /**
    public Vector getData() {
        return data;
    }
   */
}
