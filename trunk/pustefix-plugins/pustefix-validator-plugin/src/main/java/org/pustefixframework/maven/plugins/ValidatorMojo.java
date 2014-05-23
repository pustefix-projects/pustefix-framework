package org.pustefixframework.maven.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Validates configuration files
 *
 * @author mleidig@schlund.de
 *
 * @goal validate
 * @phase prepare-package
 */
public class ValidatorMojo extends AbstractMojo {

    private int validations;
    private int warnings;
    private int errors;
    private int fatalErrors;
    
    /**
     * @parameter default-value="${basedir}/src/main/webapp/WEB-INF"
     * @required
     */
    private File configDir;
    
    public void execute() throws MojoExecutionException {
        File[] files = configDir.listFiles();
        for(File file: files) {
            if(file.isFile() && file.getName().endsWith(".xml")) {
                validate(file);
            }
        }
        getLog().info("Warnings: " + warnings);
        getLog().info("Errors: " + errors);
        getLog().info("Fatal errors: " + fatalErrors);
        
        if(errors>0 || fatalErrors>0) {
            throw new MojoExecutionException("Stopping after validation errors.");
        }
    }
    
    private boolean validate(File file) throws MojoExecutionException {
        ValidatorErrorHandler handler = new ValidatorErrorHandler(getLog());
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setFeature("http://xml.org/sax/features/validation", true);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            Writer writer = new PrintWriter(System.err);
            reader.setErrorHandler(handler);
            InputSource in = new InputSource(new FileInputStream(file));
            in.setSystemId(file.getPath());
            reader.parse(in);
            writer.close();
        } catch(SAXException e) {
            getLog().error("Error parsing file '" + file.getPath() + "': " + e.getMessage());
            return false;
        } catch(IOException e) {
            throw new MojoExecutionException("Can't validate file: " + file.getPath(), e);
        } finally {
            validations++;
            warnings += handler.getWarnings();
            errors += handler.getErrors();
            fatalErrors += handler.getFatalErrors();
        }
        return true;
    }
    
}
