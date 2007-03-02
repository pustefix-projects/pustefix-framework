package de.schlund.pfixcore.webservice.beans;

@ExcludeByDefault
public class BeanF extends BeanB {

    @Override
    public int getFoo() {
        return super.getFoo();
    }
                 
    @Include
    @Alias("mybar")
    @Override                                                         
    public int getBar() {
        return super.getBar();
    }
     
    @Include
    @Override
    public int getHey() {
        return super.getHey();
    }

}
