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
    float floatVal;
    
    public DataBean() {}
    
    public DataBean(String name,Calendar date,int intVal,float floatVal) {
        this.name=name;
        this.date=date;
        this.intVal=intVal;
        this.floatVal=floatVal;
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
    
    public float getFloatVal() {
        return floatVal;
    }
    
    public void setFloatVal(float floatVal) {
        this.floatVal=floatVal;
    }
    
}
