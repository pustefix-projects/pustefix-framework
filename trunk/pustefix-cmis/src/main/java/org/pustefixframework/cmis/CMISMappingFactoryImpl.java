package org.pustefixframework.cmis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class CMISMappingFactoryImpl implements CMISMappingFactory {

    private List<CMISMapping> mappings = new ArrayList<CMISMapping>();
    
    public CMISMappingFactoryImpl() {
        try {
            Enumeration<URL> e = getClass().getClassLoader().getResources("META-INF/org/pustefixframework/cmis/mapping.properties");
            while(e.hasMoreElements()) {
                URL url = e.nextElement();
                Properties props = new Properties();
                InputStream in = url.openStream();
                props.load(in);
                in.close();
                Set<String> processed = new HashSet<String>();
                Enumeration<?> keys = props.propertyNames();
                while(keys.hasMoreElements()) {
                    String key = (String)keys.nextElement();
                    int ind = key.indexOf('.');
                    if(ind > 0) {
                        String id = key.substring(0, ind);
                        if(!processed.contains(id)) {
                            String atomPubUrl = props.getProperty(id + ".atomPubUrl");
                            String user = props.getProperty(id + ".user");
                            String password = props.getProperty(id + ".password");
                            String basePath = props.getProperty(id + ".basePath");
                            CMISMapping mapping = new CMISMappingImpl(id, atomPubUrl, user, password, basePath);
                            mappings.add(mapping);
                            processed.add(id);
                        }
                    }
                }
            }
        } catch(IOException x) {
            throw new RuntimeException("Error loading CMIS mapping properties.", x);
        }
    }
    
    public List<CMISMapping> getCMISMappings() {
        return mappings;
    }
    
}
