/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.maven.plugins;

import java.io.File;
import java.net.URL;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Runs the exploded WAR in an embedded Tomcat container.
 *
 * @goal run-war
 * @execute phase=package
 * @threadSafe
 */
public class TomcatMojo extends AbstractMojo {

	/**
	 * The exploded WAR directory.
	 *
	 * @parameter default-value = "${project.build.directory}/${project.build.finalName}"
	 */
	private File warDir;

	/**
	 * The Tomcat http connector port.
	 *
	 * @parameter default-value = "8080"
	 */
	private int httpPort;

	/**
	 * The Tomcat https connector port.
	 *
	 * @parameter default-value = "8443"
	 */
	private int httpsPort;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir("target/tomcat");
		tomcat.setPort(httpPort);

		Connector httpConnector = tomcat.getConnector();
		httpConnector.setURIEncoding("UTF-8");
		httpConnector.setRedirectPort(httpsPort);

		Connector httpsConnector = new Connector();
		httpsConnector.setURIEncoding("UTF-8");
		httpsConnector.setPort(httpsPort);
		httpsConnector.setSecure(true);
		httpsConnector.setScheme("https");
		httpsConnector.setAttribute("keyAlias", "dummy");
		httpsConnector.setAttribute("keystorePass", "passphrase");
		URL keyStoreURL = getClass().getClassLoader().getResource("keystore");
		httpsConnector.setAttribute("keystoreFile", keyStoreURL.toExternalForm());
		httpsConnector.setAttribute("clientAuth", "false");
		httpsConnector.setAttribute("sslProtocol", "TLS");
		httpsConnector.setAttribute("SSLEnabled", true);
		tomcat.getService().addConnector(httpsConnector);

		Context context = tomcat.addWebapp("/", warDir.getAbsolutePath());
		WebappLoader loader = new WebappLoader(Thread.currentThread().getContextClassLoader());
		context.setLoader(loader);
		context.addParameter("pustefix.https.port", String.valueOf(httpsPort));

		try {
			tomcat.start();
		} catch (Exception x) {
			throw new RuntimeException("Error starting Tomcat", x);
		}

		Object lock = new Object();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException x) {
				getLog().warn("Running Tomcat was interrupted", x);
			}
		}
	}

}
