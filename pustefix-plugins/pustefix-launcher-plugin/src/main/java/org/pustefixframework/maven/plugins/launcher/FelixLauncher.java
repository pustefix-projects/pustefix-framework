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

public class FelixLauncher implements Launcher {
	
	private static String FRAMEWORK_BUNDLE_SYMBOLIC_NAME = "org.apache.felix.framework";
	private static String MAIN_BUNDLE_SYMBOLIC_NAME = "org.apache.felix.main";
	
	public void configure(File launcherDirectory, List<BundleConfig> bundles, File framework, int httpPort) {
		
		StringBuilder config = new StringBuilder();
		
		config.append("felix.auto.start.1=");
		Iterator<BundleConfig> it = bundles.iterator();
		boolean first = true;
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		if(!(bundle.getBundleSymbolicName().equals(FRAMEWORK_BUNDLE_SYMBOLIC_NAME)||
    				bundle.getBundleSymbolicName().equals(MAIN_BUNDLE_SYMBOLIC_NAME))) {
    			if(bundle.doStart()) {
    				if(first) first = false;
    				else config.append(" ");
    				config.append("reference:file:").append(bundle.getFile().getAbsolutePath());
    			}
    		}
		}
		config.append("\n");
    	
		config.append("felix.auto.install.1=");
		it = bundles.iterator();
		first = true;
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		if(!(bundle.getBundleSymbolicName().equals(FRAMEWORK_BUNDLE_SYMBOLIC_NAME)||
    				bundle.getBundleSymbolicName().equals(MAIN_BUNDLE_SYMBOLIC_NAME))) {
    			if(!bundle.doStart()) {
    				if(first) first = false;
    				else config.append(" ");
    				config.append("reference:file:").append(bundle.getFile().getAbsolutePath());
    			}
    		}
		}
    	config.append("\n");
    	
    	config.append("org.osgi.framework.storage=");
    	config.append(new File(launcherDirectory, "felix-cache").getAbsolutePath());
    	config.append("\n");
    	
    	config.append("org.osgi.framework.storage.clean=onFirstInit\n");
    	
    	config.append("org.osgi.service.http.port=").append(httpPort).append("\n");
    	
    	try {
    		File configFile = new File(launcherDirectory,"config.properties");
    		FileOutputStream out = new FileOutputStream(configFile);
    		out.write(config.toString().getBytes("UTF-8"));
    		out.close();
    	} catch(IOException x) {
			throw new RuntimeException("Error creating Felix configuration", x);
		}
	}

	public void launch(List<BundleConfig> bundles, File launcherDirectory, URIToFileResolver resolver, int defaultStartLevel, int httpPort) {
		
		File frameworkBundleFile = null;
		File mainBundleFile = null;
	
		for(BundleConfig bundle: bundles) {
			if(bundle.getBundleSymbolicName().equals(FRAMEWORK_BUNDLE_SYMBOLIC_NAME)) {
				frameworkBundleFile = bundle.getFile();
			} else if(bundle.getBundleSymbolicName().equals(MAIN_BUNDLE_SYMBOLIC_NAME)) {
				mainBundleFile = bundle.getFile();
			}
		}
		if(frameworkBundleFile == null) throw new RuntimeException("No Felix framework bundle found.");
		if(mainBundleFile == null) throw new RuntimeException("No Felix main bundle found.");
		
		configure(launcherDirectory, bundles, frameworkBundleFile, httpPort);
		
		try {
			
			File configFile = new File(launcherDirectory,"config.properties");
    		
    		Properties props = new Properties();
    		props.load(new FileInputStream(configFile));
			
    		URL[] urls = {frameworkBundleFile.toURI().toURL(), mainBundleFile.toURI().toURL()};
    		URLClassLoader cl = new URLClassLoader(urls);
    		
    		Class<?> frameworkFactoryClass = cl.loadClass("org.apache.felix.framework.FrameworkFactory");
    		Object frameworkFactory = frameworkFactoryClass.newInstance();
    		Method meth = frameworkFactoryClass.getMethod("newFramework", Map.class);
    		Object framework = meth.invoke(frameworkFactory, props);
    		meth = framework.getClass().getMethod("init");
    		meth.invoke(framework);
    		
    		Class<?> bundleItf = getInterface(framework.getClass(), "org.osgi.framework.Bundle");
    		meth = bundleItf.getMethod("getBundleContext", new Class[0]);
    		
    		Object bundleContext = meth.invoke(framework); 
    		Class<?> bundleContextItf = getInterface(bundleContext.getClass(), "org.osgi.framework.BundleContext");
    		
    		Class<?> autoClass = cl.loadClass("org.apache.felix.main.AutoProcessor");
    		meth = autoClass.getMethod("process", Map.class, bundleContextItf);
    		meth.invoke(null, props, bundleContext);
    		
    		meth = framework.getClass().getMethod("start");
    		meth.invoke(framework);
    		meth = framework.getClass().getMethod("waitForStop", long.class);
    		meth.invoke(framework, 0);
    		
    	} catch(Exception x) {
    		throw new RuntimeException("Error launching Felix OSGi runtime", x);
    	}
		
	}

	private Class<?> getInterface(Class<?> clazz, String interfaceName) {
		Class<?>[] itfs = clazz.getInterfaces();
		for(Class<?> itf: itfs) {
			if(itf.getName().equals(interfaceName)) return itf;
			else {
				Class<?> supItf = getInterface(itf, interfaceName);
				if(supItf != null) return supItf;
			}
		}
		return null;
	}
	
}
