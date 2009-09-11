package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;

public class EquinoxLauncher implements Launcher {
	
	private static String FRAMEWORK_BUNDLE_SYMBOLIC_NAME = "org.eclipse.osgi";
	
	private void configure(File launcherDirectory, List<BundleConfig> bundles, File framework, int defaultStartLevel) {
		createConfigProperties(launcherDirectory, bundles, framework, defaultStartLevel);
		createDevProperties(launcherDirectory, bundles);
	}
	
	private void createConfigProperties(File launcherDirectory, List<BundleConfig> bundles, File framework, int defaultStartLevel) {
		StringBuilder config = new StringBuilder();
    	
    	config.append("org.osgi.supports.framework.extension=true\n");
    	config.append("org.osgi.supports.framework.fragment=true\n");
    	config.append("org.osgi.supports.framework.requirebundle=true\n");	
	
    	config.append("osgi.bundles=");
    	Iterator<BundleConfig> it = bundles.iterator();
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		if(!bundle.getBundleSymbolicName().equals(FRAMEWORK_BUNDLE_SYMBOLIC_NAME)) {
				config.append("reference:file:").append(bundle.getFile().getAbsolutePath());
				
				if(bundle.doStart()) {
				    if(bundle.getStartLevel()==4) config.append("@start");
				    else config.append("@"+bundle.getStartLevel()+":start");
				}
				
				if(it.hasNext()) config.append(",");
    		}
    		
		}
		config.append("\n");
    	
    	config.append("osgi.bundles.defaultStartLevel=4\n");
    	config.append("osgi.clean=true\n");
    	config.append("osgi.configuration.area=file:").append(launcherDirectory.getAbsolutePath()).append("\n");
    	config.append("osgi.console=\n");
    	//config.append("osgi.debug=").append(launcherDirectory.getAbsolutePath()).append("/.options\n");
    	config.append("osgi.dev=file:").append(launcherDirectory.getAbsolutePath()).append("/dev.properties\n");
    	
    	
    	
    	config.append("osgi.framework=").append(framework.toURI().toString()).append("\n");
    	config.append("osgi.install.area=file:").append(launcherDirectory.getAbsolutePath()).append("\n");
    	config.append("osgi.noShutdown=true\n");
    	
    	config.append("org.osgi.service.http.port=8080\n");
    	
    	try {
    		File configFile = new File(launcherDirectory,"config.ini");
    		FileOutputStream out = new FileOutputStream(configFile);
    		out.write(config.toString().getBytes("UTF-8"));
    		out.close();
    	} catch(IOException x) {
			throw new RuntimeException("Error creating Equinox configuration", x);
		}
	}
	
	private void createDevProperties(File launcherDirectory, List<BundleConfig> bundles) {
		Properties props = new Properties();
		for(BundleConfig bundle:bundles) {
			if(bundle.getFile().isDirectory()) {
				File manifestFile = new File(bundle.getFile(), "META-INF/MANIFEST.MF");
				if(!manifestFile.exists()) 
					throw new RuntimeException("Missing manifest file: " + manifestFile.getAbsolutePath());
				try {
					Manifest manifest = new Manifest(new FileInputStream(manifestFile));
					String bundleName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
					if(bundleName==null) 
						throw new RuntimeException("Missing Bundle-SymbolicName entry in manifest: " + manifestFile.getAbsolutePath());
					//props.setProperty(bundleName, "target/classes");
				} catch(IOException x) {
					throw new RuntimeException("Error reading manifest file: " + manifestFile.getAbsolutePath());
				}
			}
		}
		props.setProperty("@ignoredot@", "true");
		File devPropFile = new File(launcherDirectory, "dev.properties");
		try {
			FileOutputStream out = new FileOutputStream(devPropFile);
			props.store(out, "Eclipse dev mode properties");
			out.close();
		} catch(IOException x) {
			throw new RuntimeException("Error creating Equinox configuration", x);
		}
	}

	
	public void launch(List<BundleConfig> bundles, File launcherDirectory, URIToFileResolver resolver, int defaultStartLevel) {
		
		File framework = null;
		for(BundleConfig bundle: bundles) {
			if(bundle.getBundleSymbolicName().equals("org.eclipse.osgi")) {
				framework = bundle.getFile();
				break;
			}
		}
		if(framework == null) throw new RuntimeException("No Equinox framework bundle found.");
		
		configure(launcherDirectory, bundles, framework, defaultStartLevel);
    	try {
    		File configFile = new File(launcherDirectory,"config.ini");
    		
    		Properties props = new Properties();
    		props.load(new FileInputStream(configFile));
    		
    		URL[] urls = {framework.toURI().toURL()};
    		URLClassLoader cl = new URLClassLoader(urls);
    		Class<?> starterClass = cl.loadClass("org.eclipse.core.runtime.adaptor.EclipseStarter");
    		Method meth = starterClass.getMethod("setInitialProperties", Map.class);
    		meth.invoke(null, props);
    		meth = starterClass.getMethod("startup", String[].class, Runnable.class);
    		meth.invoke(null, new String[] {}, null);
    		
			while(true) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException x) {
				
				}
			}
    	} catch(Exception x) {
    		throw new RuntimeException("Error launching Equinox OSGI-runtime", x);
    	}
	}
	
	    
}
