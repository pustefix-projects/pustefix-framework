/*
 * de.schlund.pfixcore.webservice.generate.js.JsMethod
 */
package de.schlund.pfixcore.webservice.generate.js;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JsMethod.java 
 * 
 * Created: 30.08.2004
 * 
 * @author mleidig
 */
public class JsMethod {

    JsClass jsClass;
    String name;
    JsParam[] params;
    JsParam retParam;
    JsBlock body;
    
    public JsMethod(JsClass jsClass,String name) {
        this.jsClass=jsClass;
        this.name=name;
        params=new JsParam[0];
        body=new JsBlock();
    }
    
    public JsClass getJsClass() {
        return jsClass;
    }
    
    public String getName() {
        return name;
    }
    
    public void addParam(JsParam param) {
        JsParam[] upd=new JsParam[params.length+1];
        for(int i=0;i<params.length;i++) upd[i]=params[i];
        upd[upd.length-1]=param;
        params=upd;
    }
    
    public JsParam[] getParams() {
        return params;
    }
    
    public String getParamList() {
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<params.length;i++) {
            sb.append(params[i].getName());
            if(i<params.length-1) sb.append(",");
        }
        return sb.toString();
    }
    
    public JsBlock getBody() {
        return body;
    }
    
    public void printCode(String indent,OutputStream out) throws IOException {
        StringBuffer sb=new StringBuffer();
        sb.append(getJsClass().getName()+".prototype."+getName()+"=function("+getParamList()+") {\n");
        out.write(sb.toString().getBytes());
        getBody().printCode(indent,out);
        out.write("}\n".getBytes());
    }
    
}
