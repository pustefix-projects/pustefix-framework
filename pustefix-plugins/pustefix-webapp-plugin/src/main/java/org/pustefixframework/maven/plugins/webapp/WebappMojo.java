package org.pustefixframework.maven.plugins.webapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.pustefixframework.maven.plugins.launcher.BundleConfig;
import org.pustefixframework.maven.plugins.launcher.BundleResolver;
import org.pustefixframework.maven.plugins.launcher.FelixLauncher;
import org.pustefixframework.util.io.FileUtils;

/**
 * Goal which launches an OSGi runtime including the provisioning of bundles
 * resolved from the project's POM or a configuration file.
 *
 * @goal create
 * 
 * @execute phase="package"
 * 
 */
public class WebappMojo extends AbstractMojo {
    
	private static String FRAMEWORK_BUNDLE_SYMBOLIC_NAME = "org.eclipse.osgi";
	private static final String JETTY_BUNDLE_SYMBOLIC_NAME = "org.apache.felix.http.jetty";

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
     * @parameter default-value="${basedir}/target/${project.artifactId}-${project.version}.war""
     * @required
     */
    private File warFile;
    
	/**
     * @parameter default-value="${basedir}/provisioning.conf"
     */
    protected File provisioningConfig;
    
    /**
     * @parameter default-value=4
     */
    protected int defaultStartLevel;
	
    /**
     * @parameter default-value="${project.build.directory}/war"
     * @required
     */
    private File warDirectory;
    
