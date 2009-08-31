package org.pustefixframework.maven.plugins.autoconfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Generate runtime configuration fragment.
 * 
 * @author mleidig@schlund.de
 *
 */
public class RuntimeConfigFragmentCreator extends ConfigFragmentCreator {

	@Override
	public File createBundle(File targetDir, Map<String,String> params) throws IOException {
		return createBundle(targetDir, null);
	}
	
	@Override
	public File createBundle(File targetDir, File configFile, Map<String,String> params) throws IOException {
		File file = new File(targetDir, "org.pustefixframework.runtime.config-1.0.0.jar");
		OutputStream out = new FileOutputStream(file);
		createRuntimeConfigBundle(out, configFile, params);
		return file;
	}
	
	private void createRuntimeConfigBundle(OutputStream out, File configFile, Map<String,String> params) throws IOException {
		
		 JarOutputStream jarOut = new JarOutputStream(out);
		 JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
		 jarOut.putNextEntry(entry);
		 createManifest(jarOut, "Runtime Config", "org.pustefixframework.runtime.config", "1.0.0", "org.pustefixframework.pustefix-core");
		 entry = new JarEntry("META-INF/pustefix/runtime.properties");
		 jarOut.putNextEntry(entry);
		 if(configFile == null) createRuntimeConfig(jarOut, params);
		 else copyFileToStream(configFile, jarOut);
		 jarOut.close();
		
	}
	
	private void createRuntimeConfig(OutputStream out, Map<String,String> params) throws IOException {
		Properties props = new Properties();
		String mode = params.get("mode");
		if(mode == null || mode.trim().equals("")) mode = "prod";
		props.put("mode", mode);
		props.store(out, "Auto-generated runtime properties");
	}
	
}
