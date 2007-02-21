package de.schlund.pfixcore.webservice.beans;

public class BeanE extends BeanD {
    
    int my;
    int hey;
    int ho;
    
    public int getMy() {
        return my;
    }
    
    public void setMy(int my) {
        this.my=my;
    }
        
    @Transient
    public int getHey() {
        return hey;
    }
    
    public void setHey(int hey) {
        this.hey=hey;
    }
              
    @Transient
    public int getHo() {
        return ho;
    }
    
    public void setHo(int ho) {
        this.ho=ho;                                                                                                            
    }
    
}