    /**
     * @parameter default-value="equinox"
     */
    private String osgiRuntime;
    
    
    public void execute() throws MojoExecutionException {
    	
    	if(warDirectory.exists()) {
    		FileUtils.delete(warDirectory);
    	}
    	warDirectory.mkdir();
    	
    	File webInfDir = new File(warDirectory, "WEB-INF");
    	webInfDir.mkdir();
    	
    	File eclipseDir = new File(webInfDir, "eclipse");
    	eclipseDir.mkdir();
    	
    	File pluginsDir = new File(eclipseDir, "plugins");
    	pluginsDir.mkdir();
    	
    	File configDir = new File(eclipseDir, "configuration");
    	configDir.mkdir();
    	
    	File libDir = new File(webInfDir, "lib");
    	libDir.mkdir();
    	
    
    	URL[] provisioningConfigs;
		try {
			provisioningConfigs = new URL[] {provisioningConfig.toURI().toURL()};
		} catch (MalformedURLException x) {
			throw new MojoExecutionException("Illegal provisioning configuration URL", x);
		}
    	
    
		BundleResolver bundleResolver = new BundleResolver(provisioningConfigs, defaultStartLevel,
				mavenProject, artifactFactory, resolver, metadataSource, localRepository, remoteRepositories, getLog());
    	
    	List<BundleConfig> bundles = bundleResolver.resolve();
    	for(BundleConfig bundle:bundles) {
    		File source = bundle.getFile();
    		if(source.isDirectory()) {
    			File targetDir = new File(mavenProject.getBasedir(), "target");
    			source = new File(targetDir, mavenProject.getArtifactId() + "-" + mavenProject.getVersion() + ".jar");
    			BundleConfig bc = new BundleConfig(source, bundle.getBundleSymbolicName(), true, 4);
    			bundles.set(bundles.indexOf(bundle), bc);
    		}
    		File target = new File(pluginsDir, source.getName());
    		
    		try {
				if(!bundle.getBundleSymbolicName().contains(JETTY_BUNDLE_SYMBOLIC_NAME)) FileUtils.copyFile(source, target);
			} catch (IOException x) {
				throw new MojoExecutionException("Can't copy bundle: " + bundle.getFile().getAbsolutePath(), x);
			}
    	}

    	URL webXml = getClass().getResource("/eclipse/web.xml");
    	try {
			copyFile(webXml, new File(webInfDir, "web.xml"));
		} catch (IOException x) {
			throw new MojoExecutionException("Can't copy web.xml", x);
		}
    	
		URL launchIni = getClass().getResource("/eclipse/launch.ini");
		try {
			copyFile(launchIni, new File(webInfDir, "launch.ini"));
		} catch (IOException x) {
			throw new MojoExecutionException("Can't copy launch.ini", x);
		}
			
		URL servletBridge = getClass().getResource("/eclipse/servletbridge.jar");
		try {
			copyFile(servletBridge, new File(libDir, "servletbridge.jar"));
		} catch (IOException x) {
			throw new MojoExecutionException("Can't copy servletbridge.jar", x);
		}
		
		InputStream in = getClass().getResourceAsStream("/eclipse/bundles.list");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        try {
        	while((line = reader.readLine()) != null) {
        		line = line.trim();
        		URL bundle = getClass().getResource("/eclipse/bundles/"+line);
        		copyFile(bundle, new File(pluginsDir, line));
        	}
        	reader.close();
        } catch(IOException x) {
        	throw new MojoExecutionException("Error reading bundle list", x);
        }
        	
		StringBuilder config = new StringBuilder();
		config.append("osgi.bundles.defaultStartLevel=4\n");
		config.append("osgi.clean=true\n");
		config.append("org.osgi.supports.framework.fragment=true\n");
		config.append("osgi.bundles=");
		config.append("org.eclipse.equinox.common@2:start,org.eclipse.osgi.services,org.eclipse.equinox.servletbridge.extensionbundle,org.eclipse.equinox.http.servlet,org.eclipse.equinox.http.servletbridge@3:start,");

	
    	Iterator<BundleConfig> it = bundles.iterator();
    	while(it.hasNext()) {
    		BundleConfig bundle = it.next();
    		if(!bundle.getBundleSymbolicName().equals(FRAMEWORK_BUNDLE_SYMBOLIC_NAME) && !bundle.getBundleSymbolicName().contains(JETTY_BUNDLE_SYMBOLIC_NAME)) {
				config.append(bundle.getFile().getName());
				
				if(bundle.doStart()) {
				    if(bundle.getStartLevel()==4) config.append("@start");
				    else config.append("@"+bundle.getStartLevel()+":start");
				}
				
				if(it.hasNext()) config.append(",");
    		}
    		
		}
    	config.append("\n");
	
    	try {
    		File configFile = new File(configDir,"config.ini");
    		FileOutputStream out = new FileOutputStream(configFile);
    		out.write(config.toString().getBytes("UTF-8"));
    		out.close();
    	} catch(IOException x) {
			throw new RuntimeException("Error creating Equinox configuration", x);
		}
    	
    	try {
    		FileOutputStream out = new FileOutputStream(warFile);
    		JarOutputStream jarOut = new JarOutputStream(out);
    		addJarEntry(jarOut, warDirectory, warDirectory);
    		jarOut.close();
    	} catch(IOException x) {
    		throw new MojoExecutionException("Error writing war file", x);
    	}
    	
    }

	private void addJarEntry(JarOutputStream out, File warDir, File file) throws IOException {
		
		String warPath = warDir.getCanonicalPath();
		String filePath = file.getCanonicalPath();
		if(!filePath.startsWith(warPath)) throw new IOException("File isn't relative to war directory: " + filePath);
		String relPath;
		if(filePath.equals(warPath)) relPath = "";
		else relPath = filePath.substring(warPath.length() + 1);
		
		if(file.isDirectory() && !file.getName().startsWith(".")) {
			if(!relPath.equals("")) {
				JarEntry entry = new JarEntry(relPath + "/");
				out.putNextEntry(entry);
			}
			File[] children = file.listFiles();
			for(File child: children) {
				addJarEntry(out, warDir, child);
			}
		} else {
		    JarEntry entry = new JarEntry(relPath);
		    out.putNextEntry(entry);
		    copyFileToStream(file, out);
		}
	}
    
    
    private static void copyFile(URL url, File destFile) throws IOException {
        InputStream in = url.openStream();
        FileOutputStream out = new FileOutputStream(destFile);
        byte[] buffer = new byte[4096];
        int no = 0;
        try {
            while ((no = in.read(buffer)) != -1)
                out.write(buffer, 0, no);
        } finally {
            in.close();
            out.close();
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
