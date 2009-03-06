package de.schlund.pfixcore.beans;

import de.schlund.pfixcore.beans.Alias;
import de.schlund.pfixcore.beans.ExcludeByDefault;
import de.schlund.pfixcore.beans.Include;

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
