package org.pustefixframework.maven.plugins.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.pustefixframework.maven.plugins.launcher.BundleConfig;
import org.pustefixframework.maven.plugins.launcher.BundleResolver;
import org.pustefixframework.util.io.FileUtils;

/**
 * Goal which creates an Eclipse target definition and
 * a runtime configuration for the Pustefix application
 *
 * @goal configure
 * 
 * @execute phase="package"
 */
public class EclipseConfigurationMojo extends AbstractMojo {

	
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
     * @parameter default-value="equinox"
     */
    private String osgiRuntime;

	/**
     * @parameter default-value="${basedir}/provisioning.conf"
     */
    protected File provisioningConfig;
    
    /**
     * @parameter default-value=4
     */
    protected int defaultStartLevel;
    
    /**
     * @parameter default-value="${basedir}/.${project.artifactId}.target"
     * @required
     */
    private File targetDefinitionFile;
    
    /**
     * @parameter default-value="${project.artifactId}"
     * @required
     */
    private String targetName;
    
    /**
     * @parameter default-value="${basedir}/.${project.artifactId}.launch"
     * @required
     */
    private File launchConfigFile;
    
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		
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
		
    	//Create target definition
    	TargetDefinitionBuilder targetBuilder = new TargetDefinitionBuilder();
    	targetBuilder.build(targetName, targetDefinitionFile, bundles);
    	
    	//Create launch configuration
    	LaunchConfigurationBuilder runConfBuilder = new LaunchConfigurationBuilder();
    	runConfBuilder.build(launchConfigFile, bundles);
    	
    	//Create linked resource entry for META-INF in .project
    	ProjectConfigBuilder prjBuilder = new ProjectConfigBuilder();
    	prjBuilder.build(mavenProject.getBasedir());
    	
    	
    	
	}

	
}
