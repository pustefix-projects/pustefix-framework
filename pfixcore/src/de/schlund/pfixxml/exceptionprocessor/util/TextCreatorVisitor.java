/*
 * Created on 27.05.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
		HashMap map = data.getRequestParams();
		if(map == null) {
			requestparam_info.append("No parameters");
		} else {
			for(Iterator iter = map.keySet().iterator(); iter.hasNext();) {
				String key = iter.next().toString();
				String value = map.get(key).toString();
				requestparam_info.append(key+" = "+value+"\n");
			}
		}
		
		
		
		StringBuffer laststep_info = new StringBuffer();
		List steps = data.getLastSteps();
		if(steps == null) {
			laststep_info.append("No last step info");
		} else {
			for(Iterator iter = steps.iterator(); iter.hasNext(); ) {
				Object value = iter.next();
				laststep_info.append(value+"\n");
			}
		}
		
		StringBuffer sessiondata_info = new StringBuffer();
		HashMap sessmap = data.getSessionKeysAndValues();
		if(sessmap == null) {
			sessiondata_info.append("No session keys and values");
		} else {
			for(Iterator iter = sessmap.keySet().iterator(); iter.hasNext(); ) {
				String key = iter.next().toString();
				String value = sessmap.get(key).toString();
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
		
		
		 Object[] args = new Object[] {
	    		data.getServername(), 
				data.getServlet().startsWith("/") ?
						data.getServlet().substring(1, data.getServlet().length()) :
						data.getServlet(),
				data.getPage(),
				data.getThrowable().getClass().getName(),
				data.getThrowable().getMessage() == null ?
						(data.getThrowable().getStackTrace()[0] == null ?
								"No stacktrace" : 
								data.getThrowable().getStackTrace()[0].toString()
						) :
                        data.getThrowable().getMessage()};
	    
		 subject = MessageFormat.format("{0}|{1}|{2}|{3}:{4}", args);
	}

	public String getText() {
		return text;
	}
	
	public String getSubject() {
		return subject;
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