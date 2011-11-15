/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
    
    @Override
    public boolean isAccessible(Context context,PfixServletRequest preq) throws Exception {
        return true;
    }

    @Override
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
