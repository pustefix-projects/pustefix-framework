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



import de.schlund.pfixcore.util.Meminfo;
import de.schlund.pfixxml.*;
import de.schlund.pfixxml.targets.cachestat.SPCacheStatistic;
import de.schlund.pfixxml.util.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.log4j.Category;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * The TargetGenerator holds all the targets belonging to a certain
 * project (as defined by the config file used to init the Generator).
 *
 *
 *
 */

public class TargetGenerator {
    public static final String XSLPARAM_TG   = "__target_gen";
    public static final String XSLPARAM_TKEY = "__target_key";
    public static final String CACHEDIR      = ".cache";
        
    private static Category               CAT                            = Category.getInstance(TargetGenerator.class.getName());
    private static TargetGenerationReport report                         = new TargetGenerationReport();
    private DependencyRefCounter          refcounter                     = new DependencyRefCounter();
    private PageTargetTree                pagetree                       = new PageTargetTree();
    private HashMap                       alltargets                     = new HashMap();
    private boolean                       isGetModTimeMaybeUpdateSkipped = false;
    private long                          config_mtime                   = 0;
    private String name;
    private String[] global_themes;
    
    private String language;
    
    /* All registered TargetGenerationListener */
    private Set listeners = new HashSet();

    // needed during load.
    private int unnamedcount = 0;
    
    //--
    
    public TargetGenerator(Path confile) throws IOException, SAXException, XMLException {
        File tmp          = confile.resolve();
        this.config_mtime = tmp.lastModified();

        Meminfo.print("TG: Before loading " + confile.getRelative());
        loadConfig(confile);
        Meminfo.print("TG: after loading targets for " + confile.getRelative());
    }

    //-- attributes
    
    public String getName() {
        return name;
    }

    public String[] getGlobalThemes() {
        return global_themes;
    }

    public String getLanguage() {
        return language;
    }

    public Path getDisccachedir() {
        return PathFactory.getInstance().createPath(CACHEDIR + File.separatorChar + getName());
    }
    
    public PageTargetTree getPageTargetTree() {
        return pagetree;
    }

