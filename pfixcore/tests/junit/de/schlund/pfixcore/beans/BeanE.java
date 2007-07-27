package de.schlund.pfixcore.beans;

import de.schlund.pfixcore.beans.Exclude;

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
        
    @Exclude
    public int getHey() {
        return hey;
    }
    
    public void setHey(int hey) {
        this.hey=hey;
    }
              
    @Exclude
    public int getHo() {
        return ho;
    }
    
    public void setHo(int ho) {
        this.ho=ho;                                                                                                            
    }
    
}
