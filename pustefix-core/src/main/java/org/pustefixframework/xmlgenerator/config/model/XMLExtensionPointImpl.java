package org.pustefixframework.xmlgenerator.config.model;

import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.extension.support.AbstractExtensionPoint;

/**
 * ExtensionPoint implementation which can return a list of configuration
 * elements provided by the registered extensions
 * 
 * @author mleidig@schlund.de
 *
 * @param <T1> configuration data object type
 */
public class XMLExtensionPointImpl<T1 extends ModelElement> extends AbstractExtensionPoint<XMLExtensionPointImpl<T1>,XMLExtension<T1>> implements XMLExtensionPoint<T1> {

	public List<T1> getElements() {
		List<T1> elements = new ArrayList<T1>();
		elements.clear();
		for(XMLExtension<T1> ext: getExtensions()) {
			List<T1> list = ext.getElements();
			elements.addAll(list);
		}
		return elements;
	}

}
