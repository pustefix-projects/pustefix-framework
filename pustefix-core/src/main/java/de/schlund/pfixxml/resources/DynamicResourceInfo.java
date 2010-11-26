package de.schlund.pfixxml.resources;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DynamicResourceInfo {

    private URI originalURI;
    private URI resolvedURI;
    private List<Entry> entries = new ArrayList<Entry>();
    
    public DynamicResourceInfo(URI originalURI) {
        this.originalURI = originalURI;
    }
    
    public void setResolvedURI(URI resolvedURI) {
        this.resolvedURI = resolvedURI;
    }
    
    public void addEntry(URI uri, boolean resourceExists, boolean partExists) {
        Entry entry = new Entry(uri, resourceExists, partExists);
        entries.add(entry);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(originalURI).append("\n");
        sb.append(resolvedURI).append("\n");
        for(Entry entry: entries) {
            sb.append(entry.uri.toASCIIString()).append("|").append(entry.resourceExists)
                .append("|").append(entry.partExists).append("\n");
        }
        return sb.toString();
    }
    
    
    class Entry {
        
        Entry(URI uri, boolean resourceExists, boolean partExists) {
            this.uri = uri;
            this.resourceExists = resourceExists;
            this.partExists = partExists;
        }
        
        URI uri;
        boolean resourceExists;
        boolean partExists;
        
    }
    
}
