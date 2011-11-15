package org.pustefixframework.ide.eclipse.plugin.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.w3c.dom.Document;

public interface StatusCodeGenerator {

    public void createHeader(Writer writer, String className) throws IOException;
    public void createResources(Writer writer, List<String> docRelPaths) throws IOException;
    public void createStatusCodes(Writer writer, Document doc, int resIndex) throws IOException;
    public String getModulePath(String relPath, String module);
    
}
