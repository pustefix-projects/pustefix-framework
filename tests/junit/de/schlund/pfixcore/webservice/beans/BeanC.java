package de.schlund.pfixcore.webservice.beans;

public class BeanC extends BeanB {

    @Exclude
    @Override
    public int getFoo() {
        return super.getFoo();
    }
                   
    @Override
    @Alias("mybar")
    public int getBar() {
        return super.getBar();
    }
                                                          
    @Override
    public int getHey() {
        return super.getHey();
    }

}
