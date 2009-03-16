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
    
    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

}
