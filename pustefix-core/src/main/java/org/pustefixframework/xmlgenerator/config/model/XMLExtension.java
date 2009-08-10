package org.pustefixframework.xmlgenerator.config.model;

import java.util.List;

import org.pustefixframework.extension.support.AbstractExtension;

/**
 * Extension implementation, which can hold a mixed list of configuration data
 * objects and according ExtensionPoints.
 * 
 * @author mleidig@schlund.de
 *
 * @param <T1> configuration data object type
 */
public class XMLExtension<T1 extends ModelElement> extends AbstractExtension<XMLExtensionPointImpl<T1>,XMLExtension<T1>> implements ModelChangeListener {

	private ExtensibleList<T1> extensibleList;
	
	public XMLExtension() {
		extensibleList = new ExtensibleList<T1>();
		extensibleList.addModelChangeListener(this);
	}
	
	public List<T1> getElements() {
		return extensibleList;
	}
	
	public void add(T1 element) {
		extensibleList.add(element);
	}
	
	public void addExtensionPoint(XMLExtensionPointImpl<T1> extensionPoint) {
		extensibleList.addExtensionPoint(extensionPoint);
	}
	
	public void modelChanged(ModelChangeEvent event) {
		 synchronized (registrationLock) {
             for (XMLExtensionPointImpl<T1> extensionPoint : extensionPoints) {
                 extensionPoint.updateExtension(this);
             }
         }
	}
	
}
