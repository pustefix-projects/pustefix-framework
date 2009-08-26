/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.util;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.schlund.pfixxml.util.FileUtils;
import de.schlund.pfixxml.util.Xml;

/**
 * DelLang.java
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */

public class DelLang {
    private final static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    //private String docroot;
    private Pattern pattern = Pattern.compile("^\\s*$");
    int onelangcount = 0;
    int multilangcount = 0;
    private File docroot;
    
    static {
        dbfac.setNamespaceAware(true);
    }
    
    public DelLang(String docroot) {
        this.docroot = new File(docroot);
    }

    public static void main(String[] args) throws Exception {
        String    docroot = args[0];
        String    files = args[1];
        if (files == null || docroot == null) {
            System.err.println("Usage: java DelLang DOCROOT includefilelist");
            System.exit(0);
        }
        DelLang dellang   = new DelLang(docroot);
        Logging.configure("generator_quiet.xml");
        dellang.transform(files);
    }

    public void transform(String files) throws Exception {

        BufferedReader input  = new BufferedReader(new FileReader(files));
        Set<File>      inames = new TreeSet<File>();
        String         line;
        
        while ((line = input.readLine()) != null) {
        	inames.add(new File(docroot, line.substring(2)));
        }
        input.close();
        

        Document doc;
        
        for (Iterator<File> i = inames.iterator(); i.hasNext();) {
            File path = i.next();
            
            System.out.print(path + ":");
            
            doc = Xml.parseMutable(path);
            handleDoc(doc);
            
            File out = File.createTempFile("temp", ".TEMPFILE");
            out.getParentFile().mkdirs();
            Xml.serialize(doc, out, false, true);

            FileUtils.copyFile(out, path);
            out.delete();
            System.out.println("");
        }
        System.out.println("Multilang: " + multilangcount + " Only default lang: " + onelangcount);
    }
    
    
    public void handleDoc(Document doc) {
        
        Element  root                = doc.getDocumentElement();
        // System.out.println("NN: " + root.getNodeName());
        NodeList rootchildren        = root.getChildNodes();
        int      localmultilangcount = 0;
        
        for (int i = 0; i < rootchildren.getLength(); i++) {
            Node rootchild = rootchildren.item(i);
            if (rootchild.getNodeType() == Node.ELEMENT_NODE && rootchild.getNodeName().equals("part")) {
                String   partname       = ((Element) rootchild).getAttribute("name");
                NodeList partchildren   = rootchild.getChildNodes();
                for (int j = 0; j < partchildren.getLength(); j++) {
                    Node partchild = partchildren.item(j);
                    if (partchild.getNodeType() == Node.ELEMENT_NODE && partchild.getNodeName().equals("theme")) {
                        String   themename     = ((Element) partchild).getAttribute("name");
                        NodeList prodchildren    = partchild.getChildNodes();
                        int      count           = prodchildren.getLength();
                        boolean  multilang       = false;
                        // First of all we check if there is any other lang node than "<lang name="default">".
                        for (int k = 0; k < count; k++) {
                            Node tmp = prodchildren.item(k);
                            if (tmp.getNodeType() == Node.ELEMENT_NODE) {
                                if (tmp.getNodeName().equals("lang")) {
                                    Element tmpelem    = (Element) tmp;
                                    if (!tmpelem.getAttribute("name").equals("default")) {
                                        multilang = true;
                                    }
                                } else {
                                    System.out.println("*** Wrong element " + tmp.getNodeName() +
                                                       " under part/theme " + partname + "/" + themename);
                                    System.exit(1);
                                }
                            }
                        }
                        if (multilang) {
                                multilangcount++;
                                localmultilangcount++;
                        } else {
                            onelangcount++;
                        }
                        Element  theme = doc.createElement("theme");
                        theme.setAttribute("name", ((Element) partchild).getAttribute("name"));
                        
                        Element pfxlang = null;
                        if (multilang) {
                            pfxlang = doc.createElement("pfx:langselect");
                            theme.appendChild(pfxlang);
                        }
                        
                        for (int k = 0; k < count; k++) {
                            Node prodchild = prodchildren.item(0); // Stupid stuff...
                            partchild.removeChild(prodchild);
                            if (prodchild.getNodeType() == Node.ELEMENT_NODE && !multilang) {
                                NodeList langchildren = prodchild.getChildNodes();
                                int      langcount    = langchildren.getLength();
                                for (int l = 0; l < langcount ; l++) {
                                    Node langchild = langchildren.item(0);
                                    prodchild.removeChild(langchild);
                                    theme.appendChild(langchild);
                                }
                            } else if (!multilang) {
                                if ((k == 0 || k == (count - 1)) && prodchild.getNodeType() == Node.TEXT_NODE) {
                                    Matcher matcher = pattern.matcher(prodchild.getNodeValue());
                                    if (!matcher.matches()) {
                                        System.out.println("==>" + partname + "/" + themename + ":" + prodchild.getNodeValue());
                                        theme.appendChild(prodchild);
                                    }
                                }
                            } else if (prodchild.getNodeType() == Node.ELEMENT_NODE) {
                                Element langinst = doc.createElement("pfx:lang");
                                langinst.setAttribute("name", ((Element) prodchild).getAttribute("name"));
                                NodeList langchildren = prodchild.getChildNodes();
                                int      langcount    = langchildren.getLength();
                                for (int l = 0; l < langcount ; l++) {
                                    Node langchild = langchildren.item(0); // 
                                    prodchild.removeChild(langchild);
                                    langinst.appendChild(langchild);
                                }
                                pfxlang.appendChild(langinst);
                            } else {
                                pfxlang.appendChild(prodchild);
                            }
                            
                        }
                        rootchild.replaceChild(theme, partchild);
                    }
                }
            }
        }
        System.out.print(" (" + localmultilangcount + ") "); 
    }
    
    
} 
