package de.schlund.pfixxml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Stores the last SPDocument instances for showing them
 * on the DOM tree page (in development mode).
 */
public class SPDocumentHistory {

    private static final int MAX_ENTRIES = 10;
    private List<Entry> entries;
    
    public synchronized void addSPDocument(SPDocument doc, PfixServletRequest preq) {
        Entry entry = new Entry();
        entry.doc = doc;
        entry.url = preq.getRequestURI();
        if(preq.getQueryString() != null) {
            entry.url += "?" + preq.getQueryString();
        }
        if(entry.url.length() > 60) {
            entry.url = entry.url.substring(0, 58) + "...";
        }
        if(entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(0, entry);
        if(entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }
    
    public synchronized SPDocument getSPDocument(long serial) {
        if(entries != null) {
            for(Entry entry: entries) {
                if(entry.doc.getTimestamp() == serial) {
                    return entry.doc;
                }
            }
        }
        return null;
    }
    
    public synchronized Document toXML() {
        DocumentBuilder db;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error creating SPDocument history XML", e);
        }
        Document doc = db.newDocument();
        Element root = doc.createElement("domhistory");
        doc.appendChild(root);
        if(entries != null) {
            for(Entry entry: entries) {
                Element elem = doc.createElement("doc");
                root.appendChild(elem);
                elem.setAttribute("serial", String.valueOf(entry.doc.getTimestamp()));
                elem.setAttribute("page", entry.doc.getPagename());
                elem.setAttribute("url", entry.url);
            }
        }
        return doc;
    }
    
    
    private class Entry {
        
        SPDocument doc;
        String url;
        
    }
    
}
