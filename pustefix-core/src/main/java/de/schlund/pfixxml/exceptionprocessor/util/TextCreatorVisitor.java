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
 *
 */

package de.schlund.pfixxml.exceptionprocessor.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



/**
 * @author jh
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
//TODO: move me into a cool package
public class TextCreatorVisitor implements ExceptionDataValueVisitor {
	private String text;
	private String subject;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.schlund.jmsexceptionhandler.rmiobj.ExceptionDataValueVisitor#visit(de.schlund.jmsexceptionhandler.rmiobj.ExceptionDataValue)
	 */
	public void visit(ExceptionDataValue data) {
		String sessionid_info = MessageFormat.format("[SessionId: {0}]", new Object[]{
											data.getSessionid()});

		String url_info = MessageFormat.format("{0}://{1}:{2}{3}"
				+ (data.getQuery() != null ? "?{4}" : ""), 
				new Object[]{ data.getScheme(), data.getServername(), 
						"" + data.getPort(), data.getUri(), data.getQuery()});

		StringBuffer requestparam_info = new StringBuffer();
		HashMap<String, String> map = data.getRequestParams();
		if(map == null) {
			requestparam_info.append("No parameters");
		} else {
			for(Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
				String key = iter.next();
				String value = map.get(key);
				requestparam_info.append(key+" = "+value+"\n");
			}
		}
		
		
		
		StringBuffer laststep_info = new StringBuffer();
		List<String> steps = data.getLastSteps();
		if(steps == null) {
			laststep_info.append("No last step info");
		} else {
			for(Iterator<String> iter = steps.iterator(); iter.hasNext(); ) {
				String value = iter.next();
				laststep_info.append(value+"\n");
			}
		}
		
		StringBuffer sessiondata_info = new StringBuffer();
		HashMap<String, String> sessmap = data.getSessionKeysAndValues();
		if(sessmap == null) {
			sessiondata_info.append("No session keys and values");
		} else {
			for(Iterator<String> iter = sessmap.keySet().iterator(); iter.hasNext(); ) {
				String key = iter.next();
				String value = sessmap.get(key);
				sessiondata_info.append("Key   = "+key+"\n");
				sessiondata_info.append("Value = "+value+"\n");
				sessiondata_info.append("------------------------------------------------------\n");
			}
		}
		

		StringWriter strwriter = new StringWriter();
		PrintWriter p = new PrintWriter(strwriter);
	    data.getThrowable().printStackTrace(p);
	    p.flush();
	    

		
		text = sessionid_info + "\n\n\n" + 
				url_info +"\n\n\n" +
				"Parameter: \n" +requestparam_info.toString() +"\n\n\n" +
				"==== Last steps before error occured: ================\n" +
				laststep_info.toString() +"\n\n\n" +
				"==== Session keys and values: ========================\n"+
				sessiondata_info.toString() +"\n\n\n" +
				"==== Stacktrace: =====================================\n"+
				strwriter.getBuffer().toString();
		
		
        String msg = data.getThrowable().getMessage();
        if(msg == null) { 
           StackTraceElement[] strace = data.getThrowable().getStackTrace();
           if(strace.length > 0) {  
               msg = strace[0].toString().trim();
           } else {
                msg = "No stacktrace available";
            }
        }
                
		 Object[] args = new Object[] {
	    		data.getServername(), 
				data.getServlet().startsWith("/") ?
						data.getServlet().substring(1, data.getServlet().length()) :
						data.getServlet(),
				data.getPage(),
				data.getThrowable().getClass().getName(),
                msg
				};
	    
		 subject = MessageFormat.format("{0}|{1}|{2}|{3}:{4}", args);
         
         data.setTextSubjectRepresentation(subject);
         data.setTextBodyRepresentation(text);
	}

	
	/* (non-Javadoc)
	 * @see de.schlund.jmsexceptionhandler.tokenbucket.ReportDataValueVisitor#visit(de.schlund.jmsexceptionhandler.tokenbucket.ReportDataValue)
	 */
	/*public void visit(ReportDataValue rdata) {
		subject = "JBoss Report: "+rdata.getThrowableType()+"("+rdata.getDismissedCount()+")";
		StringBuffer sb = new StringBuffer();
		sb.append("Stacktrace: \n");
		StackTraceElement[] strace = rdata.getStackTrace();
		if(strace == null) {
			sb.append("Not available.");
		} else {
			for(int i=0; i<strace.length; i++) {
				sb.append(strace[i].toString()+"\n");
			}
		}
		text = rdata.getThrowableType()+"repeated "+rdata.getDismissedCount()+" times" +"\n" + sb.toString();
	}*/

}