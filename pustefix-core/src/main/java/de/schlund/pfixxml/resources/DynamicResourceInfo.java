package de.schlund.pfixxml.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DynamicResourceInfo {

    private List<Entry> entries = new ArrayList<Entry>();
    
    public void addEntry(String module, boolean resourceExists, boolean partExists) {
        Entry entry = new Entry(module, resourceExists, partExists);
        entries.add(entry);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry> it = entries.iterator();
        while(it.hasNext()) {
            Entry entry = it.next();
            sb.append(entry.module).append("(");
            if(entry.resourceExists) {
                if(entry.partExists) sb.append("+");
                else sb.append("-");
            } else sb.append("?");
            sb.append(")");
            if(it.hasNext()) sb.append(" ");
        }
        return sb.toString();
    }
    
    
    class Entry {
        
        Entry(String module, boolean resourceExists, boolean partExists) {
            this.module = module;
            this.resourceExists = resourceExists;
            this.partExists = partExists;
        }
        
        String module;
        boolean resourceExists;
        boolean partExists;
        
    }
    
}
