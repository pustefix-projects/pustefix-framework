/*
 * de.schlund.pfixcore.example.webservices.ComplexDataBean
 */
package de.schlund.pfixcore.example.webservices;

import java.util.Calendar;

/**
 * ComplexDataBean.java 
 * 
 * Created: 28.06.2004
 * 
 * @author mleidig
 */
public class ComplexDataBean extends DataBean {
    
    ComplexDataBean parent;
    ComplexDataBean[] children;
    
    public ComplexDataBean() {}
    
    public ComplexDataBean(String name,Calendar date,int intVal,float floatVal) {
        super(name,date,intVal,floatVal);
    }

    public void setParent(ComplexDataBean parent) {
        this.parent=parent;
    }
    
    public ComplexDataBean getParent() {
        return parent;
    }
    
    public void setChildren(ComplexDataBean[] children) {
        this.children=children;
    }
    
    public ComplexDataBean[] getChildren() {
        return children;
    }
    
}
