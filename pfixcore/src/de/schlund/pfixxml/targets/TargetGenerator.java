/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

package de.schlund.pfixxml.targets;

import de.schlund.pfixxml.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import de.schlund.pfixcore.util.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.apache.log4j.*;
import org.apache.log4j.xml.*;
import javax.xml.parsers.*;
import org.apache.xpath.*;

/**
 * The TargetGenerator holds all the targets belonging to a certain
 * project (as defined by the config file used to init the Generator).
 *
 *
 *
 */

public class TargetGenerator {
    private static Category               CAT   = Category.getInstance(TargetGenerator.class.getName());
    private static DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
    private        DependencyRefCounter   refcounter = new DependencyRefCounter();
    private        PageTargetTree         pagetree   = new PageTargetTree();
    private        HashMap                alltargets = new HashMap();
    private        String                 configname;
    private        File                   confile;
    private        long                   config_mtime = 0;
    private        String                 disccachedir;
    private        String                 docroot;
    
    
    // needed during load.
    private int unnamedcount = 0;

    public DependencyRefCounter getDependencyRefCounter() { return refcounter; }
    public PageTargetTree       getPageTargetTree() { return pagetree; }
    public String               getConfigname() { return(configname); }
    public String               getDisccachedir() { return(disccachedir); }
    public String               getDocroot() { return(docroot); }
    
    public TreeMap getAllTargets() {
        synchronized (alltargets) {
            return new TreeMap(alltargets);
        }
    }

    public Target getTarget(String key) {
        synchronized (alltargets) {
            return (Target) alltargets.get(key);
        }
    }

    public Target createXMLLeafTarget (String key) {
        return (Target) createTarget(TargetType.XML_LEAF, key);
    }

    public Target createXSLLeafTarget (String key) {
        return (Target) createTarget(TargetType.XSL_LEAF, key);
    }

    public String toString() {
        return "[TG: " + getConfigname() + "; " + alltargets.size() + " targets defined.]";
    }
    
    // -------------------------------------------------------------------------------------------

    public TargetGenerator (File confile) throws Exception {
        this.confile = confile;
        Meminfo.print("TG: Before loading " + confile.getPath());
        configname   = confile.getCanonicalPath();
        config_mtime = confile.lastModified();
        loadConfig();
        Meminfo.print("TG: after loading targets for " + confile.getPath());
    }

    public synchronized boolean tryReinit() throws Exception {
        if (confile.lastModified() > config_mtime) {
            CAT.warn("\n\n###############################\n" +
                     "#### Reloading depend file: " + confile.getAbsoluteFile() + "\n" +
                     "###############################\n");
            refcounter   = new DependencyRefCounter();
            pagetree     = new PageTargetTree();
            alltargets   = new HashMap();
            config_mtime = confile.lastModified();
            loadConfig();
            return true;
        } else {
            return false;
        }
    }
    
    // *******************************************************************************************

