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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.schlund.pfixcore.util.Meminfo;
import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.targets.cachestat.SPCacheStatistic;
import de.schlund.pfixxml.util.Path;
import de.schlund.pfixxml.util.Xml;

/**
 * The TargetGenerator holds all the targets belonging to a certain
 * project (as defined by the config file used to init the Generator).
 *
 *
 *
 */

public class TargetGenerator {
    public static final String XSLPARAM_TG              = "__target_gen";
    public static final String XSLPARAM_TKEY            = "__target_key";
        
    private static Category               CAT                            = Category.getInstance(TargetGenerator.class.getName());
    private static TargetGenerationReport report                         = new TargetGenerationReport();
    private DependencyRefCounter          refcounter                     = new DependencyRefCounter();
    private PageTargetTree                pagetree                       = new PageTargetTree();
    private HashMap                       alltargets                     = new HashMap();
    private boolean                       isGetModTimeMaybeUpdateSkipped = false;
    private long                          config_mtime                   = 0;
    private final File                    docroot;
    private final String                  fullname;
    
    private String name;
    private String language;
    
    /* All registered TargetGenerationListener */
    private Set listeners = new HashSet();

    // needed during load.
    private int unnamedcount = 0;
    
    //--
    
    public TargetGenerator(File confile) throws Exception {
        this.docroot = findDocroot(confile);
        this.config_mtime = confile.lastModified();
        this.fullname = confile.getCanonicalPath();

        Meminfo.print("TG: Before loading " + confile);
        loadConfig(confile);
        Meminfo.print("TG: after loading targets for " + confile.getPath());
    }

