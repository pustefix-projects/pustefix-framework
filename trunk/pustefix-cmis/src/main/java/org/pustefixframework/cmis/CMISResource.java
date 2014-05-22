package org.pustefixframework.cmis;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentProperties;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.pustefixframework.util.URLUtils;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceUtil;

public class CMISResource implements Resource {

    private CMISMapping cmisMapping;
    private URI uri;
    private URI origUri;
    
    private CmisObject cmisObject;
    
    public CMISResource(URI uri, CMISMapping cmisMapping) {
        this.uri = uri;
        this.cmisMapping = cmisMapping;
    }
    
    private synchronized CmisObject getCmisObject() {
        try {
            cmisObject = cmisMapping.getCMISObject(uri.getPath());
        } catch(CmisObjectNotFoundException x) {
            //ignore and let CmisObject be null to indicate that resource doesn't exist  
        }
        return cmisObject;
    }

    public boolean canRead() {
        return exists();
    }
    
    public boolean exists() {
        CmisObject obj = getCmisObject();
        return obj != null;
    }
    
    public InputStream getInputStream() throws IOException {
        CmisObject obj = getCmisObject();
        if(obj == null) throw new IOException("Resource '" + uri.toString() + "' can't be found.");
        if(obj != null && obj instanceof Document) {
            Document docObj = (Document)obj;
            return docObj.getContentStream().getStream();
        } else {
            return null;
        }
    }
    
    public boolean isFile() {
        CmisObject obj = getCmisObject();
        return (obj != null && obj instanceof Document);
    }
    
    public long lastModified() {
        CmisObject obj = getCmisObject();
        if(obj != null && obj instanceof FileableCmisObject) {
            FileableCmisObject fileObj = (FileableCmisObject)obj;
            return fileObj.getLastModificationDate().getTimeInMillis();
        } else {
            return -1;
        }
    }
    
    public long length() {
        CmisObject obj = getCmisObject();
        if(obj != null && obj instanceof DocumentProperties) {
            DocumentProperties docObj = (DocumentProperties)obj;
            return docObj.getContentStreamLength();
        } else {
            return 0;
        }
    }
    
    public URI toURI() {
        return uri;
    }
    
    public URI getOriginatingURI() {
        if(origUri == null) return toURI();
        return origUri;
    }
    
    public void setOriginatingURI(URI origUri) {
        this.origUri = origUri;
    }
    
    public int compareTo(Resource res) {
        return uri.compareTo(res.toURI());
    }
    
    @Override
    public String toString() {
        return uri.toString();
    }
    
    //Spring Resource compatibility methods
    
    public boolean isReadable() {
        CmisObject obj = getCmisObject();
        return obj != null;
    }

    public boolean isOpen() {
        return false;
    }
    
    public URL getURL() throws IOException {
        return toURI().toURL();
    }

    public URI getURI() throws IOException {
        return toURI();
    }
    
    public String getFilename() {
        String path = uri.getPath();
        int ind = path.lastIndexOf('/');
        return ind > -1 ? path.substring(ind + 1) : path;
    }
    
    public long contentLength() throws IOException {
        return length();
    }
    
    public File getFile() throws IOException {
        throw new IOException("Resource isn't available on the file system: " + toURI());
    }

    public String getDescription() {
        return toURI().toASCIIString();
    }
    
    public org.springframework.core.io.Resource createRelative(String relativePath) throws IOException {
        String parentPath = null;
        if(!isFile()) {
            parentPath = uri.getPath();
        } else {
            parentPath = URLUtils.getParentPath(uri.getPath());
        }
        if(parentPath.endsWith("/")) parentPath = parentPath.substring(0, parentPath.length()-1);
        try {
            URI relUri = new URI(uri.getScheme(), uri.getAuthority(), parentPath + "/" + relativePath , null);
            return ResourceUtil.getResource(relUri);
        } catch (URISyntaxException e) {
            throw new IOException("Error creating relative URI: " + uri.toASCIIString() + " " + relativePath, e);
        }
    }
    
}
