package org.pustefixframework.xmlgenerator.view;

import org.pustefixframework.extension.ExtensionPoint;


/**
 * Interface implemented by view extension points supportung pluggin in according view extensions.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface ViewExtensionPoint extends ExtensionPoint<ViewExtension> {

	public final static String TYPE = "xml.view";
	
}
