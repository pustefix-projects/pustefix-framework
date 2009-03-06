/*
 * de.schlund.pfixcore.example.EncodingTestState
 */
package de.schlund.pfixcore.example;

import javax.servlet.http.HttpServletRequest;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.util.URIParameters;

/**
 * EncodingTestState.java 
 * 
 * Created: 25.11.2004
 * 
 * @author mleidig
 */
public class EncodingTestState extends DefaultIWrapperState {
    
    public boolean isAccessible(Context context,PfixServletRequest preq) throws Exception {
        return true;
    }

    public ResultDocument getDocument(Context context,PfixServletRequest req) throws Exception {
        HttpServletRequest srvReq=req.getRequest();
        String enc=srvReq.getCharacterEncoding();
        ContextEncodingTest ctxEnc=(ContextEncodingTest)context.getContextResourceManager().getResource(ContextEncodingTest.class.getName());
        ctxEnc.setEncoding(enc);
        String encoding=srvReq.getParameter("text.Encoding");
        if(encoding!=null && encoding.trim().length()>0 && !encoding.equals("none")) {
        	    URIParameters params=new URIParameters(srvReq.getQueryString(),encoding);
        	    String val=params.getParameter("text.Text");
        	    ctxEnc.setText(val);
        }
        return super.getDocument(context,req);
    }

}
