package de.schlund.pfixcore.beans;

import de.schlund.pfixcore.beans.Alias;
import de.schlund.pfixcore.beans.Exclude;

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
