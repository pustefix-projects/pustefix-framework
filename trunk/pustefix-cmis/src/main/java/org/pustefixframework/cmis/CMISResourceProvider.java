package org.pustefixframework.cmis;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceProvider;
import de.schlund.pfixxml.resources.ResourceProviderException;

public class CMISResourceProvider implements ResourceProvider {

    private final static String CMIS_SCHEME = "cmis";
    
    private String[] supportedSchemes = {CMIS_SCHEME};
    
    private Map<String, CMISMapping> cmisMappings = new HashMap<String, CMISMapping>();
    
    public CMISResourceProvider() {
        registerMappings();
    }
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
    public Resource getResource(URI uri) throws ResourceProviderException {
        if (uri.getScheme() == null)
            throw new ResourceProviderException("Missing URI scheme: " + uri);
        if (!uri.getScheme().equals(CMIS_SCHEME))
            throw new ResourceProviderException("URI scheme not supported: " + uri);
        String mappingId = uri.getAuthority();
        if(mappingId == null || mappingId.equals("")) mappingId = "default";
        CMISMapping mapping = cmisMappings.get(mappingId);
        if (mapping == null) throw new ResourceProviderException("CMIS mapping not found: " + mappingId);
        return new CMISResource(uri, mapping);
    }
    
    private void registerMappings() {
        ServiceLoader<CMISMappingFactory> loader = ServiceLoader.load(CMISMappingFactory.class);
        Iterator<CMISMappingFactory> factories = loader.iterator();
        while(factories.hasNext()) {
            CMISMappingFactory factory = factories.next();
            List<CMISMapping> mappings = factory.getCMISMappings();
            if(mappings != null) {
                for(CMISMapping mapping: mappings) {
                    cmisMappings.put(mapping.getId(), mapping);
                }
            }
        }
    }
    
}
