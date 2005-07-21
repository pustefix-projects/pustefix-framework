/*
 * de.schlund.pfixcore.webservice.generate.js.JsStatement
 */
package de.schlund.pfixcore.webservice.generate.js;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JsStatement.java 
 * 
 * Created: 30.08.2004
 * 
 * @author mleidig
 */
public class JsStatement {
    
    String code;
        
    public JsStatement(String code) {
        this.code=code;
    }
    
    public void printCode(String indent,OutputStream out) throws IOException { 
        StringBuffer sb=new StringBuffer();
        sb.append(indent);
        sb.append(code);
        if(!code.endsWith(";")) sb.append(";");
        sb.append("\n");
        out.write(sb.toString().getBytes());
    }
    
}
