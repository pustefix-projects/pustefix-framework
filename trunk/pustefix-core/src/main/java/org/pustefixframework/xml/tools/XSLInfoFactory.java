package org.pustefixframework.xml.tools;

import java.util.HashMap;
import java.util.Map;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.resources.Resource;

public class XSLInfoFactory {

    private final Map<String, XSLInfo> urisToInfo;
    private final boolean reloadable;
    
    public XSLInfoFactory() {
        urisToInfo = new HashMap<String, XSLInfo>();
        String mode = EnvironmentProperties.getProperties().getProperty("mode");
        reloadable = !"prod".equals(mode);
    }
    
    public XSLInfo getXSLInfo(Resource resource) throws XSLInfoParsingException {
        String uri = resource.toURI().toString();
        XSLInfo info = null;
        synchronized(urisToInfo) {
            info = urisToInfo.get(uri);
        }
        if(info == null || (reloadable && (info.getLastModified() < resource.lastModified()))) {
            info = XSLInfoParser.parse(resource);
            synchronized(urisToInfo) {
                urisToInfo.put(uri, info);
            }
        }
        return info;
    }

}
