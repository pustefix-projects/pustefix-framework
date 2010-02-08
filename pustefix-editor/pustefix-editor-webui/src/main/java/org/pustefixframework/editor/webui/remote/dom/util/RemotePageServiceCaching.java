package org.pustefixframework.editor.webui.remote.dom.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.editor.common.remote.transferobjects.PageTO;

public class RemotePageServiceCaching implements InvocationHandler {

    private Object target;
    
    private Map<String, PageTO> pageCache = new HashMap<String, PageTO>();

    public RemotePageServiceCaching(Object target) {
        this.target = target;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        
        if(method.getName().equals("getPage")) {
            String pageName = (String)args[0];
            PageTO pageTO;
            synchronized(pageCache) {
                pageTO = pageCache.get(pageName);
            }
            if(pageTO == null) {
                pageTO = (PageTO)method.invoke(target, args);
                synchronized(pageCache) {
                    if(pageTO != null) pageCache.put(pageName, pageTO);
                }
            }
            return pageTO;
        }
        return method.invoke(target, args);
    }

}
