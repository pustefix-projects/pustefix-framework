package org.pustefixframework.cmis;

import org.apache.chemistry.opencmis.client.api.CmisObject;

public interface CMISMapping {
    
    public String getId();
    public CmisObject getCMISObject(String path);

}
