package org.pustefixframework.webservices.osgi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.pustefixframework.extension.support.AbstractExtension;
import org.pustefixframework.extension.support.ExtensionPointRegistrationListener;
import org.pustefixframework.webservices.spring.WebserviceRegistration;

public class WebserviceExtensionImpl extends AbstractExtension<WebserviceExtensionPoint,WebserviceExtensionImpl> implements WebserviceExtension {

	private List<WebserviceExtensionPointImpl> extensionPointList;
	private List<WebserviceRegistration> webserviceRegistrationList;
	
	private ExtensionPointRegistrationListener<WebserviceExtensionPointImpl,WebserviceExtension> registrationListener = createRegistrationListener();
	
	private List<WebserviceRegistration> cachedWebserviceRegistrations;
	
	
	public void setExtensionPoints(List<WebserviceExtensionPointImpl> list) {
		extensionPointList = new ArrayList<WebserviceExtensionPointImpl>();
		Iterator<WebserviceExtensionPointImpl> it = list.iterator();
		while(it.hasNext()) {
			WebserviceExtensionPointImpl ext = it.next();
			ext.registerListener(registrationListener);
			extensionPointList.add(ext);
		}
	}
	
	public void setWebserviceRegistrations(List<WebserviceRegistration> list) {
		webserviceRegistrationList = new ArrayList<WebserviceRegistration>();
		Iterator<WebserviceRegistration> it = list.iterator();
		while(it.hasNext()) {
			WebserviceRegistration reg = it.next();
			webserviceRegistrationList.add(reg);
		}
	}
	
	public List<WebserviceRegistration> getWebserviceRegistrations() {
		if(cachedWebserviceRegistrations == null) {
			rebuild();
		}
		return cachedWebserviceRegistrations;
	}
	
	private void rebuild() {
		synchronized(registrationLock) {
			cachedWebserviceRegistrations = new ArrayList<WebserviceRegistration>();;
			cachedWebserviceRegistrations.addAll(webserviceRegistrationList);
			for(WebserviceExtensionPointImpl extensionPoint: extensionPointList) {
				for(WebserviceExtension extension:extensionPoint.getExtensions()) {
					cachedWebserviceRegistrations.addAll(extension.getWebserviceRegistrations());
				}
			}
			for(WebserviceExtensionPoint extensionPoint: extensionPoints) {
				((WebserviceExtensionPointImpl)extensionPoint).updateExtension(this);
			}
		}
	}
	
	private ExtensionPointRegistrationListener<WebserviceExtensionPointImpl, WebserviceExtension> createRegistrationListener() {
		return new ExtensionPointRegistrationListener<WebserviceExtensionPointImpl, WebserviceExtension>() {
			@Override
			public void afterRegisterExtension(WebserviceExtensionPointImpl extensionPoint, WebserviceExtension extension) {
				rebuild();
			}
			@Override
			public void afterUnregisterExtension(WebserviceExtensionPointImpl extensionPoint, WebserviceExtension extension) {
				rebuild();
			}
			@Override
			public void updateExtension(WebserviceExtensionPointImpl extensionPoint, WebserviceExtension extension) {
				rebuild();
			}
		};
	}
	
}
