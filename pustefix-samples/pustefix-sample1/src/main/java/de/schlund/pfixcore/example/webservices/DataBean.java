/*
 * de.schlund.pfixcore.example.webservices.DataBean
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;

/**
 * DataBean.java 
 * 
 * Created: 28.06.2004
 * 
 * @author mleidig
 */
public class DataBean {
    
    String name;
    Calendar date;
    int intVal;
    float[] floatVals;
    Boolean boolVal;
    DataBean[] children;
    
    public DataBean() {}
    
    public DataBean(String name,Calendar date,int intVal,float[] floatVals) {
        this.name=name;
        this.date=date;
        this.intVal=intVal;
        this.floatVals=floatVals;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name=name;
    }
    
    public Calendar getDate() {
        return date;
    }
    
    public void setDate(Calendar date) {
        this.date=date;
    }
    
    public int getIntVal() {
        return intVal;
    }
    
    public void setIntVal(int intVal) {
        this.intVal=intVal;
    }
    
    public float[] getFloatVals() {
        return floatVals;
    }
    
    public void setFloatVals(float[] floatVals) {
        this.floatVals=floatVals;
    }
    
    public DataBean[] getChildren() {
        return children;
    }
   
    public void setChildren(DataBean[] children) {
        this.children=children;
    }
    
    public Boolean getBoolVal() {
        return boolVal;
    }
    
    public void setBoolVal(Boolean boolVal) {
        this.boolVal=boolVal;
    }
    
}