    public void addListener(TargetGeneratorListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TargetGeneratorListener listener) {
        listeners.remove(listener);
    }
    
    
    public DependencyRefCounter getDependencyRefCounter() {
        return refcounter;
    }
    public PageTargetTree getPageTargetTree() {
        return pagetree;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getConfigname() {
        return fullname;
    }
    public File getDisccachedir() {
        return new File(getDocroot(), ".cache" + File.separatorChar + getName());
    }
    
    public File getRecorddir() {
        return new File(getDocroot(), "record_dir" + File.separatorChar + getName());
    }

    public File getDocroot() {
        return docroot;
    }

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

    public Target createXMLLeafTarget(String key) {
        return (Target) createTarget(TargetType.XML_LEAF, key);
    }

    public Target createXSLLeafTarget(String key) {
        return (Target) createTarget(TargetType.XSL_LEAF, key);
    }

    public String toString() {
        return "[TG: " + getConfigname() + "; " + alltargets.size() + " targets defined.]";
    }

    // -------------------------------------------------------------------------------------------

    public static File findDocroot(File file) { // TODO
        file = file.getAbsoluteFile();
        while (!isDocroot(file.getName())) {
            file = file.getParentFile();
        }
        return file;
    }
    private static boolean isDocroot(String name) {
        return name.equals("projects") || name.equals("example");
    }
    
    public synchronized boolean tryReinit(File confile) throws Exception {
        if (confile.lastModified() > config_mtime) {
            CAT.warn(
                "\n\n###############################\n"
                + "#### Reloading depend file: "
                + confile.getAbsoluteFile()
                + "\n"
                + "###############################\n");
            refcounter   = new DependencyRefCounter();
            pagetree     = new PageTargetTree();
            alltargets   = new HashMap();
            config_mtime = confile.lastModified();
            loadConfig(confile);
            return true;
        } else {
            return false;
        }
    }

    // *******************************************************************************************

    private void loadConfig(File confile) throws Exception {
        CAT.warn("\n***** CAUTION! ***** loading config " + confile + "...");
        Document config;

        config = Xml.parse(confile);

        Element  root    = (Element) config.getElementsByTagName("make").item(0);
        NodeList targetnodes = config.getElementsByTagName("target");

        name = getAttribute(root, "project");
        language = getAttribute(root, "lang");
        
        File disccache = getDisccachedir();
        if (!disccache.exists()) {
            disccache.mkdirs();
        } else if (!disccache.isDirectory() || !disccache.canWrite() || !disccache.canRead()) {
            throw new XMLException("Directory " + disccache + " is not writeable, readeable or is no directory");
        }

        HashSet depxmls = new HashSet();
        HashSet depxsls = new HashSet();
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
                Path auxname    = Path.create(getDocroot(), aux.getAttribute("name"));
                depaux.add(auxname);
            }
            struct.setDepaux(depaux);
            for (int j = 0; j < allpar.getLength(); j++) {
                Element par     = (Element) allpar.item(j);
                String  parname = par.getAttribute("name");
                if ("docroot".equals(parname)) {
                    throw new XMLException("docroot is no longer needed");
                }
                String  value   = par.getAttribute("value");
                params.put(parname, value);
            }
            params.put("docroot", docroot.getPath());
            struct.setParams(params);
            allstructs.put(name, struct);
        }
        CAT.warn("\n=====> Preliminaries took " + (System.currentTimeMillis() - start) +
                 "ms. Now looping over " + allstructs.keySet().size() + " targets");
        start = System.currentTimeMillis();
        for (Iterator i = allstructs.keySet().iterator(); i.hasNext();) {
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
                throw new XMLException("Already have a target '" + key + "' with type " + tmp.getType() + ". Requested type was '" + reqtype + "'");
            }
        } else {
            String xmldep = struct.getXMLDep();
            String xsldep = struct.getXSLDep();
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

            VirtualTarget virtual = (VirtualTarget) createTarget(reqtype, key);
            virtual.setXMLSource(xmlsource);
            virtual.setXSLSource(xslsource);

            AuxDependencyManager manager = virtual.getAuxDependencyManager();
            HashSet auxdeps = struct.getDepaux();
            for (Iterator i = auxdeps.iterator(); i.hasNext();) {
                Path path = (Path) i.next();
                manager.addDependency(DependencyType.FILE, path, null, null, null, null, null);
            }
            
            HashMap params    = struct.getParams();
            String  pageparam = null;
            // we want to remove already defined params (needed when we do a reload)
            virtual.resetParams();
            for (Iterator i = params.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                String value = (String) params.get(name);
                CAT.debug("* Adding Param " + name + " with value " + value);
                virtual.addParam(name, value);
                if (name.equals("page")) {
                    pageparam = value;
                }
            }
            virtual.addParam(XSLPARAM_TG, Path.getRelativeString(getDocroot(), fullname));
            virtual.addParam(XSLPARAM_TKEY, key);

            if (!depxmls.contains(key) && !depxsls.contains(key)) {
                // it's a toplevel target...
                if (pageparam == null) {
                    CAT.warn("*** WARNING *** Target '" + key + "' is top-level, but has no 'page' parameter set! Will use 'Unnamed_"
                             + unnamedcount + "' instead");
                    pageparam = "Unamed_" + unnamedcount++;
                }
                PageInfo info = PageInfoFactory.getInstance().getPage(this, pageparam);
                pagetree.addEntry(info, virtual);
            }
            return virtual;
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

    private TargetRW createTarget(TargetType type, String key) {
        TargetFactory tfac = TargetFactory.getInstance();
        TargetRW tmp = tfac.getTarget(type, this, key);
        TargetRW tmp2 = getTargetRW(key);
        if (tmp2 == null) {
            synchronized (alltargets) {
                alltargets.put(tmp.getTargetKey(), tmp);
            }
        } else if (tmp != tmp2) {
            throw new RuntimeException("Requesting Target '" + key + "' of type " + tmp.getType() + ", but already have a Target of type "
                                       + tmp2.getType() + " with the same key in this Generator!");
        }
        return tmp;
    }

    private class TargetStruct {
        HashSet depaux; 
        HashMap params;
        String type;
        String name;
        String xsldep;
        String xmldep;

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

    public static void main(String[] args) {
        TargetGenerator gen = null;

        String log4jconfig = System.getProperty("log4jconfig");
        if (log4jconfig == null || log4jconfig.equals("")) {
            System.out.println("*** FATAL: Need the log4jconfig property. Exiting... ***");
            System.exit(-1);
        }
        DOMConfigurator.configure(log4jconfig);

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                try {
                    /* resetting the factories for better memory performance */
                    TargetGenerator.resetFactories();
                    System.gc();
                     
                    File file = new File(args[i]);
                    if (file.exists() && file.canRead() && file.isFile()) {
                        gen = TargetGeneratorFactory.getInstance().createGenerator(file);
                        gen.setIsGetModTimeMaybeUpdateSkipped(false);
                        System.out.println("---------- Doing " + args[i] + "...");
                        gen.generateAll();
                        System.out.println("---------- ...done [" + args[i] + "]");
                        TargetGeneratorFactory.getInstance().remove(file);
                    } else {
                        CAT.error("Couldn't read configfile '" + args[i] + "'");
                        throw (new XMLException("Oops!"));
                    }
                } catch (Exception e) {
                    CAT.error("Oops! TargetGenerator exit!", e);
                    System.exit(-1);
                }
            }
            
            System.out.println(report.toString());
           
        } else {
            CAT.error("Need configfile to work on");
        }
    }

    /**
     * Make sure this target generator object is properly configured before calling this method.
     * To obtain a propely configured TargetGenerator Object follow these steps:
     * <ul>
     * <li/><code>String log4jconfig = System.getProperty("log4jconfig"); DOMConfigurator.configure(log4jconfig);</code>
     * <li/>{@link TargetGenerator} gen = {@link TargetGeneratorFactory}.{@link TargetGeneratorFactory#getInstance()}.{@link TargetGeneratorFactory#createGenerator(String)};
     * <li/>gen.{@link TargetGenerator#setIsGetModTimeMaybeUpdateSkipped(boolean)};
     * </ul>
     * @throws Exception
     */
    public void generateAll() throws Exception {
        for (Iterator e = getAllTargets().keySet().iterator(); e.hasNext();) {
            Target current = getTarget((String) e.next());
            if (current.getType() != TargetType.XML_LEAF && current.getType() != TargetType.XSL_LEAF) {
                StringBuffer buf = new StringBuffer();
                buf.append(">>>>> Generating ").append(getDisccachedir()).append(File.separator).append(current.getTargetKey())
                    .append(" from ").append(current.getXMLSource().getTargetKey()).append(" and ") .append(current.getXSLSource().getTargetKey());
                System.out.println(buf.toString());
                
                boolean needs_update = false;
                needs_update = current.needsUpdate();
                if (needs_update) {
                    try {
                        current.getValue();
                        notifyListenerTargetDone(current);
                    } catch(TargetGenerationException tgex) {
                        notifyListenerTargetException(current,tgex);
                        report.addError(tgex, getConfigname());
                        tgex.printStackTrace();
                    }
                }
                else {
                    notifyListenerTargetDone(current);
                }
                System.out.println("done.");
            }
            else {
                notifyListenerTargetDone(current);
            }
            /* if all listeners want to stop, 
             * there is no point in continuing ... */
            if (needsToStop()) {
                break;
            }
        }
    }
    
    /**
     * This method checks, if a TargetGeneratorListener wants to stop,
     * if so he will get kicked out of the listener set. 
     * 
     * @return true if all listeners want to stop
     */
    private boolean needsToStop() {
        boolean result = false;
        if (listeners.size() > 0) {
            result = true;
            for (Iterator it=listeners.iterator();it.hasNext();) {
                TargetGeneratorListener listener = (TargetGeneratorListener) it.next();
                if (listener.needsStop()) {
                    result = result && true;
                    it.remove();
                }
                else {
                    result = false;
                }
            }
        }
        return result;
    }
    
    
    /**
     * This calls the finishedTarget method of all registered listeners
     * @param target the finished target
     */
    private void notifyListenerTargetDone(Target target) {
        for (Iterator it = listeners.iterator();it.hasNext();) {
            TargetGeneratorListener listener = (TargetGeneratorListener) it.next();
            listener.finishedTarget(target);
        }
    }

    /**
     * This calls the generationException method of all registered listeners
     * @param target the finished target
     * @param tgex the exception!
     */
    private void notifyListenerTargetException(Target target,TargetGenerationException tgex) {
        for (Iterator it = listeners.iterator();it.hasNext();) {
            TargetGeneratorListener listener = (TargetGeneratorListener) it.next();
            listener.generationException(target,tgex);
        }
    }
    
    
    
    /**
     * Returns the isGetModTimeMaybeUpdateSkipped.
     * @return boolean
     */
    public boolean isGetModTimeMaybeUpdateSkipped() {
        return isGetModTimeMaybeUpdateSkipped;
    }

    /**
     * Sets the isGetModTimeMaybeUpdateSkipped.
     * @param isGetModTimeMaybeUpdateSkipped The isGetModTimeMaybeUpdateSkipped to set
     */
    public void setIsGetModTimeMaybeUpdateSkipped(boolean isGetModTimeMaybeUpdateSkipped) {
        this.isGetModTimeMaybeUpdateSkipped = isGetModTimeMaybeUpdateSkipped;
    }

    /**
     * @return report containing sensilbe information after {@link #generateAll()}, not null
     */
    public static String getReportAsString() {
        return report.toString();
    }

    public static void resetGenerationReport() {
        report = new TargetGenerationReport();
    }

    public static void resetFactories() {
        SPCacheStatistic.reset();
        TargetGeneratorFactory.getInstance().reset();
        TargetGenerator.resetGenerationReport();
        TargetFactory.getInstance().reset();
        IncludeDocumentFactory.getInstance().reset();
        PageInfoFactory.getInstance().reset();
        SharedLeafFactory.getInstance().reset();
        AuxDependencyFactory.getInstance().reset();
    }

    //--
    
    private static String getAttribute(Element node, String name) throws XMLException {
        String value;
        
        value = getAttributeOpt(node, name);
        if (value == null) {
            throw new XMLException("missing attribute: " + name);
        }
        return value;
    }

    private static String getAttributeOpt(Element node, String name) {
        Attr attr;
        
        attr = node.getAttributeNode(name);
        if (attr == null) {
            return null;
        }
        return attr.getValue();
    }
}

