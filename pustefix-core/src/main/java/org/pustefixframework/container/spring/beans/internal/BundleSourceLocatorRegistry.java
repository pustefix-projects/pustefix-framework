package org.pustefixframework.container.spring.beans.internal;

import java.io.File;

/**
 * Registry holding all registered BundleSourceLocators.
 * Returns the source location of a bundle by requesting the registered locators.
 * 
 * @author mleidig@schlund.de
 *
 */
public interface BundleSourceLocatorRegistry {

	public File getSourceLocation(String bundleSymbolicName, String bundleVersion);
	
}
