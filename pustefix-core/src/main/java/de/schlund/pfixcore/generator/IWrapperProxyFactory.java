package de.schlund.pfixcore.generator;

import net.sf.cglib.proxy.Enhancer;

public class IWrapperProxyFactory {
    
    public static Class<? extends IWrapper> getIWrapperProxyClass(Class<?> modelClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(modelClass);
        enhancer.setInterfaces(new Class[] {IWrapper.class});
        enhancer.setCallback(new IWrapperProxyInterceptor());
        IWrapper iwrapper = (IWrapper)enhancer.create();
        return iwrapper.getClass();
    }

}
