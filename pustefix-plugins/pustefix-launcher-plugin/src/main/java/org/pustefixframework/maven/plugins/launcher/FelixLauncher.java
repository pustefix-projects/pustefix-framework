package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class FelixLauncher implements Launcher {

	private static String FRAMEWORK_BUNDLE = "mvn:org.apache.felix/org.apache.felix.framework/[1.8.1,2.0.0)/bundle";
	private static String MAIN_BUNDLE = "mvn:org.apache.felix/org.apache.felix.main/[1.8.1,2.0.0)/bundle";
	private static String[] SHELL_BUNDLES = {
		"mvn:org.apache.felix/org.apache.felix.shell/[1.0.2,1.3.0)/bundle",
		"mvn:org.apache.felix/org.apache.felix.shell.tui/[1.0.2,1.3.0)/bundle"
	};
	
	public void configure(File launcherDirectory, List<BundleConfig> bundles, File framework) {
		
		StringBuilder config = new StringBuilder();
		
		config.append("felix.auto.start.1=");
		Iterator<BundleConfig> it = bundles.iterator();
		boolean first = true;
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		if(bundle.doStart()) {
    			if(first) first = false;
    			else config.append(" ");
    			config.append("reference:file:").append(bundle.getFile().getAbsolutePath());
    		}
		}
		config.append("\n");
    	
		config.append("felix.auto.install.1=");
		it = bundles.iterator();
		first = true;
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		if(!bundle.doStart()) {
    			if(first) first = false;
    			else config.append(" ");
    			config.append("reference:file:").append(bundle.getFile().getAbsolutePath());
    		}
		}
    	config.append("\n");
    	
    	config.append("org.osgi.framework.storage=");
    	config.append(new File(launcherDirectory, "felix-cache").getAbsolutePath());
    	config.append("\n");
    	
    	config.append("org.osgi.framework.storage.clean=onFirstInit\n");
    	
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
		
		File frameworkBundleFile;
		File mainBundleFile;
		
		try {
			frameworkBundleFile = resolver.resolve(new URI(FRAMEWORK_BUNDLE));
			mainBundleFile = resolver.resolve(new URI(MAIN_BUNDLE));
			for(String shellBundle:SHELL_BUNDLES) {
				File file = resolver.resolve(new URI(shellBundle));
				String name = Utils.getBundleSymbolicNameFromJar(file);
				bundles.add(0, new BundleConfig(file, name, true, defaultStartLevel));
			}
		} catch(URISyntaxException x) {
			throw new RuntimeException("Illegal bundle URI", x);
		}
	
		configure(launcherDirectory, bundles, frameworkBundleFile);
		
		try {
			
			File configFile = new File(launcherDirectory,"config.properties");
    		
    		Properties props = new Properties();
    		props.load(new FileInputStream(configFile));
			
    		URL[] urls = {frameworkBundleFile.toURI().toURL(), mainBundleFile.toURI().toURL()};
    		URLClassLoader cl = new URLClassLoader(urls);
    		
    		List<Object> list = new ArrayList<Object>();
    		Class<?> activatorClass = cl.loadClass("org.apache.felix.main.AutoActivator");
    		Constructor<?> con = activatorClass.getConstructor(Map.class);
    		list.add(con.newInstance(props));
    		props.put("felix.systembundle.activators", list);
    		Class<?> starterClass = cl.loadClass("org.apache.felix.framework.Felix");
    		con = starterClass.getConstructor(Map.class);
    		Object obj = con.newInstance(props);
    		Method meth = starterClass.getMethod("start");
    		meth.invoke(obj);
    		meth = starterClass.getMethod("waitForStop", long.class);
    		meth.invoke(obj, 1000);
    		
    	} catch(Exception x) {
    		throw new RuntimeException("Error launching Felix OSGi runtime", x);
    	}
		
	}

}
