package de.schlund.pfixcore.example;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.generator.IWrapperParam;
import de.schlund.pfixcore.generator.IWrapperProxyFactory;
import de.schlund.pfixcore.generator.IWrapperProxyInterceptor;

public class Test {
    
    public static void main(String[] args) throws Exception {
      
        Class<?> iwrpClass = IWrapperProxyFactory.getIWrapperProxyClass(MyModel.class);
        IWrapper iwrp = (IWrapper)iwrpClass.newInstance();
        ((Factory)iwrp).setCallbacks(new Callback[] {new IWrapperProxyInterceptor()});
        iwrp.init("dummy");
        ((MyModel)iwrp).setAdult(true);
        iwrp.init("dummy");
        IWrapperParam[] params = iwrp.gimmeAllParams();
        for(IWrapperParam param: params) {
            System.out.println("PARAM: "+param.getName()+ " " + param.getType() + " "+ param.getValue());
        }
        
    }

}
