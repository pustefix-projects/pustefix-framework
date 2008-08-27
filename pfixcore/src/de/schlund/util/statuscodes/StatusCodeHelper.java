package de.schlund.util.statuscodes;

import java.lang.reflect.Method;

public class StatusCodeHelper {

    private final static String DEFAULT_STATUSCODE_CLASS = "org.pustefixframework.generated.CoreStatusCodes";
    
    public static StatusCode getStatusCodeByName(String name) {
        return getStatusCodeByName(name, false);
    }
    
    public static StatusCode getStatusCodeByName(String name, boolean optional) {
        StatusCode statusCode;
        String scClassName, scName;
        int ind = name.indexOf('#');
        if(ind==-1) {
            scClassName = DEFAULT_STATUSCODE_CLASS;
            scName = name;
        } else {
            scClassName = name.substring(0,ind);
            scName = name.substring(ind+1);
        }
        try {
            Class<?> scClass = Class.forName(scClassName);
            Method scMeth = scClass.getDeclaredMethod("getStatusCodeByName",String.class);
            statusCode = (StatusCode)scMeth.invoke(null, scName);
            if(statusCode==null && optional==false) 
                throw new StatusCodeException("StatusCode " + name + " is not defined.");
        } catch(ClassNotFoundException x) {
            throw new StatusCodeException("StatusCode class not found: "+scClassName,x);
        } catch(NoSuchMethodException x) {
            throw new StatusCodeException("StatusCode class hasn't method 'getStatusCodeByName'",x);
        } catch(Exception x) {
            throw new StatusCodeException("Can't get StatusCode '"+scName+"' from class '"+scClassName+"'",x);
        }
        return statusCode;
    }
    
}
