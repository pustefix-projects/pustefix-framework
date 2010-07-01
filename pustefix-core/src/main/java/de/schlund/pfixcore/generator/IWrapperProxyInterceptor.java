package de.schlund.pfixcore.generator;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class IWrapperProxyInterceptor implements MethodInterceptor {

    private IWrapperProxyDelegate delegate;
    
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
    
        if(delegate == null) delegate = new IWrapperProxyDelegate(obj);
        Method[] methods = delegate.getClass().getMethods();
        for (Method meth : methods) {
            if (meth.getName().equals(method.getName())) {
                return meth.invoke(delegate, args);
            }
        }
        if(method.getName().startsWith("set") && args.length==1 && args[0]!=null) {
            String param = method.getName().substring(3);
            delegate.gimmeParamForKey(param).setStringValue(new String[] {args[0].toString()});
        }
        return proxy.invokeSuper(obj, args);
        
    }
    
}
