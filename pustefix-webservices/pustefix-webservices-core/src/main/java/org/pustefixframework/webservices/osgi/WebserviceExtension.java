package org.pustefixframework.webservices.osgi;

import java.util.List;

import org.pustefixframework.extension.Extension;
import org.pustefixframework.webservices.spring.WebserviceRegistration;

public interface WebserviceExtension extends Extension {

	public List<WebserviceRegistration> getWebserviceRegistrations();
	
}
