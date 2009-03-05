package de.schlund.pfixxml.resources;

import java.net.URI;

public interface ResourceProvider {

    public String[] getSupportedSchemes();
    public Resource getResource(URI uri) throws ResourceProviderException;
    
}
