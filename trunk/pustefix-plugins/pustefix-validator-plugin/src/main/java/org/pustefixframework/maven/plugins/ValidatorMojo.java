package org.pustefixframework.maven.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Validates configuration files.
 *
 * @goal validate
 * @phase prepare-package
 */
public class ValidatorMojo extends AbstractMojo {

    private int checks;
    private int warnings;
    private int errors;
    private int fatalErrors;
    
    /**
     * @parameter default-value="${basedir}/src/main/webapp/WEB-INF"
     * @required
     */
    private File configDir;
    
    /**
     * @parameter default-value=false
     */
    private boolean failOnWarning;
    
    /**
     * @parameter default-value=false
     */
    private boolean recursive;
    
    /**
     * @parameter default-value=false
     */
    private boolean cacheEntities;
    
    private static ThreadLocal<EntityResolver> entityResolver = new ThreadLocal<>();
    
    
    public void execute() throws MojoExecutionException {
        if(configDir.exists()) {
            
            validate(configDir, recursive);
            
            getLog().info("Checked " + checks + " file" + ( checks > 1 ? "s" : ""));
            getLog().info("Warnings: " + warnings);
            getLog().info("Errors: " + errors);
            getLog().info("Fatal errors: " + fatalErrors);
            
            if(errors>0 || fatalErrors>0) {
                throw new MojoExecutionException("Stopping after validation errors.");
            }
            if(warnings > 0 && failOnWarning) {
                throw new MojoExecutionException("Stopping after validation warnings.");
            }
        }
    }
    
    private void validate(File dir, boolean recursive) throws MojoExecutionException {
        File[] files = dir.listFiles();
        for(File file: files) {
            if(!file.isHidden()) {
                if(file.isFile()) {
                    if(file.getName().endsWith(".xml")) {
                        validate(file);
                    }
                } else if(file.isDirectory() && recursive) {
                    validate(file, recursive);
                }
            }
        }
    }
    
    private void validate(File file) throws MojoExecutionException {
        ValidatorErrorHandler handler = new ValidatorErrorHandler(getLog());
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.getEntityResolver();
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setFeature("http://xml.org/sax/features/validation", true);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            if(cacheEntities) {
                EntityResolver resolver = entityResolver.get();
                if(resolver == null) {
                    resolver = new CachingEntityResolver();
                    entityResolver.set(resolver);
                }
                reader.setEntityResolver(resolver);
            }
            Writer writer = new PrintWriter(System.err);
            reader.setErrorHandler(handler);
            InputSource in = new InputSource(new FileInputStream(file));
            in.setSystemId(file.getPath());
            
            reader.parse(in);
            writer.close();
        } catch(SAXException e) {
            getLog().error("Error parsing file '" + file.getPath() + "': " + e.getMessage());
        } catch(IOException e) {
            throw new MojoExecutionException("Can't validate file: " + file.getPath(), e);
        } finally {
            checks++;
            warnings += handler.getWarnings();
            errors += handler.getErrors();
            fatalErrors += handler.getFatalErrors();
        }
    }
    
    
    private class CachingEntityResolver implements EntityResolver {
        
        private Map<String, byte[]> cache = new HashMap<>();
        
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            
            if(systemId != null) {
                if(systemId.startsWith("http")) {
                    byte[] entity = null;
                    entity = cache.get(systemId);
                    if(entity == null) {
                        URL url = new URL(systemId);
                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        con.setConnectTimeout(1000);
                        con.setReadTimeout(1000);
                        InputStream in = con.getInputStream();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int no = 0;
                        try {
                            while ((no = in.read(buffer)) != -1) {
                                out.write(buffer, 0, no);
                            }
                        } finally {
                            in.close();
                            out.close();
                        }
                        if(con.getResponseCode() >= 400) {
                            throw new SAXException("Error resolving entity '" + systemId + "': " + con.getResponseMessage());
                        }
                        entity = out.toByteArray();
                        cache.put(systemId, entity);
                    }
                    if(entity != null) {
                        InputSource src = new InputSource(new ByteArrayInputStream(entity));
                        src.setSystemId(systemId);
                        return src;
                    }
                } else {
                    return new InputSource(systemId);
                }
            }
            return null;
        }
        
    }
    
}
