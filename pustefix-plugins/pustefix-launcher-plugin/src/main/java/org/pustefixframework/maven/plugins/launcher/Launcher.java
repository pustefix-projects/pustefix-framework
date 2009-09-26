package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.util.List;

/**
 * Interface implemented by OSGi runtime launchers.
 *
 * @author mleidig@schlund.de
 *
 */
public interface Launcher {

	/**
	 * Configure and start the OSGi runtime.
	 *
	 * @param bundles Bundles to be installed/started
	 * @param launcherDirectory Location to store data
	 * @param resolver Resolver for required additional artifacts
     * @param defaultStartLevel
     * @param httpPort Servlet container http port
	 */
	public void launch(List<BundleConfig> bundles, File launcherDirectory, URIToFileResolver resolver, int defaultStartLevel, int httpPort);

}
