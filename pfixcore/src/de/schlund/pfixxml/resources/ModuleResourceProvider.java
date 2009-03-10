package de.schlund.pfixxml.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.ModuleDescriptor;
import de.schlund.pfixcore.util.ModuleInfo;
import de.schlund.pfixxml.config.BuildTimeProperties;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ModuleResourceProvider implements ResourceProvider {

    private static Logger LOG = Logger.getLogger(ModuleResourceProvider.class);
    
    private static String MODULE_SCHEME = "module";
    
    private String[] supportedSchemes = {MODULE_SCHEME};
    
    public String[] getSupportedSchemes() {
        return supportedSchemes;
    }
    
    public Resource getResource(URI uri) throws ResourceProviderException {
        if(uri.getScheme() == null) 
            throw new ResourceProviderException("Missing URI scheme: "+uri);
        if(!uri.getScheme().equals(MODULE_SCHEME)) 
            throw new ResourceProviderException("URI scheme not supported: "+uri);
        String module = uri.getAuthority();
        if(module == null || module.equals(""))
            throw new ResourceProviderException("Missing module name: "+uri);
        ModuleDescriptor desc = ModuleInfo.getInstance().getModuleDescriptor(module);
        if(desc != null) {
            URL url = getJarURL(desc.getURL());
            //Ensure module resources are read from classpath in production environment
            if(!BuildTimeProperties.getProperties().getProperty("mode").equals("prod")) {
                List<ModuleSourceLocator> locators = ModuleSourceLocatorRegistry.getInstance().getLocators();
                for(ModuleSourceLocator locator:locators) {
                    File location = locator.getLocation(url);
                    if(location!=null) {
                        if(location.exists()) {
                            ModuleResource res = new ModuleSourceResource(uri, location);
                            return res;
                        } else {
                            LOG.warn("Module source location doesn't exist: " + location.getAbsolutePath());
                        }
                    }
                }
            }
            return new ModuleResource(uri, url);
        }
        return new ModuleResource(uri);
    }
    
    private static URL getJarURL(URL url) {
        if(!url.getProtocol().equals("jar")) throw new PustefixRuntimeException("Invalid protocol: "+url);
        String urlStr = url.toString();
        int ind = urlStr.indexOf('!');
        if(ind > -1 && urlStr.length() > ind + 1)  {
            urlStr = urlStr.substring(0, ind+2);
        } else throw new PustefixRuntimeException("Unexpected module descriptor URL: "+url);
        try {
            return new URL(urlStr);
        } catch(MalformedURLException x) {
            throw new PustefixRuntimeException("Invalid module URL: "+urlStr);
        }
    }
    
}
