package de.schlund.pfixcore.editor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author zaich
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class EditorDocumentation {

    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private static Category LOG                 = Category.getInstance(EditorProductFactory.class.getName());
    private long   creationTime                 = 0;

    private HashMap hashmap                     = new HashMap();
    private NodeList nlist                         = null;
    private Document doc                         = null;

    private HashMap filesMap                     = new HashMap();
    private String[] allFiles                     = null;
    
    
    
    // Constructer called with an array of the xsl.in files
    public EditorDocumentation(String args[]) throws Exception{
        this.allFiles = args;
        this.generateDocumentation(args);

    }
    
    public void generateDocumentation(String args[]) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String path = args[i];
            try {
                File file = new File(path);
                Long time = new Long(file.lastModified());
                if (time != null && path != null) {
                    filesMap.put(path, time);
                    
                }
                LOG.debug(" * DOCUMENTATION-FILE " + args[i] + " found * ");
                this.readFile(path);
            }
            catch (Exception ex) {
                LOG.debug(" * File " + args[i] + " not found * ");
            }
        }
    }
    
    // read the xsl-File and create the NodeList
    private void readFile(String filename) throws Exception {
        
        DocumentBuilder domp = dbfac.newDocumentBuilder();
        this.doc = domp.parse(filename);
        this.doc.normalize();
        
        NodeList nl = doc.getElementsByTagName("xsl:template");
        
        for (int i = 0; i < nl.getLength(); i++) {
            
            Element prj = (Element) nl.item(i);
            String name = prj.getAttribute("name");
            String match = prj.getAttribute("match");
            String mode = prj.getAttribute("mode");
            
            // Getting the Filename without the Path
            String filenameNew =
                filename.substring(filename.lastIndexOf("/") + 1, filename.lastIndexOf("."));
            
            // Building the New Id, e.g. forminput.xsl.in@macht|pfx:button|name
            // String id = filenameNew + "@match|" + match + "|name|" + name;
            String id = filenameNew + "@" + match + "|" + name + "|" + mode;
            
            
            // Creating the new CoreDocumentation Object (Param = Id)
            CoreDocumentation coreDoc = new CoreDocumentation(id);
            
            
            // Setting new NodeList for cus:documentation..
            NodeList neuList = prj.getElementsByTagName("cus:documentation");
            
            Node node = neuList.item(0);
            
            coreDoc.setMatch(match);
            coreDoc.setName(name);
            coreDoc.setMode(mode);
            
            // Setting the Mode
            if (!match.equals("") && !name.equals("")) {
                coreDoc.setModus("public|private");
            }
            else if (!match.equals("")) {
                coreDoc.setModus("public");
            }
            else if (!name.equals("")) {
                coreDoc.setModus("private");
            }
            
            // Getting the ChildNotes for put 'em into the Hashmap !
            
            if (neuList.getLength() > 0) {
                NodeList nodeListChild = node.getChildNodes();
                coreDoc.setNodeList(nodeListChild);
                coreDoc.setDocument(this.doc);
                
                // HashMap Put with the Id and CoreDoc Object
                this.hashmap.put(coreDoc.getId(), coreDoc);
            } 
            else {
                String errormsg = "Documentation for " + match + " not found";
                LOG.debug(errormsg);
                coreDoc.setNodeList(neuList);                        
                this.hashmap.put(coreDoc.getId(), coreDoc);
            }
        }
        this.nlist = nl;
    }
    
    // Checks if the Xsl-Files has been modified
    public void checkFile() throws Exception {
                    
        // Getting all Files and there LastModified-Time and put 'em into Hashmap
        HashMap files = this.getAllStylesheets();
        
        Collection menge = files.keySet();
        
        boolean destroy = false;
        
        for (Iterator it = menge.iterator(); it.hasNext();) {
            
            String filename = (String) it.next();
            
            File file = new File(filename);

            Long time_neu = new Long(file.lastModified());

            if (time_neu != null) {

                if (this.hasFileModified(filename, time_neu)) {
                    LOG.debug("File " + filename + "has been modified");
                    destroy = true;            
                    break;
                }

            }

        }
        
        if (destroy) {
            // Clearing all Hashmaps and generate new Documentations
            this.hashmap.clear();
            this.filesMap.clear();
            this.generateDocumentation(this.allFiles);
        }
    }
    


