package org.pustefixframework.webservices;


public interface ProtocolProvider {

	public String getProtocolName();
	public String getProtocolVersion();
	public ServiceProcessor getServiceProcessor();
	public ServiceStubGenerator getServiceStubGenerator();
	
}
