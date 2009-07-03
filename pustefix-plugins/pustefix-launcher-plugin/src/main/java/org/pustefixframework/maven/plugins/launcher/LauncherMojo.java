package org.pustefixframework.maven.plugins.launcher;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;

/**
 * Goal which launches an OSGi runtime including the provisioning of bundles
 * resolved from the project's POM or a configuration file.
 *
 * @goal run
 */
public class LauncherMojo extends AbstractMojo implements URIToFileResolver {
	
    /**
     * @parameter default-value="${project.build.directory}/launcher"
     * @required
     */
    private File launcherDirectory;
    
    /**
     * @parameter default-value="equinox"
     */
    private String osgiRuntime;
    
    /** @parameter expression="${project}" */
    private MavenProject mavenProject;
    
    /** @component */
    private ArtifactFactory artifactFactory;

    /** @component */
    private ArtifactResolver resolver;

    /** @component */
    private ArtifactMetadataSource metadataSource;

    /**@parameter expression="${localRepository}" */
    private ArtifactRepository localRepository;

    /** @parameter expression="${project.remoteArtifactRepositories}" */
    private List<?> remoteRepositories;

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
    	
    	List<BundleConfig> bundles = new ArrayList<BundleConfig>();
    	
    	
    	if(mavenProject.getPackaging().equals("bundle")) {
    		BundleConfig bundle = new BundleConfig(mavenProject.getBasedir(), true);
    		bundles.add(bundle);
    	}
    	
    	try {
    		List<?> list=mavenProject.getDependencies();
    		Set<?> dependencyArtifacts = MavenMetadataSource.createArtifacts( artifactFactory, list, null, null, null );
    		Artifact pomArtifact = mavenProject.getArtifact();
    	
    		ArtifactResolutionResult result = resolver.resolveTransitively(dependencyArtifacts, pomArtifact, 
    			Collections.EMPTY_MAP, localRepository, remoteRepositories, metadataSource, null, Collections.EMPTY_LIST);
    		Set<?> resolved = result.getArtifacts();
    		Iterator<?> it = resolved.iterator();
    		while(it.hasNext()) {
    			Artifact artifact = (Artifact)it.next();
    			if(!artifact.getArtifactId().equals("com.springsource.slf4j.jcl")) {
    			if(artifact.getScope().equals(Artifact.SCOPE_RUNTIME) || artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
    				JarFile jarFile = new JarFile(artifact.getFile());
    				Manifest m=jarFile.getManifest();
    				boolean startable = false;
    				String fragmentHost = m.getMainAttributes().getValue("Fragment-Host");
    				if(fragmentHost == null) {
    					String bundleActivator = m.getMainAttributes().getValue("Bundle-Activator");
    					if(bundleActivator != null) startable = true;
    				}
    				BundleConfig bundle = new BundleConfig(artifact.getFile(), startable);
    				bundles.add(bundle);
    			}
    			}
    		}
    	
    	} catch(Exception x) {
    		x.printStackTrace();
    	}
       
    
    	launcher.launch(bundles, launcherDirectory, this);
    	
    }
    
    public File resolve(URI uri) {
    	if(uri.getScheme().equals("mvn")) {
    		Artifact artifact = resolveMavenURI(uri);
    		return artifact.getFile();
    	} 
    	return null;
    }
    
    private Artifact resolveMavenURI(URI uri) {
    	String[] parts = uri.getSchemeSpecificPart().split("/");
    	String groupId = parts[0];
    	String artifactId = parts[1];
    	String version = parts[2];
    	String type = parts[3];
    	VersionRange versionRange;
    	try {
    		versionRange = VersionRange.createFromVersionSpec(version);
    	} catch(InvalidVersionSpecificationException x) {
    		throw new RuntimeException("Illegal OSGi runtime version spec: " + version, x);
    	}
    	Artifact artifact = artifactFactory.createPluginArtifact(groupId, artifactId, versionRange);
    	try {
			List<?> versions = metadataSource.retrieveAvailableVersions(artifact, localRepository, remoteRepositories);
			ArtifactVersion latestVersion = null;
			Iterator<?> it = versions.iterator();
			while(it.hasNext()) {
				ArtifactVersion artifactVersion = (ArtifactVersion)it.next();
				if(versionRange.containsVersion(artifactVersion)) {
					if(latestVersion == null) latestVersion = artifactVersion;
					else if(compareVersion(latestVersion, artifactVersion) >0) latestVersion = artifactVersion;
				}
			}
			if(latestVersion == null) throw new RuntimeException("Artifact not found: " + uri.toString());
			artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, latestVersion.toString(), type, null);
    	} catch (ArtifactMetadataRetrievalException x) {
			throw new RuntimeException("Error while checking available versions: " + uri.toString(), x);
		}
    	try {
    		resolver.resolve(artifact, remoteRepositories, localRepository);
    	} catch (AbstractArtifactResolutionException x) {
    		throw new RuntimeException("Can't resolve Maven artifact: " + uri.toString(), x);
    	}
    	return artifact;
    }


	@SuppressWarnings("unchecked")
	private int compareVersion(ArtifactVersion latestVersion, ArtifactVersion artifactVersion) {
		return artifactVersion.compareTo(latestVersion);
	}
    
}