    private void loadConfig () throws Exception {
        DocumentBuilder domp = dbfac.newDocumentBuilder();

        CAT.warn("\n***** CAUTION! ***** loading config " + configname + "...");
        Document  config;
        
        try {
            config = domp.parse(configname);
        } catch (SAXException e) {
            CAT.error("\nConfigfile '" + configname + "' couldn't be parsed by XML parser: \n"+e.toString());
            throw e;
        } catch (IOException e) {
            CAT.error("\nConfigfile '" + configname + "' I/O Error: \n" + e.toString());
            throw e;
        }

        Element  makenode    = (Element) config.getElementsByTagName("make").item(0);
        NodeList targetnodes = config.getElementsByTagName("target");
        
        disccachedir = makenode.getAttribute("cachedir") + "/";
        CAT.debug("* Set CacheDir to " + disccachedir);
        File cache = new File(disccachedir);
        if (!cache.exists()) {
            cache.mkdirs();
        } else if (!cache.isDirectory() || !cache.canWrite() || !cache.canRead()) {
            XMLException e = new XMLException ("Directory " + disccachedir +
                                               " is not writeable,readeable or is no directory");
            throw(e);
        }

        docroot = makenode.getAttribute("docroot") + "/";
        CAT.debug("* Set docroot to " + docroot);

        HashSet depxmls    = new HashSet();
        HashSet depxsls    = new HashSet();
        HashMap allstructs = new HashMap();
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < targetnodes.getLength(); i++) {
            Element      node   = (Element) targetnodes.item(i);
            String       name   = node.getAttribute("name");
            String       type   = node.getAttribute("type");   
            TargetStruct struct = new TargetStruct(name, type);
            HashMap      params = new HashMap();
            HashSet      depaux = new HashSet();
            Element      xmlsub = (Element) node.getElementsByTagName("depxml").item(0);
            Element      xslsub = (Element) node.getElementsByTagName("depxsl").item(0);
            NodeList     allaux = node.getElementsByTagName("depaux");
            NodeList     allpar = node.getElementsByTagName("param");
            if (xmlsub != null) {
                String xmldep = xmlsub.getAttribute("name");
                if (xmldep != null) {
                    struct.setXMLDep(xmldep);
                    depxmls.add(xmldep);
                } else {
                    throw new XMLException("Defined VirtualTarget '" + name + "' with depxml without a name");
                }
            } else {
                throw new XMLException("Defined VirtualTarget '" + name + "' without [depxml]");
            }
            if (xslsub != null) {
                String xsldep = xslsub.getAttribute("name");
                if (xsldep != null) {
                    struct.setXSLDep(xsldep);
                    depxsls.add(xsldep);
                } else {
                    throw new XMLException("Defined VirtualTarget '" + name + "' with depxsl without a name");
                }
            } else {
                throw new XMLException("Defined VirtualTarget '" + name + "' without [depxsl]");
            }
            for (int j = 0; j < allaux.getLength(); j++) {
                Element aux     = (Element) allaux.item(j);
                String  auxname = aux.getAttribute("name");
                depaux.add(auxname);
            }
            struct.setDepaux(depaux);
            for (int j = 0; j < allpar.getLength(); j++) {
                Element par     = (Element) allpar.item(j);
                String  parname = par.getAttribute("name");
                String  value   = par.getAttribute("value");
                params.put(parname, value);
            }
            struct.setParams(params);
            allstructs.put(name, struct);
        }
        CAT.warn("\n=====> Preliminaries took " + (System.currentTimeMillis() - start) +
                 "ms. Now looping over " + allstructs.keySet().size() + " targets");
        start = System.currentTimeMillis();
        for (Iterator i = allstructs.keySet().iterator(); i.hasNext(); ) {
            TargetStruct struct = (TargetStruct) allstructs.get(i.next());
            createTargetFromTargetStruct(struct, allstructs, depxmls, depxsls);
        }
        CAT.warn("\n=====> Creating targets took " + (System.currentTimeMillis() - start) + "ms. Now init pagetree");
        start = System.currentTimeMillis();
        pagetree.initTargets();
        CAT.warn("\n=====> Init of Pagetree took " + (System.currentTimeMillis() - start) + "ms. Ready...");
    }

    private TargetRW createTargetFromTargetStruct(TargetStruct struct, HashMap allstructs, HashSet depxmls, HashSet depxsls) throws Exception {
        String     key     = struct.getName();
        String     type    = struct.getType();   
        TargetType reqtype = TargetType.getByTag(type);
        TargetRW   tmp     = getTargetRW(key);

        if (tmp != null) {
            if (reqtype == tmp.getType()) { 
                return tmp;
            } else {
                throw new XMLException("Already have a target '" + key + "' with type " + tmp.getType() +
                                       ". Requested type was '" + reqtype + "'");
            }
        } else {
            String   xmldep    = struct.getXMLDep();
            String   xsldep    = struct.getXSLDep();
            TargetRW xmlsource = null;
            TargetRW xslsource = null;
                        
            // We need to handle the xml/xsldep first.
            // Check if xmldep is a leaf node or virtual:
            
            if (!allstructs.containsKey(xmldep)) {
                xmlsource = createTarget(TargetType.XML_LEAF, xmldep);
            } else {
                xmlsource = createTargetFromTargetStruct((TargetStruct) allstructs.get(xmldep), allstructs, depxmls, depxsls);
            }
            
            // Check if xsldep is a leaf node or virtual:
            if (!allstructs.containsKey(xsldep)) {
                xslsource = createTarget(TargetType.XSL_LEAF, xsldep);
            } else {
                xslsource = createTargetFromTargetStruct((TargetStruct) allstructs.get(xsldep), allstructs, depxmls, depxsls);
            }

            tmp = createTarget(reqtype, key);
            tmp.setXMLSource(xmlsource);
            tmp.setXSLSource(xslsource);

            AuxDependencyManager manager = tmp.getAuxDependencyManager();
            HashSet              auxdeps = struct.getDepaux();
            for (Iterator i = auxdeps.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                manager.addDependency(DependencyType.FILE, name, null, null, null, null, null);
            }
            
            HashMap params    = struct.getParams();
            String  pageparam = null;
            for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
                String name  = (String) i.next();
                String value = (String) params.get(name);
                CAT.debug("* Adding Param " + name + " with value " + value);
                tmp.addParam(name, value);
                if (name.equals("page")) {
                    pageparam = value;
                }
            }
            tmp.addParam("__target_gen", configname);
            tmp.addParam("__target_key", key);

            if (!depxmls.contains(key) && !depxsls.contains(key)) {
                // it's a toplevel target...
                if (pageparam == null) {
                    CAT.warn("*** WARNING *** Target '" + key + "' is top-level, but has no 'page' parameter set! Will use 'Unnamed_"
                             + unnamedcount + "' instead");
                    pageparam = "Unamed_" + unnamedcount++;
                }
                PageInfo info = PageInfoFactory.getInstance().getPage(this, pageparam);
                pagetree.addEntry(info, tmp);
            }
            return tmp;
        }
    }

    // *******************************************************************************************
    private TargetRW getTargetRW(String key) {
        synchronized (alltargets) {
            return (TargetRW) alltargets.get(key);
        }
    }

    private TargetRW createXMLVirtualTarget(String key) {
        return createTarget(TargetType.XML_VIRTUAL, key);
    }
    
    private TargetRW createXSLVirtualTarget(String key) {
        return createTarget(TargetType.XSL_VIRTUAL, key);
    }

    private TargetRW createTarget (TargetType type, String key) {
        TargetFactory tfac = TargetFactory.getInstance();
        TargetRW      tmp  = tfac.getTarget(type, this, key);
        TargetRW      tmp2 = getTargetRW(key);
        if (tmp2 == null) {
            synchronized (alltargets) {
                alltargets.put(tmp.getTargetKey(), tmp);
            }
        } else if (tmp != tmp2) {
            throw new RuntimeException("Requesting Target '" + key + "' of type " + tmp.getType() +
                                       ", but already have a Target of type " + tmp2.getType() +
                                       " with the same key in this Generator!");
        }
        return tmp;
    }


    private class TargetStruct {
        HashSet depaux;
        HashMap params;
        String  type;
        String  name;
        String  xsldep;
        String  xmldep;

        public TargetStruct(String name, String type) {
            this.name = name;
            this.type = type;
        }
        
        public String getXMLDep() {
            return xmldep;
        }

        public void setXMLDep(String in) {
            this.xmldep = in;
        }

        public String getXSLDep() {
            return xsldep;
        }

        public void setXSLDep(String in) {
            this.xsldep = in;
        }

        public HashSet getDepaux() {
            return depaux;
        }

        public void setDepaux(HashSet in) {
            this.depaux = in;
        }

        public HashMap getParams() {
            return params;
        }

        public void setParams(HashMap in) {
            this.params = in;
        }

        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // *******************************************************************************************

    public static void main (String[] args) {
        TargetGenerator gen;

        String log4jconfig = System.getProperty("log4jconfig");
        if (log4jconfig == null || log4jconfig.equals("")) {
            System.out.println("*** FATAL: Need the log4jconfig property. Exiting... ***");
            System.exit(-1);
        }
        DOMConfigurator.configure(log4jconfig);
        
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                try {
                    File confile = new File(args[i]);
                    if (confile.exists() && confile.canRead() && confile.isFile()) {
                        gen = TargetGeneratorFactory.getInstance().createGenerator(args[i]);
                        System.out.println("---------- Doing " + args[i] + "...");
                        gen.generateAll();
                        System.out.println("---------- ...done [" + args[i] + "]");
                    } else {
                        CAT.error("Couldn't read configfile '" + args[i] + "'");
                        throw(new XMLException("Oops!"));
                    }
                } catch (Exception e) {
                    CAT.error("Oops!", e);
                }
            }
        } else {
            CAT.error("Need configfile to work on");
        }
    }

    protected void generateAll() throws Exception {
        for (Iterator e = getAllTargets().keySet().iterator(); e.hasNext(); ) {
            Target current = getTarget((String) e.next());
            if (current.getType() != TargetType.XML_LEAF &&
                current.getType() != TargetType.XSL_LEAF) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(">>>>> Generating ").append(getDisccachedir()).append(current.getTargetKey()).append(" from ").
                        append(current.getXMLSource().getTargetKey()).append(" and ").append(current.getXSLSource().getTargetKey());   
                    System.out.println(buf.toString());
                if (current.needsUpdate()) {
                    current.getValue();
                }
                System.out.println("done.");
            }
        }
    }
}
