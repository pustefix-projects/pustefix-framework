/*
 * de.schlund.pfixcore.webservice.generate.js.JsBlock
 */
package de.schlund.pfixcore.webservice.generate.js;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JsBlock.java 
 * 
 * Created: 30.08.2004
 * 
 * @author mleidig
 */
public class JsBlock {

    JsStatement[] statements;
    
    public JsBlock() {
        statements=new JsStatement[0];
    }
    
    public void addStatement(JsStatement statement) {
        JsStatement[] upd=new JsStatement[statements.length+1];
        for(int i=0;i<statements.length;i++) upd[i]=statements[i];
        upd[upd.length-1]=statement;
        statements=upd;
    }
    
    public JsStatement[] getStatements() {
        return statements;
    }
    
    public void printCode(String indent,OutputStream out) throws IOException {
        for(int i=0;i<statements.length;i++) {
            statements[i].printCode(indent,out);
        }
    }
    
}
