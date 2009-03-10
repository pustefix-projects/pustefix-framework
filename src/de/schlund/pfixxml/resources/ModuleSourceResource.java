package de.schlund.pfixxml.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ModuleSourceResource extends ModuleResource {
    
    private File file;
    
    public ModuleSourceResource(URI uri, File moduleDir) {
        super(uri);
        file = new File(moduleDir, "PUSTEFIX-INF"+uri.getPath());
    }
    
    public boolean canRead() {
        return file.canRead();
    }

    public boolean exists() {
        return file.exists();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public boolean isFile() {
        return file.isFile();
    }

    public long lastModified() {
        return file.lastModified();
    }
    
}
