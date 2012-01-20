package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.Resource;

public class IncludePartsInfoFactory {
    
    private final Map<String, IncludePartsInfo> urisToInfo;
    private final boolean reloadable;
    
    public IncludePartsInfoFactory() {
        urisToInfo = new HashMap<String, IncludePartsInfo>();
        String mode = EnvironmentProperties.getProperties().getProperty("mode");
        reloadable = !"prod".equals(mode);
    }
    
    public boolean containsPart(Resource resource, String part) throws IncludePartsInfoParsingException {
        IncludePartsInfo info = getIncludePartsInfo(resource);
        if(info != null) {
            return info.getParts().containsKey(part);
        }
        return false;
    }
    
    public IncludePartsInfo getIncludePartsInfo(Resource resource) throws IncludePartsInfoParsingException {
        String uri = resource.toURI().toString();
        IncludePartsInfo info = null;
        synchronized(urisToInfo) {
            info = urisToInfo.get(uri);
        }
        if(info == null || (reloadable && (info.getLastMod() < resource.lastModified()))) {
            info = IncludePartsInfoParser.parse(resource);
            synchronized(urisToInfo) {
                urisToInfo.put(uri, info);
            }
        }
        return info;
    }
    
    public void reset() {
        synchronized(urisToInfo) {
            urisToInfo.clear();
        }
    }
    
}
