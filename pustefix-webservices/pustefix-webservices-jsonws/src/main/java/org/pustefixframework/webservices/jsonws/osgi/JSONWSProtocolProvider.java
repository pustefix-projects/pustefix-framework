package org.pustefixframework.webservices.jsonws.osgi;

import org.pustefixframework.webservices.ProtocolProvider;
import org.pustefixframework.webservices.ServiceException;
import org.pustefixframework.webservices.ServiceProcessor;
import org.pustefixframework.webservices.ServiceStubGenerator;
import org.pustefixframework.webservices.jsonws.JSONWSProcessor;
import org.pustefixframework.webservices.jsonws.JSONWSStubGenerator;

public class JSONWSProtocolProvider implements ProtocolProvider {

	public final static String PROTOCOL_NAME = "JSONWS";
	public final static String PROTOCOL_VERSION = "1";
	
	private ServiceProcessor serviceProcessor;
	private ServiceStubGenerator stubGenerator;
	
	public String getProtocolName() {
		return PROTOCOL_NAME;
	}
	
	public String getProtocolVersion() {
		return PROTOCOL_VERSION;
	}
	
	public synchronized ServiceProcessor getServiceProcessor() {
		if(serviceProcessor == null) {
			try {
				serviceProcessor = new JSONWSProcessor();
			} catch(ServiceException x) {
				throw new RuntimeException("Can't create ServiceProcessor for " + PROTOCOL_NAME + " protocol.", x);
			}
		}
		return serviceProcessor;
	}
	
	public synchronized ServiceStubGenerator getServiceStubGenerator() {
		if(stubGenerator == null) {
			stubGenerator = new JSONWSStubGenerator();
		}
		return stubGenerator;
	}
	
}