    //-- targets
    
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
        return createTarget(TargetType.XML_LEAF, key);
    }

    public Target createXSLLeafTarget(String key) {
        return createTarget(TargetType.XSL_LEAF, key);
    }

    //-- misc
    
    public void addListener(TargetGeneratorListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TargetGeneratorListener listener) {
        listeners.remove(listener);
    }
    
    
    public DependencyRefCounter getDependencyRefCounter() {
        return refcounter;
    }

    public String toString() {
        return "[TG: " + getName() + "; " + alltargets.size() + " targets defined.]";
    }

    // *******************************************************************************************

    
    public synchronized boolean tryReinit(Path confile) throws Exception {
        File tmp = confile.resolve();
        if (tmp.lastModified() > config_mtime) {
            CAT.warn(
                "\n\n###############################\n"
                + "#### Reloading depend file: "
                + confile.getRelative()
                + "\n"
                + "###############################\n");
            refcounter   = new DependencyRefCounter();
            pagetree     = new PageTargetTree();
            alltargets   = new HashMap();
            config_mtime = tmp.lastModified();
            loadConfig(confile);
            return true;
        } else {
            return false;
        }
    }

    private void loadConfig(Path confile) throws XMLException, IOException, SAXException {
        CAT.warn("\n***** CAUTION! ***** loading config " + confile.getRelative() + "...");
        Document config;

        config = Xml.parseMutable(confile.resolve());
        
        Element  root    = (Element) config.getElementsByTagName("make").item(0);
        NodeList targetnodes = config.getElementsByTagName("target");

        name = getAttribute(root, "project");
        language = getAttribute(root, "lang");

        String gl_theme_str = null;
        if (root.hasAttribute("themes")) {
            gl_theme_str = getAttribute(root, "themes");
        }
        if (gl_theme_str == null || gl_theme_str.equals("")) {
            gl_theme_str = name + " default"; 
        }
        
        StringTokenizer gl_tok  = new StringTokenizer(gl_theme_str);
        ArrayList       gl_list = new ArrayList();
        while (gl_tok.hasMoreElements()) {
            String currtok = gl_tok.nextToken();
            gl_list.add(currtok);
        }
        global_themes = (String[]) gl_list.toArray(new String[]{});
        
        File disccache = getDisccachedir().resolve();
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
            String       nameattr = node.getAttribute("name");
            String       type   = node.getAttribute("type");
            String       themes = node.getAttribute("themes");
            TargetStruct struct = new TargetStruct(nameattr, type, themes);
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
                    throw new XMLException("Defined VirtualTarget '" + nameattr + "' with depxml without a name");
                }
            } else {
                throw new XMLException("Defined VirtualTarget '" + nameattr + "' without [depxml]");
            }
            if (xslsub != null) {
                String xsldep = xslsub.getAttribute("name");
                if (xsldep != null) {
                    struct.setXSLDep(xsldep);
                    depxsls.add(xsldep);
                } else {
                    throw new XMLException("Defined VirtualTarget '" + nameattr + "' with depxsl without a name");
                }
            } else {
                throw new XMLException("Defined VirtualTarget '" + nameattr + "' without [depxsl]");
            }
            for (int j = 0; j < allaux.getLength(); j++) {
                Element aux     = (Element) allaux.item(j);
                Path auxname    = PathFactory.getInstance().createPath(aux.getAttribute("name"));
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
            params.put("docroot",  confile.getBase().getPath());
            struct.setParams(params);
            allstructs.put(nameattr, struct);
        }
        CAT.warn("\n=====> Preliminaries took " + (System.currentTimeMillis() - start) +
                 "ms. Now looping over " + allstructs.keySet().size() + " targets");
        start = System.currentTimeMillis();
        String tgParam = confile.getRelative();
        for (Iterator i = allstructs.keySet().iterator(); i.hasNext();) {
            TargetStruct struct = (TargetStruct) allstructs.get(i.next());
            createTargetFromTargetStruct(struct, allstructs, depxmls, depxsls, tgParam);
        }
        CAT.warn("\n=====> Creating targets took " + (System.currentTimeMillis() - start) + "ms. Now init pagetree");
        start = System.currentTimeMillis();
        pagetree.initTargets();
        CAT.warn("\n=====> Init of Pagetree took " + (System.currentTimeMillis() - start) + "ms. Ready...");
    }

    private TargetRW createTargetFromTargetStruct(TargetStruct struct, HashMap allstructs, HashSet depxmls, HashSet depxsls, String tgParam) throws XMLException {
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
                xmlsource = createTargetFromTargetStruct((TargetStruct) allstructs.get(xmldep), allstructs, depxmls, depxsls, tgParam);
            } 

            // Check if xsldep is a leaf node or virtual:
            if (!allstructs.containsKey(xsldep)) {
                xslsource = createTarget(TargetType.XSL_LEAF, xsldep);
            } else {
                xslsource = createTargetFromTargetStruct((TargetStruct) allstructs.get(xsldep), allstructs, depxmls, depxsls, tgParam);
            }

            VirtualTarget virtual = (VirtualTarget) createTarget(reqtype, key);
            virtual.setXMLSource(xmlsource);
            virtual.setXSLSource(xslsource);
            String themes = struct.getThemes();
            if (themes != null && !themes.equals("")) {
                StringTokenizer tok  = new StringTokenizer(themes);
                ArrayList       list = new ArrayList();
                while (tok.hasMoreElements()) {
                    String currtok = tok.nextToken();
                    list.add(currtok);
                }
                virtual.setThemes((String[]) list.toArray(new String[]{}));
            }
            
            AuxDependencyManager manager = virtual.getAuxDependencyManager();
            HashSet auxdeps = struct.getDepaux();
            for (Iterator i = auxdeps.iterator(); i.hasNext();) {
                Path path = (Path) i.next();
                manager.addDependency(DependencyType.FILE, path, null, null, null, null, null);
            }
            
            HashMap params       = struct.getParams();
            String  pageparam    = null;
            String  variantparam = null;
            
            // we want to remove already defined params (needed when we do a reload)
            virtual.resetParams();
            for (Iterator i = params.keySet().iterator(); i.hasNext();) {
                String pname = (String) i.next();
                String value = (String) params.get(pname);
                CAT.debug("* Adding Param " + pname + " with value " + value);
                virtual.addParam(pname, value);
                if (pname.equals("page")) {
                    pageparam = value;
                }
                if (pname.equals("variant")) {
                    variantparam = value;
                }
            }
            virtual.addParam(XSLPARAM_TG, tgParam);
            virtual.addParam(XSLPARAM_TKEY, key);

            if (!depxmls.contains(key) && !depxsls.contains(key)) {
                // it's a toplevel target...
                if (pageparam == null) {
                    CAT.warn("*** WARNING *** Target '" + key + "' is top-level, but has no 'page' parameter set! Will use 'Unnamed_"
                             + unnamedcount + "' instead");
                    pageparam = "Unamed_" + unnamedcount++;
                }
                PageInfo info = PageInfoFactory.getInstance().getPage(this, pageparam, variantparam);
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
        String themes;
        
        public TargetStruct(String name, String type, String themes) {
            this.name   = name;
            this.type   = type;
            this.themes = themes;
        }

        public String getThemes() {
            return themes;
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

        if (args.length > 1) {
            File docroot = new File(args[0]);
            if (!docroot.exists() || !docroot.isDirectory()) {
                throw new IllegalArgumentException("*** First argument has to be the docroot directory! ***");
            }
            PathFactory.getInstance().init(docroot.getPath());
            
            for (int i = 1; i < args.length; i++) {
                try {
                    /* resetting the factories for better memory performance */
                    TargetGenerator.resetFactories();
                    System.gc();
                    
                    Path filepath = PathFactory.getInstance().createPath(args[i]);
                    File file = filepath.resolve();
                    if (file.exists() && file.canRead() && file.isFile()) {
                        gen = TargetGeneratorFactory.getInstance().createGenerator(filepath);
                        gen.setIsGetModTimeMaybeUpdateSkipped(false);
                        System.out.println("---------- Doing " + args[i] + "...");
                        gen.generateAll();
                        System.out.println("---------- ...done [" + args[i] + "]");
                        TargetGeneratorFactory.getInstance().remove(filepath);
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
            CAT.error("Need docroot and configfile(s) to work on");
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
                System.out.println(">>>>> Generating " + getDisccachedir().getRelative() + File.separator +
                                   current.getTargetKey() + " from " + current.getXMLSource().getTargetKey() +
                                   " and " + current.getXSLSource().getTargetKey());
                
                boolean needs_update = false;
                needs_update = current.needsUpdate();
                if (needs_update) {
                    try {
                        current.getValue();
                        notifyListenerTargetDone(current);
                    } catch(TargetGenerationException tgex) {
                        notifyListenerTargetException(current,tgex);
                        report.addError(tgex, getName());
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
