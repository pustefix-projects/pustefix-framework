package de.schlund.pfixcore.example;

import java.io.File;

import de.schlund.pfixcore.workflow.ContextResource;

/**
 * @author mleidig@schlund.de
 */
public interface ContextEncodingTest extends ContextResource {
    
    public String getText();
    public void setText(String text);
    public File getFile();
    public void setFile(File file);
    public String getEncoding();
    public void setEncoding(String encoding);

}
