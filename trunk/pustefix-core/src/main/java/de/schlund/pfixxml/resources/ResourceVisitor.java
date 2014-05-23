package de.schlund.pfixxml.resources;

import de.schlund.pfixxml.resources.Resource;

public interface ResourceVisitor {

    public void visit(Resource resource);
    
}
