/*
 * de.schlund.pfixcore.example.DataBean
 */
package de.schlund.pfixcore.example;

/**
 * DataBean.java 
 * 
 * Created: 28.06.2004
 * 
 * @author mleidig
 */
public class DataBean implements java.io.Serializable {
    
    String id;
    
    public DataBean() {
        
    }
    
    public DataBean(String id) {
        this.id=id;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id=id;
    }
    
}
