package org.pustefixframework.maven.plugins.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.MavenMetadataSource;


public class BundleResolver implements URIToFileResolver {
	
	protected URL[] provisioningConfigs;
    protected int defaultStartLevel;
    protected MavenProject mavenProject;
    protected ArtifactFactory artifactFactory;
    protected ArtifactResolver resolver;
    protected ArtifactMetadataSource metadataSource;
    protected ArtifactRepository localRepository;
    protected List<?> remoteRepositories;
    protected Log log;

    public BundleResolver(URL[] provisioningConfigs, int defaultStartLevel,
			MavenProject mavenProject, ArtifactFactory artifactFactory,
			ArtifactResolver resolver, ArtifactMetadataSource metadataSource,
			ArtifactRepository localRepository, List<?> remoteRepositories,
			Log log) {
    	
		this.provisioningConfigs = provisioningConfigs;
		this.defaultStartLevel = defaultStartLevel;
		this.mavenProject = mavenProject;
		this.artifactFactory = artifactFactory;
		this.resolver = resolver;
		this.metadataSource = metadataSource;
		this.localRepository = localRepository;
		this.remoteRepositories = remoteRepositories;
		this.log = log;
	}

	public List<BundleConfig> resolve() throws MojoExecutionException {

        List<BundleConfig> bundles = new ArrayList<BundleConfig>();
        String bundleSymbolicName = Utils.getBundleSymbolicNameFromProject(mavenProject.getBasedir());
        File bundleDir = new File(mavenProject.getBasedir(),"target/classes");
        BundleConfig bundle = new BundleConfig(bundleDir, bundleSymbolicName, true, defaultStartLevel);
        bundles.add(bundle);

    	Set<String> excludedBundles = new HashSet<String>();
    	for(URL provisioningConfig: provisioningConfigs) {
	            String line = null;
	            try {
	                BufferedReader reader = new BufferedReader(new InputStreamReader(provisioningConfig.openStream()));
	                while((line = reader.readLine()) != null) {
	                    line = line.trim();
	                    if(!(line.equals("")||line.startsWith("#"))) {
	                        if(line.startsWith("!")) {
	                            line = line.substring(1).trim();
	                            excludedBundles.add(line);
	                        } else if(line.startsWith("mvn:") ||  line.startsWith("file:")) {
	                        	String uriStr = line;
	                        	int level = defaultStartLevel;
	                        	int ind = line.indexOf('@');
	                        	if(ind > -1) {
	                        		uriStr = line.substring(0,ind);
	                        		level = Integer.parseInt(line.substring(ind+1));
	                        	}
	                            URI uri = new URI(uriStr);
	                            BundleConfig bundleConfig = getBundleConfig(uri, level);
	                            if(bundleConfig != null) bundles.add(bundleConfig);
	                        } else throw new MojoExecutionException("Unsupported provisioning config entry: " + line);
	                    }
	                }
	            } catch(URISyntaxException x) {
	                throw new MojoExecutionException("Illegal URI in provisioning configuration file '" +
	                        provisioningConfig.toExternalForm() + "': " + line, x);
	                
	            } catch(IOException x) {
	                throw new MojoExecutionException("Error while reading provisioning configuration from file '" + 
	                        provisioningConfig.toExternalForm() + "'.", x);
	            }
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
    			if(artifact.getScope().equals(Artifact.SCOPE_RUNTIME) || artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
    			    BundleConfig bundleConfig = getBundleConfig(artifact.getFile(), defaultStartLevel);
    			    if(bundleConfig != null && !excludedBundles.contains(bundleConfig.getBundleSymbolicName())) 
    			        bundles.add(bundleConfig);
    			}
    		}
    	} catch(Exception x) {
    		throw new MojoExecutionException("Error resolving artifact dependencies", x);
    	}
    	
    	return bundles;
    	
    }
    
    private BundleConfig getBundleConfig(URI uri, int startLevel) throws MojoExecutionException {
        File file = resolve(uri);
        if(!file.exists()) throw new MojoExecutionException("Bundle doesn't exist: " + uri.toASCIIString());
        return getBundleConfig(file, startLevel);
    }
    
    private BundleConfig getBundleConfig(File file, int startLevel) throws MojoExecutionException {
    	BundleConfig bundleConfig = null;
    	try {
	    	JarFile jarFile = new JarFile(file);
			Manifest manifest = jarFile.getManifest();
			if(manifest.getMainAttributes().getValue("Bundle-ManifestVersion") != null) {
				boolean startable = false;
				String fragmentHost = manifest.getMainAttributes().getValue("Fragment-Host");
				if(fragmentHost == null) startable = true;
				String bundleSymbolicName = Utils.getBundleSymbolicName(manifest);
				bundleConfig = new BundleConfig(file, bundleSymbolicName, startable, startLevel);
			} else {
				if(log.isDebugEnabled()) {
					log.debug("Artifact '" + file.getAbsolutePath() + "'" +
							" doesn't contain 'Bundle-ManifestVersion' MANIFEST.MF entry -> artifact will be ignored.");
				}
			}
    	} catch(IOException x) {
    		throw new MojoExecutionException("Can't read MANIFEST.MF of artifact '" + file.getAbsolutePath() + "'.", x);
    	}
		return bundleConfig;
    }
    
    public File resolve(URI uri) {
    	if(uri.getScheme().equals("mvn")) {
    		Artifact artifact = resolveMavenURI(uri);
    		return artifact.getFile();
    	} else if(uri.getScheme().equals("file")) {
    		if(!uri.getSchemeSpecificPart().startsWith("/")) {
    			URI base = mavenProject.getBasedir().toURI();
        		uri = base.resolve(uri.getSchemeSpecificPart());
    		}
    		File file = new File(uri);
    		return file;
    	} else throw new IllegalArgumentException("URI scheme not supported: " + uri.getScheme());
    }
    
    private Artifact resolveMavenURI(URI uri) {
        String path = uri.getSchemeSpecificPart();
    	String[] parts = path.split("/");
    	String groupId = parts[0];
    	String artifactId = parts[1];
    	String version = parts[2];
    	String type = parts[3];
    	VersionRange versionRange;
    	try {
    	//	versionRange = VersionRange.createFromVersion(version);
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
			
			if(latestVersion == null) {
				if(versionRange.getRestrictions().size() == 1) {
					Restriction restriction = (Restriction)versionRange.getRestrictions().get(0);
					//handle broken/missing maven-metadata -> try to resolve fix version
					if(restriction.getLowerBound()!=null && restriction.getLowerBound().equals(restriction.getUpperBound()) &&
							restriction.isLowerBoundInclusive() && restriction.isUpperBoundInclusive())
							latestVersion = restriction.getLowerBound();
				}
				if(latestVersion == null) {
				    latestVersion = versionRange.getRecommendedVersion();
				    if(latestVersion == null) throw new RuntimeException("Artifact not found: " + uri.toString() + " Version: " + versionRange);
				}
			}
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
