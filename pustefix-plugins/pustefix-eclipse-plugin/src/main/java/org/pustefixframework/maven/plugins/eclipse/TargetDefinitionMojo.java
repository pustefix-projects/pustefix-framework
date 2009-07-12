package org.pustefixframework.maven.plugins.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * Goal which creates an Eclipse target definition
 * from the project's POM or a configuration file.
 *
 * @goal target
 */
public class TargetDefinitionMojo extends AbstractMojo {
	
    /**
     * @parameter default-value="${project.build.directory}/eclipse-target-platform"
     * @required
     */
    private File targetPlatformDirectory;
    
    /**
     * @parameter default-value="${project.build.directory}/${project.groupId}-${project.artifactId}.target"
     * @required
     */
    private File targetDefinitionFile;
    
    /**
     * @parameter default-value="${project.groupId}-${project.artifactId}"
     * @required
     */
    private String targetName;
    
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


    public TargetDefinitionMojo() {
    }

    public void execute() throws MojoExecutionException {
    	
    	if(!targetPlatformDirectory.exists()) targetPlatformDirectory.mkdirs();
    	
    	Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException("Can't create target definition document", e);
        }
        ProcessingInstruction pi = doc.createProcessingInstruction("pde", "version=\"3.5\"");
        doc.appendChild(pi);
        
    	Element targetElem = doc.createElement("target");
    	targetElem.setAttribute("name", targetName);
    	doc.appendChild(targetElem);
    	Element locationsElem = doc.createElement("locations");
    	targetElem.appendChild(locationsElem);
    	
    	Element locationElem = doc.createElement("location");
    	locationElem.setAttribute("path", targetPlatformDirectory.getAbsolutePath());
    	locationElem.setAttribute("type", "Directory");
    	locationsElem.appendChild(locationElem);
    	
    	List<?> list=mavenProject.getDependencies();
    	Set<?> dependencyArtifacts;
        try {
            dependencyArtifacts = MavenMetadataSource.createArtifacts( artifactFactory, list, null, null, null );
        } catch (InvalidDependencyVersionException e) {
            throw new MojoExecutionException("Can't create artifacts", e);
        }
    	Artifact pomArtifact = mavenProject.getArtifact();
    	
    	ArtifactResolutionResult result;
        try {
            result = resolver.resolveTransitively(dependencyArtifacts, pomArtifact, 
                    Collections.EMPTY_MAP, localRepository, remoteRepositories, metadataSource, null, Collections.EMPTY_LIST);
        } catch (AbstractArtifactResolutionException e) {
            throw new MojoExecutionException("Can't resolve artifacts", e);
        } 
    	Set<?> resolved = result.getArtifacts();
    	Iterator<?> it = resolved.iterator();
    	while(it.hasNext()) {
    	    Artifact artifact = (Artifact)it.next();
    	    if(artifact.getScope().equals(Artifact.SCOPE_RUNTIME) || artifact.getScope().equals(Artifact.SCOPE_COMPILE)) {
    	        try {
    	            JarFile jarFile = new JarFile(artifact.getFile());
    	            Manifest m=jarFile.getManifest();
    	            boolean startable = false;
    	            String fragmentHost = m.getMainAttributes().getValue("Fragment-Host");
    	            if(fragmentHost == null) {
    	                String bundleActivator = m.getMainAttributes().getValue("Bundle-Activator");
    	                if(bundleActivator != null) startable = true;
    	            }
    	        } catch(IOException x) {
    	            throw new MojoExecutionException("Can't read manifest", x);
    	        }
    	        File srcFile = artifact.getFile();
    	        File destFile = new File(targetPlatformDirectory, srcFile.getName());
    	        try {
                    copyFile(srcFile, destFile);
                } catch (IOException e) {
                    throw new MojoExecutionException("Can't copy artifact", e);
                }
    	    }
    	}
    	
    	try {
        	FileOutputStream out = new FileOutputStream(targetDefinitionFile);
        	Transformer transformer = TransformerFactory.newInstance().newTransformer();
        	Source src = new DOMSource(doc);
        	Result res = new StreamResult(out);
        	transformer.transform(src, res);
        	out.close();
    	} catch(Exception x) {
    	    throw new MojoExecutionException("Can't write target definition file", x);
    	}
    
    }
    
    private void copyFile(File srcFile, File destFile) throws IOException {
        if (!(srcFile.exists() && srcFile.isFile())) throw new IllegalArgumentException("Source file doesn't exist: " + srcFile.getAbsolutePath());
        if (destFile.exists() && destFile.isDirectory())
            throw new IllegalArgumentException("Destination file is directory: " + destFile.getAbsolutePath());
        FileInputStream in = new FileInputStream(srcFile);
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
    
}
