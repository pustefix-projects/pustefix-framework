package org.pustefixframework.maven.plugins.autoconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Base class of configuration fragment bundle creation implementations.
 * 
 * @author mleidig@schlund.de
 *
 */
public abstract class ConfigFragmentCreator {

	public abstract File createBundle(File targetDir, Map<String,String> params) throws IOException;
	public abstract File createBundle(File targetDir, File configFile, Map<String,String> params) throws IOException;
	
	protected void createManifest(OutputStream out, String bundleName, String bundleSymbolicName, 
			String bundleVersion, String fragmentHost) throws IOException {
		
	    println(out, "Manifest-Version: 1.0");
	    println(out, "Bundle-ClassPath: .");
		println(out, "Bundle-ManifestVersion: 2");
		println(out, "Bundle-Name: " + bundleName);
		println(out, "Bundle-RequiredExecutionEnvironment: J2SE-1.5");
		println(out, "Bundle-SymbolicName: " + bundleSymbolicName);
		println(out, "Bundle-Version: " + bundleVersion);
	    println(out, "Fragment-Host: " + fragmentHost);
	}
	
	protected void println(OutputStream out, String str) throws IOException {
		try {
			out.write((str + "\n").getBytes("UTF-8"));
		} catch(UnsupportedEncodingException x) {
			throw new RuntimeException("Illegal encoding", x);
		}
	}
	
	protected void copyFileToStream(File file, OutputStream out) throws IOException {
	     FileInputStream in = new FileInputStream(file);
	     byte[] buffer = new byte[4096];
	     int no = 0;
	     try {
	    	 while ((no = in.read(buffer)) != -1)
	    		 out.write(buffer, 0, no);
	     } finally {
	    	 in.close();
	     }
	}
	
}
