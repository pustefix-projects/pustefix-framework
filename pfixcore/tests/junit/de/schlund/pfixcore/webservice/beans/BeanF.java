package de.schlund.pfixcore.webservice.beans;

@TransientByDefault
public class BeanF extends BeanB {

    @Override
    public int getFoo() {
        return super.getFoo();
    }
                 
    @Property
    @Override                                                         
    public int getBar() {
        return super.getBar();
    }
     
    @Property
    @Override
    public int getHey() {
        return super.getHey();
    }

}
