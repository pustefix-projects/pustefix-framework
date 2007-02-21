package de.schlund.pfixcore.webservice.beans;

public class BeanC extends BeanB {

    @Transient
    @Override
    public int getFoo() {
        return super.getFoo();
    }
                   
    @Override                                                         
    public int getBar() {
        return super.getBar();
    }
                                                          
    @Override
    public int getHey() {
        return super.getHey();
    }

}
