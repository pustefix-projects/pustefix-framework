package org.pustefixframework.pfxinternals;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SendErrorAction implements Action {

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse res, PageContext pageContext) throws IOException {
        
        int statusCode = 404;
        String param = req.getParameter("sc");
        if(param != null && !param.equals("")) {
            statusCode = Integer.parseInt(param);
        }
        String message = req.getParameter("msg");
        if(message == null || message.equals("")) {
            message = getStatusCodeMessage(statusCode);
        }
        res.sendError(statusCode, message);
    }
    
    private static String getStatusCodeMessage(int statusCode) {
        Field[] fields = HttpServletResponse.class.getFields();
        for(Field field: fields) {
            if(field.getName().startsWith("SC_") && field.getType() == int.class) {
                int value;
                try {
                    value = field.getInt(null);
                    if(statusCode == value) {
                        return field.getName().substring(3);
                    }
                } catch(Exception e) {
                    //ignore;
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
    
}
