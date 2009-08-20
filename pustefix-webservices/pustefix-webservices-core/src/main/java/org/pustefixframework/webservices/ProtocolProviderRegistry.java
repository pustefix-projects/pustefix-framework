package org.pustefixframework.webservices;

public interface ProtocolProviderRegistry {

	public ProtocolProvider getProtocolProvider(String protocolName, String protocolVersion);
	
}