// returns true if XSL-Files hase been Modified
    public boolean hasFileModified(String key, Long newtime) throws Exception{
        Long oldtime = (Long) this.filesMap.get(key);
        // Long time = new Long(newtime);

        if (newtime != null && oldtime != null) {
            if (newtime.longValue() > oldtime.longValue()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
        
    }

    // Returns the Value for the all_documentation tree in the
    // Result Document
    public String[] getDocumentationValues() throws Exception{
        this.checkFile();
        
        TreeMap treemap = new TreeMap (this.hashmap);
    
        String id[] = new String[this.hashmap.size()];

        Collection menge = treemap.keySet();

        int i = 0;
        for (Iterator it = menge.iterator(); it.hasNext();) {

            String idTemp = (String) it.next();
            CoreDocumentation coreDoc = null;
                
            coreDoc = (CoreDocumentation) treemap.get(idTemp);

            id[i] = coreDoc.getValue();
            i++;
            
        }
    
        return id;
    }

    public HashMap getAllStylesheets() {
        return filesMap;
    }


// returns the Stylsheet for the CoreDoch Object
    public String getSimpleStylesheet(String key) throws Exception{

        CoreDocumentation coreDoc = null;
        String stylesheet = "";

        Collection menge = hashmap.entrySet();
        coreDoc = (CoreDocumentation) hashmap.get(key);
        stylesheet = coreDoc.getStyleSheet();

        return stylesheet;

    }


    public String getMode(String key) throws Exception {
        
        CoreDocumentation coreDoc = null;
        String mode = "";

        Collection menge = hashmap.entrySet();
        coreDoc = (CoreDocumentation) hashmap.get(key);
        mode = coreDoc.getMode();

        return mode;
        
        
    }
    
    public String getModus(String key) throws Exception {
        CoreDocumentation coreDoc = null;
        String modus = "";

        Collection menge = hashmap.entrySet();
        coreDoc = (CoreDocumentation) hashmap.get(key);
        modus = coreDoc.getModus();

        return modus;
    }


    public Document getCurrentDoc(String key) throws Exception {
        
        this.checkFile();    
        CoreDocumentation coreDoc = null;
        Document doc = null;
        
        Collection menge = hashmap.entrySet();
        coreDoc = (CoreDocumentation) hashmap.get(key);
        doc = coreDoc.getDocument();
        return doc;
    }


    // Returns the Nodelist
    public NodeList getDocumentationNode(String key) throws Exception {
    
        this.checkFile();

        CoreDocumentation coreDoc = null;
        NodeList nlist = null;

        Collection menge = hashmap.entrySet();
        coreDoc = (CoreDocumentation) hashmap.get(key);

        nlist = coreDoc.getNodeList();


        NodeList nl = this.doc.getElementsByTagName(key);
            
        return nlist;
    }


    public CoreDocumentation getCoreDocumentationForId(String key) {
        return (CoreDocumentation) hashmap.get(key);
    }

    
    // Returns array of the Documentation Ids
    public String[] getDocumentationIds() throws Exception{
            
        
        this.checkFile();
        
        
        TreeMap treemap = new TreeMap (this.hashmap);                
        String id[] = new String[this.hashmap.size()];
        
        Collection menge = treemap.keySet();
        

        int i = 0;
        for (Iterator it = menge.iterator(); it.hasNext();) {
            id[i] = it.next().toString();
            i++;
        }

        return id;

    }
    
    
    
    

}
