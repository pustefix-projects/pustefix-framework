package de.schlund.pfixcore.example;

import org.springframework.core.io.Resource;

public class ResourceTestBean {

    private Resource resource;
    
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    public Resource getResource() {
        return resource;
    }
    
}
