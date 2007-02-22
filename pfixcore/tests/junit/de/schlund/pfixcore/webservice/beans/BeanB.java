package de.schlund.pfixcore.webservice.beans;

@ExcludeByDefault
public class BeanB extends BeanA {
    
    int my;
    int hey;
    int ho;
    
    @Include
    public int getMy() {
        return my;
    }
    
    public void setMy(int my) {
        this.my=my;
    }
        
    public int getHey() {
        return hey;
    }
    
    public void setHey(int hey) {
        this.hey=hey;
    }
                                                                                                                                     
    public int getHo() {
        return ho;
    }
    
    public void setHo(int ho) {
        this.ho=ho;                                                                                                            
    }

    
}
