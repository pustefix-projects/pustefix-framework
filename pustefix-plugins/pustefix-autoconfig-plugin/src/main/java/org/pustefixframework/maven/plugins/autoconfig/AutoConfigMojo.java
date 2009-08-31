package org.pustefixframework.maven.plugins.autoconfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generates fragment bundles for runtime and logback configuration.
 * 
 * @goal generate-config-fragments
 * @phase package
 *
 * @author mleidig@schlund.de
 */
public class AutoConfigMojo extends AbstractMojo {
   
    /**
     * @parameter default-value="${project.build.directory}/generated-bundles"
     * @required
     */
    private File targetDir;
    
    /**
     * @parameter default-value="prod"
     */
    private String mode;
    
    /**
     * @parameter default-value="WARN"
     */
    private String logLevel;
    
    /**
     * @parameter
     */
    private File logbackConfig;
    
    /**
     * @parameter
     */
    private File runtimeConfig;
    
    public void execute() throws MojoExecutionException {
    	
    	if(!targetDir.exists()) {
    		targetDir.mkdirs();
    	}
    	
    	Map<String, String> params = new HashMap<String, String>();
    	params.put("mode", mode);
    	params.put("logLevel", logLevel);
    	
    	try {
    		RuntimeConfigFragmentCreator runtimeCreator = new RuntimeConfigFragmentCreator();
    		runtimeCreator.createBundle(targetDir, runtimeConfig, params);
    	} catch(IOException x) {
    		throw new MojoExecutionException("Error creating runtime config fragment" ,x);
    	}
    	
    	try {
    		LogbackConfigFragmentCreator logbackCreator = new LogbackConfigFragmentCreator();
    		logbackCreator.createBundle(targetDir, logbackConfig, params);
    	} catch(IOException x) {
    		throw new MojoExecutionException("Error creating logback config fragment", x);
    	}
    	
    }
}
