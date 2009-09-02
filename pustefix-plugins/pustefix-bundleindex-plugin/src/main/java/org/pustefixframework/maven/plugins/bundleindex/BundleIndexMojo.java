package org.pustefixframework.maven.plugins.bundleindex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.Manifest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Updates the bundle index file.
 * 
 * @goal update-bundle-index
 * @phase package
 *
 * @author mleidig@schlund.de
 */
public class BundleIndexMojo extends AbstractMojo {
   
    /**
     * @parameter default-value="${user.home}/.m2/.bundle-index"
     * @required
     */
    private File bundleIndex;
    
    /**
     * @parameter default-value=3600
     * @required
     */
    private long cleanInterval;
    
    /**
     * @parameter default-value="${project.build.directory}/classes/META-INF/MANIFEST.MF"
     * @required
     */
    private File manifest;
    
    /** @parameter expression="${basedir}" */
    private File bundleDir;
    
    public void execute() throws MojoExecutionException {
    	
    	if(!manifest.exists()) {
    		getLog().warn("Manifest file '" + manifest.getAbsolutePath() + "' doesn't exist. Can't update bundle index.");
    	} else {
    		Manifest man;
    		try {
    			FileInputStream in = new FileInputStream(manifest);
    			man = new Manifest(in);
    		} catch(IOException x) {
    			throw new MojoExecutionException("Can't read manifest file '" + manifest.getAbsolutePath() + "'" ,x);
    		}
    		String bundleSymbolicName = man.getMainAttributes().getValue("Bundle-SymbolicName");
    		if(bundleSymbolicName == null) {
    			getLog().warn("Manifest file '" + manifest.getAbsolutePath() + "' doesn't contain Bundle-SymblicName " +
    					"entry. Can't update bundle index.");
    		} else {
    			String bundleVersion = man.getMainAttributes().getValue("Bundle-Version");
    			if(bundleVersion == null) {
    				getLog().warn("Manifest file '" + manifest.getAbsolutePath() + "' doesn't contain Bundle-Version " +
					"entry. Can't update bundle index.");
    			} else {
    				boolean clean = false;
    				if((System.currentTimeMillis() - manifest.lastModified()) > (cleanInterval * 1000)) clean = true; 
    				try {
    					updateBundleIndex(bundleSymbolicName, bundleVersion, clean);
    				} catch(IOException x) {
    					throw new MojoExecutionException("Updating bundle index failed", x);
    				}
    			}
    		}
    	}
    }
    
    private void updateBundleIndex(String bundleSymbolicName, String bundleVersion, boolean clean) throws IOException {
    
		Properties props = new Properties();
		RandomAccessFile raf = new RandomAccessFile(bundleIndex, "rw");
		FileLock lock = raf.getChannel().lock();
		try {
			FileInputStream in = new FileInputStream(raf.getFD());
			props.load(in);
			
			List<String> removableKeys = new ArrayList<String>();
			
			if(clean) {	
				Enumeration<?> keys = (Enumeration<?>)props.propertyNames();
				while(keys.hasMoreElements()) {
					String key = (String)keys.nextElement();
					String value = props.getProperty(key);
					if(value == null || value.trim().equals("") || key.trim().equals("") ) {
						removableKeys.add(key);
					} else {
						String location = value.trim();
						File bundleDir;
						if(location.startsWith("/") || bundleIndex.getParentFile() == null) bundleDir = new File(location);
						else bundleDir = new File(bundleIndex.getParentFile(), location);
						if(!bundleDir.exists()) removableKeys.add(key);
					}
				}
			}
			
			String path = bundleDir.getAbsolutePath();
			if(bundleIndex.getParentFile() != null) {
				String parentPath = bundleIndex.getParentFile().getAbsolutePath();
				if(path.startsWith(parentPath)) {
					path = path.substring(parentPath.length() + 1);
				}
			}
			
			//Remove old entries referencing the same path
			Enumeration<?> keys = (Enumeration<?>)props.propertyNames();
			while(keys.hasMoreElements()) {
				String key = ((String)keys.nextElement()).trim();
				String value = props.getProperty(key).trim();
				if(path.equals(value) && !key.equals(bundleSymbolicName + "@" + bundleVersion)) removableKeys.add(key);
			}
			for(String key: removableKeys) props.remove(key);
		
			boolean changed = (removableKeys.size() > 0);
			String oldVal = props.getProperty(bundleSymbolicName + "@" + bundleVersion);
			if(oldVal == null || !path.equals(oldVal)) {
				props.setProperty(bundleSymbolicName + "@" + bundleVersion, path);
				changed = true;
			}
			
			if(changed) {
				FileOutputStream out = new FileOutputStream(raf.getFD());
				raf.seek(0);
				raf.setLength(0);
				props.store(out, "Bundle index file");
				getLog().info("Updating bundle index " + bundleIndex.getAbsolutePath());
			}
			
		} finally {
			lock.release();
		}
		
	}
    
    
    
    
    
    
    
}
