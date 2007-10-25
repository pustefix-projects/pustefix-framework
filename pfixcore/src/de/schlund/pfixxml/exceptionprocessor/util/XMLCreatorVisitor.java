/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package de.schlund.pfixxml.exceptionprocessor.util;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;


/**
 * @author jh
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XMLCreatorVisitor implements ExceptionDataValueVisitor {

	private Document doc;
	/* (non-Javadoc)
	 * @see de.schlund.jmsexceptionhandler.rmiobj.ExceptionDataValueVisitor#visit(de.schlund.jmsexceptionhandler.rmiobj.ExceptionDataValue)
	 */
	public void visit(ExceptionDataValue data) {
		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		dbfac.setNamespaceAware(false);
		dbfac.setValidating(false);
		try {
			doc = dbfac.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return;
		}
		Element e = doc.createElement("error");
		e.setAttribute("type", data.getThrowable().getClass().getName());
		
		Element sess_info = doc.createElement("sessioninfo");
		Text sess_info_txt = doc.createTextNode(data.getSessionid());
		sess_info.appendChild(sess_info_txt);
		e.appendChild(sess_info);
		
		Element req_params = doc.createElement("requestparams");
		HashMap params = data.getRequestParams();
		for(Iterator iter = params.keySet().iterator(); iter.hasNext(); ) {
			String key = iter.next().toString();
			String value = params.get(key).toString();
			Element req_p = doc.createElement("param");
			req_p.setAttribute("key", key);
			Text req_p_txt = doc.createTextNode(value);
			req_p.appendChild(req_p_txt);
			req_params.appendChild(req_p);
		}
		e.appendChild(req_params);
		
		Element last_steps = doc.createElement("laststeps");
		for(Iterator iter = data.getLastSteps().iterator(); iter.hasNext(); ) {
			Element step = doc.createElement("step");
			Text step_txt = doc.createTextNode(iter.next().toString());
			step.appendChild(step_txt);
			last_steps.appendChild(step);
		}
		e.appendChild(last_steps);
		
		Element sess_keysnvals = doc.createElement("session_dump");
		HashMap map = data.getSessionKeysAndValues();
		for(Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
			String key = iter.next().toString();
			String val = map.get(key).toString();
			Element pair = doc.createElement("pair");
			pair.setAttribute("key", key);
			Text cd = doc.createTextNode(val); 
			pair.appendChild(cd);
			sess_keysnvals.appendChild(pair);
		}
		e.appendChild(sess_keysnvals);
		
		appendThrowable(e, data.getThrowable());
		
		doc.appendChild(e);
        data.setXMLRepresentation(doc);
	}

	private void appendThrowable(Element elem, Throwable throwable) {
	    if(throwable!=null) {
	        Element exElem = doc.createElement("exception");
	        exElem.setAttribute("type", throwable.getClass().getName());
	        exElem.setAttribute("msg", throwable.getMessage());
	        Element stackElem = doc.createElement("stacktrace");
	        StackTraceElement[] strace = throwable.getStackTrace();
	        for(int i=0; i<strace.length; i++) {
	            Element lineElem = doc.createElement("line");
	            Text lineText = doc.createTextNode(strace[i].toString());
	            lineElem.appendChild(lineText);
	            stackElem.appendChild(lineElem);
	        }
	        exElem.appendChild(stackElem);
	        elem.appendChild(exElem);
	        if(throwable.getCause()!=null) appendThrowable(exElem, throwable.getCause());
	    }
	}
	
}
