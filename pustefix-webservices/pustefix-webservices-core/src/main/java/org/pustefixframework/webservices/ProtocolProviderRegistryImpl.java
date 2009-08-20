package org.pustefixframework.webservices;

import java.util.ArrayList;
import java.util.List;

public class ProtocolProviderRegistryImpl implements ProtocolProviderRegistry {

	private List<ProtocolProvider> protocolProviders = new ArrayList<ProtocolProvider>();
	
	public void addProtocolProvider(ProtocolProvider protocolProvider) {
		synchronized(protocolProviders) {
			protocolProviders.add(protocolProvider);
		}
	}
	
	public ProtocolProvider getProtocolProvider(String protocolName, String protocolVersion) {
		synchronized(protocolProviders) {
			for(ProtocolProvider proto: protocolProviders) {
				if(proto.getProtocolName().equals(protocolName)) {
					if(protocolVersion == null || protocolVersion.equals(proto.getProtocolVersion())) {
						return proto;
					}
				}
			}
		}
		return null;
	}
	
	public void removeProtocolProvider(ProtocolProvider protocolProvider) {
		synchronized(protocolProviders) {
			protocolProviders.remove(protocolProvider);
		}
	}
	
}
