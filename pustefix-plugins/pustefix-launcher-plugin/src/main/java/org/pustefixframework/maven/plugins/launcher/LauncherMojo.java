package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which launches an OSGi runtime including the provisioning of bundles
 * resolved from the project's POM or a configuration file.
 *
 * @goal run
 * 
 * @execute phase="package"
 * 
 */
public class LauncherMojo extends AbstractMojo {
    
	
    /** @parameter expression="${project}" */
    protected MavenProject mavenProject;
    
    /** @component */
    protected ArtifactFactory artifactFactory;

    /** @component */
    protected ArtifactResolver resolver;

    /** @component */
    protected ArtifactMetadataSource metadataSource;

    /**@parameter expression="${localRepository}" */
    protected ArtifactRepository localRepository;

    /** @parameter expression="${project.remoteArtifactRepositories}" */
    protected List<ArtifactRepository> remoteRepositories;

    
	/**
     * @parameter default-value="${basedir}/provisioning.conf"
     */
    protected File provisioningConfig;
    
    /**
     * @parameter default-value=4
     */
    protected int defaultStartLevel;
	
    /**
     * @parameter default-value=8080
     */
    protected int httpPort;
    
    /**
     * @parameter default-value="${project.build.directory}/launcher"
     * @required
     */
    private File launcherDirectory;
    
    /**
     * @parameter default-value="equinox"
     */
    private String osgiRuntime;
    
    private Map<String, Launcher> launchers;
    private Launcher launcher;
    
    
    public LauncherMojo() {
    	launchers = new HashMap<String, Launcher>();
    	launchers.put("equinox", new EquinoxLauncher());
    	launchers.put("felix" , new FelixLauncher());
    }
    
    
    public void execute() throws MojoExecutionException {
    	
    	launcher = launchers.get(osgiRuntime);
    	if(launcher == null) {
    		throw new MojoExecutionException("OSGi runtime not supported: " + osgiRuntime);
    	}
    	
    	if(!launcherDirectory.exists()) launcherDirectory.mkdir();
    	
    	URL frameworkConfig = getClass().getResource("/META-INF/provisioning_" + osgiRuntime + ".conf");
    	
    	URL[] provisioningConfigs;
		try {
			provisioningConfigs = new URL[] {frameworkConfig, provisioningConfig.toURI().toURL()};
		} catch (MalformedURLException x) {
			throw new MojoExecutionException("Illegal provisioning configuration URL", x);
		}
    	
    	BundleResolver bundleResolver = new BundleResolver(provisioningConfigs, defaultStartLevel,
    			mavenProject, artifactFactory, resolver, metadataSource, localRepository, remoteRepositories, getLog());
    	
    	List<BundleConfig> bundles = bundleResolver.resolve();
    	
    	launcher.launch(bundles, launcherDirectory, bundleResolver, defaultStartLevel, httpPort);
    }
	
}
