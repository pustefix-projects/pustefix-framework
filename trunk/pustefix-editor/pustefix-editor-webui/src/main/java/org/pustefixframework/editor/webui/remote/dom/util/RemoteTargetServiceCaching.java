package org.pustefixframework.editor.webui.remote.dom.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.editor.common.remote.transferobjects.TargetTO;

public class RemoteTargetServiceCaching implements InvocationHandler {

    private Object target;
    
    private Map<String, TargetTO> targetCache = new HashMap<String, TargetTO>();
    
    public RemoteTargetServiceCaching(Object target) {
        this.target = target;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         
        if(method.getName().equals("getTarget")) {
            String targetName = (String)args[0];
            TargetTO targetTO;
            synchronized(targetCache) {
                targetTO = targetCache.get(targetName);
            }
            if(targetTO == null) {
                targetTO = (TargetTO)method.invoke(target, args);
                synchronized(targetCache) {
                    if(targetTO != null) targetCache.put(targetName, targetTO);
                }
            }
            return targetTO;
        }
        return method.invoke(target, args);
    }
    
}