class TargetGenerationReport {
    private HashMap hash = new HashMap();
    
    TargetGenerationReport() {}
    
    void addError(Exception e, String key) {
        if(hash.get(key) == null) {
            ArrayList list = new ArrayList();
            list.add(e);
            hash.put(key, list);
        } else {
            ((ArrayList) hash.get(key)).add(e);
        }
    }
    
    public String toString() {
        StringBuffer buf        = new StringBuffer(255);
        String       prod_break = "'============================================================================================'\n";
        String       ex_break   = "|----------------------------------------------------------------------------------\n";
        if (hash.isEmpty()) {
            StringBuffer sb = new StringBuffer();            
            sb.append(prod_break);
            sb.append("| No exceptions\n");
            sb.append(prod_break);
            return sb.toString();
        }
        
        Iterator iter = hash.keySet().iterator();
        
        while(iter.hasNext()) {
           Object key = iter.next();
           buf.append(prod_break);    
           buf.append("| Config: ").append((String) key).append("\n");
           ArrayList exs = (ArrayList) hash.get(key);
           buf.append("| Exceptions: ").append("\n");
           for(int i=0; i<exs.size(); i++) {
               TargetGenerationException tgex = (TargetGenerationException) exs.get(i);
               String str = tgex.toStringRepresentation();
               buf.append(str);
               if(exs.size() - 1 > i)
                    buf.append(ex_break);
           }
           buf.append(prod_break);
        }
        return buf.toString();
    }
}
