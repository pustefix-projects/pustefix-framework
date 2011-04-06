package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.Resource;

public class IncludePartsInfoFactory {
    
    private Map<String, IncludePartsInfo> urisToInfo;
    private boolean reloadable;
    
    public IncludePartsInfoFactory() {
        urisToInfo = new HashMap<String, IncludePartsInfo>();
        String mode = EnvironmentProperties.getProperties().getProperty("mode");
        if(mode != null && !mode.equals("prod")) reloadable = true;
    }
    
    public boolean containsPart(Resource resource, String part) {
        String uri = resource.toURI().toString();
        IncludePartsInfo info = urisToInfo.get(uri);
        if(info == null || (reloadable && (info.getLastMod() < resource.lastModified()))) {
            info = IncludePartsInfoParser.parse(resource);
            urisToInfo.put(uri, info);
        }
        return info.getParts().contains(part);
    }
    
}
