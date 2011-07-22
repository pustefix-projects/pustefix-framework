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

package de.schlund.pfixxml.targets;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.pustefixframework.util.xml.DOMUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.Meminfo;
import de.schlund.pfixcore.workflow.SiteMap;
import de.schlund.pfixxml.IncludeDocumentFactory;
import de.schlund.pfixxml.IncludePartInfo;
import de.schlund.pfixxml.IncludePartsInfo;
import de.schlund.pfixxml.IncludePartsInfoFactory;
import de.schlund.pfixxml.IncludePartsInfoParsingException;
import de.schlund.pfixxml.Tenant;
import de.schlund.pfixxml.TenantInfo;
import de.schlund.pfixxml.Variant;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.config.includes.FileIncludeEvent;
import de.schlund.pfixxml.config.includes.FileIncludeEventListener;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.event.ConfigurationChangeEvent;
import de.schlund.pfixxml.event.ConfigurationChangeListener;
import de.schlund.pfixxml.resources.DocrootResource;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ModuleResource;
import de.schlund.pfixxml.resources.Resource;
import de.schlund.pfixxml.resources.ResourceFinder;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.resources.ResourceVisitor;
import de.schlund.pfixxml.util.FileUtils;
import de.schlund.pfixxml.util.SimpleResolver;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * The TargetGenerator holds all the targets belonging to a certain
 * project (as defined by the config file used to init the Generator).
 *
 */

public class TargetGenerator implements ResourceVisitor, ServletContextAware, InitializingBean {

    public static final String XSLPARAM_TG = "__target_gen";

    public static final String XSLPARAM_TKEY = "__target_key";
    
    public static final String XSLPARAM_SITEMAP = "__sitemap";

    public static final String CACHEDIR = ".cache";
    
    private static final char RENDER_KEY_SEPARATOR = '#';
    
    private static final Logger LOG = Logger.getLogger(TargetGenerator.class);

    private TargetGenerationReport report = new TargetGenerationReport();

    private PageTargetTree pagetree = new PageTargetTree();

    private HashMap<String, Target> alltargets = new HashMap<String, Target>();

    private boolean isGetModTimeMaybeUpdateSkipped = false;

    private long config_mtime = 0;

    private Set<Resource> configFileDependencies = new HashSet<Resource>();

    private String name;
    
    private XsltVersion xsltVersion=XsltVersion.XSLT1; //default, can be overridden in depend.xml 

    private Themes global_themes;
    
    private String default_theme;

    private String language;

    /* All registered TargetGenerationListener */
    private Set<TargetGeneratorListener> generationListeners = new HashSet<TargetGeneratorListener>();

    private Set<ConfigurationChangeListener> configurationListeners = Collections.synchronizedSet(new HashSet<ConfigurationChangeListener>());

    private Resource config_path;
    
    private SiteMap siteMap;
    
    private FileResource cacheDir;
    
    private SPCacheFactory cacheFactory;
    private IncludeDocumentFactory includeDocumentFactory;
    private AuxDependencyFactory auxDependencyFactory;
    private TargetDependencyRelation targetDependencyRelation;
    private TargetFactory targetFactory;
    private SharedLeafFactory sharedLeafFactory;
    private PageInfoFactory pageInfoFactory;
    private IncludePartsInfoFactory includePartsInfo;
    private boolean parseIncludes = true;

    private Map<String, String> renderParams;
    private ServletContext servletContext;
    private TenantInfo tenantInfo;
    
    //--

    public TargetGenerator(final Resource confile, final FileResource cacheDir, final boolean parseIncludes) throws IOException, SAXException, XMLException {
        this(confile, cacheDir, new SPCacheFactory().init(), new SiteMap(confile), parseIncludes);   
    }
    
    public TargetGenerator(final Resource confile, final FileResource cacheDir, final SPCacheFactory cacheFactory, final SiteMap siteMap) throws IOException, SAXException, XMLException {
        this(confile, cacheDir, cacheFactory, siteMap, true);
    }
        
    public TargetGenerator(final Resource confile, final FileResource cacheDir, final SPCacheFactory cacheFactory, final SiteMap siteMap, final boolean parseIncludes) throws IOException, SAXException, XMLException {
        this.config_path = confile;
        this.cacheDir = cacheDir;
        this.cacheFactory = cacheFactory;
        this.siteMap = siteMap;
        this.parseIncludes = parseIncludes;
    }
        
    public TargetGenerator(Resource confile) throws IOException, SAXException, XMLException {
        this(confile, null, new SPCacheFactory().init(), new SiteMap(confile));
    }
    
    public TargetGenerator(Resource confile, SPCacheFactory cacheFactory) throws IOException, SAXException, XMLException {
        this(confile, null, cacheFactory, new SiteMap(confile));
    }
    
    public TargetGenerator(Resource confile, SPCacheFactory cacheFactory, SiteMap siteMap) throws IOException, SAXException, XMLException {
        this(confile, null, cacheFactory, siteMap);
    }
    
    public void afterPropertiesSet() throws Exception {
        includeDocumentFactory = new IncludeDocumentFactory(cacheFactory);
        targetDependencyRelation = new TargetDependencyRelation();
        auxDependencyFactory = new AuxDependencyFactory(targetDependencyRelation);
        targetFactory = new TargetFactory();
        sharedLeafFactory = new SharedLeafFactory();
        pageInfoFactory = new PageInfoFactory();
        includePartsInfo = new IncludePartsInfoFactory();
        //TODO: factory init on reload
        Meminfo meminfo = new Meminfo();
        meminfo.print("TG: Before loading " + config_path.toString());
        loadConfig(config_path);
        meminfo.print("TG: after loading targets for " + config_path.toString());
    }
    
    //-- attributes
    
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    public XsltVersion getXsltVersion() {
        return xsltVersion;
    }

    public Themes getGlobalThemes() {
        return global_themes;
    }
    
    public String getDefaultTheme() {
        return default_theme;
    }

    public String getLanguage() {
        return language;
    }
    
    public Resource getConfigPath() {
        return config_path;
    }

    public FileResource getDisccachedir() {
        return cacheDir;
    }

    public PageTargetTree getPageTargetTree() {
        return pagetree;
    }
    
    public SiteMap getSiteMap() {
        return siteMap;
    }

    public SPCacheFactory getCacheFactory() {
        return cacheFactory;
    }
    
    public IncludeDocumentFactory getIncludeDocumentFactory() {
        return includeDocumentFactory;
    }
    
    public AuxDependencyFactory getAuxDependencyFactory() {
        return auxDependencyFactory;
    }
    
    public TargetDependencyRelation getTargetDependencyRelation() {
        return targetDependencyRelation;
    }
    
    public SharedLeafFactory getSharedLeafFactory() {
        return sharedLeafFactory;
    }
    
    public PageInfoFactory getPageInfoFactory() {
        return pageInfoFactory;
    }
    
    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }
    
    //-- targets

    public TreeMap<String, Target> getAllTargets() {
        synchronized (alltargets) {
            return new TreeMap<String, Target>(alltargets);
        }
    }

    public Target getTarget(String key) {
        synchronized (alltargets) {
            Target target = (Target) alltargets.get(key);
            return target;
        }
    }
    
    public Target getRenderTarget(String href, String part, String module, String search, Variant variant) throws IncludePartsInfoParsingException {
        String uri;
        if("dynamic".equals(search)) {
            uri = "dynamic:/" + href + "?part=" + part;
            if(module != null && !module.equals("")) {
                uri += "&module=" + module;
            }
        } else {
            if(module != null && !module.equals("")) {
                uri = "module://" + module + "/" + href;
            } else {
                uri = "docroot:/" + href;
            }
        }
        Resource res = ResourceUtil.getResource(uri);
        IncludePartsInfo info = includePartsInfo.getIncludePartsInfo(res);
        IncludePartInfo partInfo = info.getParts().get(part);
        if(partInfo != null) {
            if(partInfo.isRender()) {
                String selectedVariant = null;
                if(variant != null) {
                    String[] variants = variant.getVariantFallbackArray();
                    for (int i = 0; i < variants.length; i++) {
                        if(partInfo.getRenderVariants().contains(variants[i])) {
                            selectedVariant = variants[i];
                            break;
                        }
                    }
                }
                if("dynamic".equals(search)) {
                    if(res instanceof ModuleResource) {
                        module = res.toURI().getAuthority();
                    }
                }
                if(module == null || module.equals("")) module = "WEBAPP";
                return createTargetForRender(href, part, module, selectedVariant);
            } else throw new RuntimeException("Part '" + part + "' in '" + res.toURI() + "' is not marked as render part");
        } else throw new RuntimeException("Render part '" + part + "' in '" + res.toURI() + "' not found.");
    }

    public Target createXMLLeafTarget(String key) {
        return createTarget(TargetType.XML_LEAF, key, null);
    }

    public Target createXSLLeafTarget(String key) {
        return createTarget(TargetType.XSL_LEAF, key, null);
    }

    //-- misc

    public void addListener(TargetGeneratorListener listener) {
        generationListeners.add(listener);
    }

    public void removeListener(TargetGeneratorListener listener) {
        generationListeners.remove(listener);
    }

    public void addListener(ConfigurationChangeListener listener) {
        configurationListeners.add(listener);
    }

    public void removeListener(ConfigurationChangeListener listener) {
        configurationListeners.remove(listener);
    }

    @Override
    public String toString() {
        return "[TG: " + alltargets.size() + " targets defined.]";
    }

    // *******************************************************************************************

    public synchronized boolean tryReinit() throws Exception {
        if (needsReload()) {
            LOG.info("\n\n###############################\n" + "#### Reloading depend file: " + this.config_path.toString() + "\n" + "###############################\n");
            synchronized (alltargets) {
                if (alltargets != null && !alltargets.isEmpty()) {
                    targetDependencyRelation.resetAllRelations((Collection<Target>) alltargets.values());
                }
            }
            pagetree = new PageTargetTree();
            alltargets = new HashMap<String, Target>();
            loadConfig(this.config_path);
            this.fireConfigurationChangeEvent();
            return true;
        } else {
            return false;
        }
    }

    private boolean needsReload() {
        for (Resource file : configFileDependencies) {
            if (file.lastModified() > config_mtime) {
                return true;
            }
        }
        return false;
    }
    
    protected long getConfigMaxModTime() {
        long tmptime = -1;
        for (Resource file: configFileDependencies) {
            tmptime = Math.max(file.lastModified(), tmptime);
        }
        return tmptime;
    }

    private void fireConfigurationChangeEvent() {
        for (Iterator<ConfigurationChangeListener> i = this.configurationListeners.iterator(); i.hasNext();) {
            ConfigurationChangeListener listener = i.next();
            listener.configurationChanged(new ConfigurationChangeEvent(this));
        }
    }

    private void loadConfig(Resource configFile) throws XMLException, IOException, SAXException {
        config_mtime = System.currentTimeMillis();
        String path = configFile.toURI().toString();
        LOG.info("\n***** CAUTION! ***** loading config " + path + "...");
        
        Document config;

        // String containing the XML code with resolved includes
        String fullXml = null;

        final Document confDoc = Xml.parseMutable(configFile);
        IncludesResolver iresolver = new IncludesResolver(null, "config-include");
        // Make sure list of dependencies only contains the file itself
        configFileDependencies.clear();
        configFileDependencies.add(configFile);
        FileIncludeEventListener listener = new FileIncludeEventListener() {

            public void fileIncluded(FileIncludeEvent event) {
                configFileDependencies.add(event.getIncludedFile());
            }

        };
        iresolver.registerListener(listener);
        iresolver.resolveIncludes(confDoc);
        
        //automatically add standardpage-alternatives for tenants/languages
        if(tenantInfo != null) {
            for(Tenant tenant: tenantInfo.getTenants()) {
                for(String tenantLang: tenant.getSupportedLanguages()) {
                    Element pageAltElem = confDoc.createElement("standardpage-alternative");
                    pageAltElem.setAttribute("tenant", tenant.getName());
                    pageAltElem.setAttribute("lang", tenantLang);
                    confDoc.getDocumentElement().appendChild(pageAltElem);
                    LOG.debug("Added standardpage-alternative for " + tenant.getName() + "-" + tenantLang);
                }
            }
        }
            
        String configFileModule = null;
        URI configFileURI = configFile.toURI();
        if("module".equals(configFileURI.getScheme())) {
            configFileModule = configFileURI.getAuthority();
        }
        
        final Set<String> definedStandardPages = new HashSet<String>();
        NodeList pageNodes = confDoc.getElementsByTagName("standardpage");
        for(int i = 0; i < pageNodes.getLength(); i++) {
            Element pageElem = (Element)pageNodes.item(i);
            definedStandardPages.add(pageElem.getAttribute("name").trim());
        }
        
        //Add autodetected standardpages
        NodeList templateNodes = confDoc.getElementsByTagName("auto-standardpage");
        for(int i = 0; i < templateNodes.getLength(); i++) {
            final Element templateNode = (Element)templateNodes.item(i);
            String lookupPath = templateNode.getAttribute("lookup-path").trim();
            if(lookupPath.equals("")) lookupPath = "txt/pages";
            String lookupModule = templateNode.getAttribute("lookup-module").trim();
            if(lookupModule.equals("")) lookupModule = null;
            ResourceVisitor pageVisitor = new ResourceVisitor() {
                public void visit(Resource resource) {
                    URI uri = resource.toURI();
                    String path = uri.getPath();
                    int ind = path.lastIndexOf('/');
                    if(ind > -1) {
                        path = path.substring(ind + 1);
                        path = path.substring(0, path.length() - 4);
                        ind = path.indexOf('_');
                        if(ind > -1) path = path.substring(ind + 1);
                    }
                    if(!definedStandardPages.contains(path)) {
                        Element pageElem = DOMUtils.cloneAndRename(templateNode, "standardpage");
                        if("module".equals(uri.getScheme())) {
                            pageElem.setUserData("module", uri.getAuthority(), null);
                        }
                        pageElem.setAttribute("name", path);
                        confDoc.getDocumentElement().appendChild(pageElem);
                        definedStandardPages.add(path);
                    }
                }
            };
            try {
                ResourceFinder.find(new String[] {".xml"}, new String[] {lookupPath}, lookupModule, pageVisitor);
            } catch (Exception x) {
                throw new XMLException("Error while looking up page resources", x);
            }   
        }
        
        for(int i = 0; i < pageNodes.getLength(); i++) {
            Element pageElem = (Element)pageNodes.item(i);
            String module = (String)pageElem.getUserData("module");
            if("webapp".equals("module")) module = null;
            if(module == null) module = configFileModule;
            if(module != null) {
                pageElem.setAttribute("defining-module", module);
                String moduleAttr = pageElem.getAttribute("module");
                if(moduleAttr.length() == 0) pageElem.setAttribute("module", module);
            }
        }
        NodeList includeNodes = confDoc.getElementsByTagName("include");
        for(int i = 0; i < includeNodes.getLength(); i++) {
            Element includeElem = (Element)includeNodes.item(i);
            if(!(includeElem.getAttribute("stylesheet").startsWith("module://")
                    || includeElem.getAttribute("module").length() > 0)) {
                String module = (String)includeElem.getUserData("module");
                if("webapp".equals("module")) module = null;
                if(module == null) module = configFileModule;
                if(module != null) {
                    includeElem.setAttribute("module", module);
                }
            }
        }

        fullXml = Xml.serialize(confDoc, false, true);

        XMLReader xreader = XMLReaderFactory.createXMLReader();
        TransformerFactory tf = TransformerFactory.newInstance();
        if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
            SAXTransformerFactory stf = (SAXTransformerFactory) tf;
            TransformerHandler th;
            try {
                th = stf.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException("Failed to configure TransformerFactory!", e);
            }
            DOMResult dr = new DOMResult();
            DOMResult dr2 = new DOMResult();
            th.setResult(dr);
            DefaultHandler dh = new TransformerHandlerAdapter(th);
            DefaultHandler cushandler = new CustomizationHandler(dh);
            xreader.setContentHandler(cushandler);
            xreader.setDTDHandler(cushandler);
            xreader.setErrorHandler(cushandler);
            xreader.setEntityResolver(cushandler);
            xreader.parse(new InputSource(new StringReader(fullXml)));
            try {
                Transformer trans = SimpleResolver.configure(tf, "/pustefix/xsl/depend.xsl");
                if (EnvironmentProperties.getProperties().getProperty("mode").equals("prod")) {
                    trans.setParameter("prohibitEdit", "yes");
                } else {
                    trans.setParameter("prohibitEdit", "no");
                }
                trans.transform(new DOMSource(dr.getNode()), dr2);
                Node tempNode = dr2.getNode();
                config = tempNode.getOwnerDocument();

                // tempNode might already be the document and
                // getOwnerDocument() will return null in this case
                if (config == null) {
                    if (tempNode.getNodeType() == Node.DOCUMENT_NODE) {
                        config = (Document) tempNode;
                    } else {
                        throw new SAXException("XML result is not a Document though it should be");
                    }
                }
            } catch (TransformerException e) {
                throw new SAXException(e);
            }
        } else {
            throw new RuntimeException("Could not get instance of SAXTransformerFactory!");
        }

        renderParams = new HashMap<String, String>();
        List<Element> renderParamsElems = DOMUtils.getChildElementsByTagName(config.getDocumentElement(), "render-params");
        if(!renderParamsElems.isEmpty()) {
            List<Element> paramElems = DOMUtils.getChildElementsByTagName(renderParamsElems.get(0), "param");
            for(Element paramElem: paramElems) {
                String name = paramElem.getAttribute("name").trim();
                String value = paramElem.getAttribute("value").trim();
                renderParams.put(name, value);
            }
        }
        
        Element root = (Element) config.getElementsByTagName("make").item(0);
        
        String versionStr=root.getAttribute("xsltversion");
        if(versionStr!=null&&!versionStr.equals("")) {
            try {
                xsltVersion=XsltVersion.valueOf("XSLT"+versionStr);
            } catch(IllegalArgumentException x) {
                throw new RuntimeException("XSLT version not supported: "+versionStr);
            }
        }
        
        
        NodeList targetnodes = config.getElementsByTagName("target");

        name = root.getAttribute("project");
        if (name == null || name.length() == 0) {
            // Generate name based on path to depend.xml
            String relativePath;
            if (configFile instanceof DocrootResource) {
                relativePath = ((DocrootResource) configFile).getRelativePath();
            } else {
                throw new XMLException("project attribute is not set and depend.xml is not within docroot");
            }
            name = "GENERATED_NAME" + relativePath.replace("/", "_SLASH_");
        }
        
        
        language = getAttribute(root, "lang");

        String gl_theme_str = null;
        String def_theme_str = null;
        if (root.hasAttribute("themes")) {
            gl_theme_str = getAttribute(root, "themes");
        }
        if (gl_theme_str == null || gl_theme_str.equals("")) {
            gl_theme_str = name + " default";
        } else if (!gl_theme_str.endsWith(" default")) {
            if (gl_theme_str.lastIndexOf(' ') == -1) {
                def_theme_str = gl_theme_str.trim();
            } else {
                def_theme_str = gl_theme_str.substring(gl_theme_str.lastIndexOf(' ')).trim();
            }
            gl_theme_str = gl_theme_str + " default";
        }
        if (def_theme_str == null) {
            def_theme_str = "default";
        }
        
        global_themes = new Themes(gl_theme_str);
        default_theme = def_theme_str;

        if(cacheDir == null) {
            cacheDir = ResourceUtil.getFileResourceFromDocroot(CACHEDIR);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            } else if (!cacheDir.isDirectory() || !cacheDir.canRead()) {
                throw new XMLException("Directory " + cacheDir + " is not readeable or is no directory");
            } else if (!cacheDir.canWrite()) {
                // When running in WAR mode this is okay
                LOG.warn("Directory " + cacheDir + " is not writable!");
                if(servletContext.getRealPath("/") == null) {
                    File tmpDir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
                    File dir = new File(tmpDir, "pustefix-xsl-cache");
                    if(dir.exists()) {
                        FileUtils.delete(dir);
                        dir.mkdir();
                    }
                    cacheDir = ResourceUtil.getFileResource(dir.toURI());
                }
            }
        }

        HashSet<String> depxmls = new HashSet<String>();
        HashSet<String> depxsls = new HashSet<String>();
        HashMap<String, TargetStruct> allstructs = new HashMap<String, TargetStruct>();

        long start = System.currentTimeMillis();
        for (int i = 0; i < targetnodes.getLength(); i++) {
            Element node = (Element) targetnodes.item(i);
            String nameattr = node.getAttribute("name");
            String type = node.getAttribute("type");
            String themes = node.getAttribute("themes");
            String variant = node.getAttribute("variant");
            String pagename = node.getAttribute("page");
            TargetStruct struct = new TargetStruct(nameattr, type, themes, variant, pagename);
            HashMap<String, String> params = new HashMap<String, String>();
            HashSet<Resource> depaux = new HashSet<Resource>();
            Element xmlsub = (Element) node.getElementsByTagName("depxml").item(0);
            Element xslsub = (Element) node.getElementsByTagName("depxsl").item(0);
            NodeList allaux = node.getElementsByTagName("depaux");
            NodeList allpar = node.getElementsByTagName("param");

            if (xmlsub != null) {
                String xmldep = xmlsub.getAttribute("name");
                String module = xmlsub.getAttribute("module");
                if(module.length()>0) xmldep = "module://"+module+"/"+xmldep;
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
                String module = xslsub.getAttribute("module");
                if(module.length()>0) xsldep = "module://"+module+"/"+xsldep;
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
                Element aux = (Element) allaux.item(j);
                Resource auxname = ResourceUtil.getResource(aux.getAttribute("name"));
                depaux.add(auxname);
            }
            struct.setDepaux(depaux);
            for (int j = 0; j < allpar.getLength(); j++) {
                Element par = (Element) allpar.item(j);
                String parname = par.getAttribute("name");
                if ("docroot".equals(parname)) {
                    throw new XMLException("The docroot parameter is no longer allowed! [" + nameattr + "]");
                }
                String value = par.getAttribute("value");
                params.put(parname, value);
            }
            String defModule = node.getAttribute("defining-module");
            if(!defModule.equals("")) {
                params.put("__defining_module", defModule);
            }
            // TODO Check that docroot really is not needed by targets
            // params.put("docroot", confile.getBase().getPath());
            struct.setParams(params);
            allstructs.put(nameattr, struct);
        }
        
        LOG.info("\n=====> Preliminaries took " + (System.currentTimeMillis() - start) + "ms. Now looping over " + allstructs.keySet().size() + " targets");
        start = System.currentTimeMillis();
        for (Iterator<String> i = allstructs.keySet().iterator(); i.hasNext();) {
            TargetStruct struct = allstructs.get(i.next());
            createTargetFromTargetStruct(struct, allstructs, depxmls, depxsls);
        }
        LOG.info("\n=====> Creating targets took " + (System.currentTimeMillis() - start) + "ms. Now init pagetree");
        start = System.currentTimeMillis();
        pagetree.initTargets();
        LOG.info("\n=====> Init of Pagetree took " + (System.currentTimeMillis() - start) + "ms. Ready...");
    
        start = System.currentTimeMillis();
        if(parseIncludes) {
            try {
                ResourceFinder.find(new String[] {".xml"}, new String[] {"txt", "xml"}, null, this);
            } catch (Exception e) {
                throw new XMLException("Parsing of include files failed", e);
            }
        }
        LOG.info("\n=====> Include parsing took " + (System.currentTimeMillis() - start) + "ms. Ready...");
        
    }

    public void visit(Resource resource) {
        IncludePartsInfo info;
        try {
            info = includePartsInfo.getIncludePartsInfo(resource);
        } catch (IncludePartsInfoParsingException e) {
            throw new PustefixRuntimeException("Error while trying to parse include parts " +
                    "in " + "resource '" + resource.toURI() + "'.", e);
        }
        if(info != null) {
            for(IncludePartInfo partInfo: info.getParts().values()) {
                if(partInfo.isRender()) {
                    URI uri = resource.toURI();
                    String href = null;
                    String module = null;
                    if("file".equals(uri.getScheme())) {
                        try {
                            URI docUri = GlobalConfig.getDocrootAsURL().toURI();
                            URI relUri = docUri.relativize(uri);
                            href = relUri.getPath();
                        } catch (URISyntaxException e) {
                            LOG.error("Error getting docroot relative path for render include '" + uri.toString() + "@" + partInfo.getName(), e);
                            return;
                        }
                    } else if("module".equals(uri.getScheme())){
                        href = uri.getPath();
                        module = uri.getAuthority();
                    } else {
                        href = uri.getPath();
                    }
                    if(href.startsWith("/")) href = href.substring(1);
                    String part = partInfo.getName();
                    createTargetForRender(href, part, module, null);
                    for(String variant: partInfo.getRenderVariants()) {
                        createTargetForRender(href, part, module, variant);
                    }
                }
            }
        }
    }
    
    private TargetRW createTargetFromTargetStruct(TargetStruct struct, HashMap<String, TargetStruct> allstructs, HashSet<String> depxmls, HashSet<String> depxsls) throws XMLException {

        String key = struct.getName();
        String type = struct.getType();
        TargetType reqtype = TargetType.getByTag(type);
        TargetRW tmp = getTargetRW(key);

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
                xmlsource = createTarget(TargetType.XML_LEAF, xmldep, null);
            } else {
                xmlsource = createTargetFromTargetStruct(allstructs.get(xmldep), allstructs, depxmls, depxsls);
            }

            // Check if xsldep is a leaf node or virtual:
            if (!allstructs.containsKey(xsldep)) {
                xslsource = createTarget(TargetType.XSL_LEAF, xsldep, null);
            } else {
                xslsource = createTargetFromTargetStruct(allstructs.get(xsldep), allstructs, depxmls, depxsls);
            }

            String themes_str = struct.getThemes();
            Themes themes = null;

            if (themes_str != null && !themes_str.equals("")) {
                if (!themes_str.endsWith(" default")) {
                    themes_str = themes_str + " default";
                }
                themes = new Themes(themes_str);
            } else {
                themes = global_themes;
            }

            VirtualTarget virtual = (VirtualTarget) createTarget(reqtype, key, themes);
            String variantname = struct.getVariant();
            String pagename = struct.getPage();

            virtual.setXMLSource(xmlsource);
            virtual.setXSLSource(xslsource);

            AuxDependencyManager manager = virtual.getAuxDependencyManager();
            HashSet<Resource> auxdeps = struct.getDepaux();
            for (Iterator<Resource> i = auxdeps.iterator(); i.hasNext();) {
                Resource path = i.next();
                manager.addDependencyFile(path);
            }

            HashMap<String, String> params = struct.getParams();
            // we want to remove already defined params (needed when we do a reload)
            virtual.resetParams();
            
            for (Iterator<String> i = params.keySet().iterator(); i.hasNext();) {
                String pname = i.next();
                String value = params.get(pname);
                LOG.debug("* Adding Param " + pname + " with value " + value);
                virtual.addParam(pname, value);
            }
            virtual.addParam(XSLPARAM_TG, this);
            virtual.addParam(XSLPARAM_TKEY, key);
            try {
                virtual.addParam(XSLPARAM_SITEMAP, siteMap.getSiteMapXMLElement(getXsltVersion(), params.get("lang")));
            } catch (Exception e) {
                throw new XMLException("Cannot get navigation tree", e);
            }
            
            if (!depxmls.contains(key) && !depxsls.contains(key)) {
                // it's a toplevel target...
                if (pagename == null) {
                    LOG.info("*** WARNING *** Target '" + key + "' is top-level, but has no 'page' attribute set! Ignoring it... ***");
                } else {
                    //CAT.warn("REGISTER " + pagename + " " + variantname);
                    PageInfo info = pageInfoFactory.getPage(pagename, variantname);
                    pagetree.addEntry(info, virtual);
                }
            } else if (pagename != null) {
                throw new RuntimeException("*** ERROR *** Target '" + key + "' is NOT top-level, but has a 'page' attribute set! ***");
            }
            return virtual;
        }
    }

    // *******************************************************************************************
    
    private Target createTargetForRender(String href, String part, String module, String variantId) {
        
        Themes themes = global_themes;
        if(variantId != null) {
            String[] varThemes = variantId.split(":");
            String[] globThemes = themes.getThemesArr();
            ArrayList<String> allThemes = new ArrayList<String>();
            for(String varTheme: varThemes) allThemes.add(0, varTheme);
            for(String globTheme: globThemes) allThemes.add(globTheme);
            String[] themeArr = new String[allThemes.size()];
            allThemes.toArray(themeArr);
            themes = new Themes(themeArr);
        }
        
        String renderKey = createRenderKey(href, part, module, variantId);
        Target target = alltargets.get(renderKey);
        if(target == null) {
            
            XMLVirtualTarget xmlTarget = (XMLVirtualTarget)createTarget(TargetType.XML_VIRTUAL, renderKey + ".xml", themes);
            Target xmlSource = createTarget(TargetType.XML_LEAF, "module://pustefix-core/xml/render.xml", null);
            Target xslSource = createTarget(TargetType.XSL_VIRTUAL, "metatags.xsl", null);
            xmlTarget.setXMLSource(xmlSource);
            xmlTarget.setXSLSource(xslSource);
            xmlTarget.addParam(XSLPARAM_TG, this);
            xmlTarget.addParam(XSLPARAM_TKEY, renderKey + ".xml");
            xmlTarget.addParam("render_href", href);
            xmlTarget.addParam("render_part", part);
            xmlTarget.addParam("render_module", module);
                    
            XSLVirtualTarget xslTarget = (XSLVirtualTarget)createTarget(TargetType.XSL_VIRTUAL, renderKey + ".xsl", themes);
            xmlSource = xmlTarget;
            xslSource = createTarget(TargetType.XSL_VIRTUAL, "master.xsl", null);
            xslTarget.setXMLSource(xmlSource);
            xslTarget.setXSLSource(xslSource);
            xslTarget.addParam(XSLPARAM_TG, this);
            xslTarget.addParam(XSLPARAM_TKEY, renderKey + ".xsl");
            Iterator<String> it = renderParams.keySet().iterator();
            while(it.hasNext()) {
                String name = it.next();
                xslTarget.addParam(name, renderParams.get(name));
            }
         
            alltargets.put(renderKey, xslTarget);
            target = xslTarget;
        }
        return target;
    }
    
    private TargetRW getTargetRW(String key) {
        synchronized (alltargets) {
            return (TargetRW) alltargets.get(key);
        }
    }

    private TargetRW createTarget(TargetType type, String key, Themes themes) {
        
        TargetRW tmp = targetFactory.getTarget(type, this, key, themes);
        TargetRW tmp2 = getTargetRW(key);

        if (tmp2 == null) {
            synchronized (alltargets) {
                alltargets.put(tmp.getTargetKey(), tmp);
            }
        } else if (tmp != tmp2) {
            throw new RuntimeException("Requesting Target '" + key + "' of type " + tmp.getType() + ", but already have a Target of type " + tmp2.getType() + " with the same key in this Generator!");
        }
        return tmp;
    }

    private class TargetStruct {

        HashSet<Resource> depaux;

        HashMap<String, String> params;

        String type;

        String name;

        String xsldep;

        String xmldep;

        String variant = null;

        String themes = null;

        String page = null;

        public TargetStruct(String name, String type, String themes, String variant, String page) {
            this.name = name;
            this.type = type;
            if (variant != null && !variant.equals("")) {
                this.variant = variant;
            }
            if (themes != null && !themes.equals("")) {
                this.themes = themes;
            }
            if (page != null && !page.equals("")) {
                this.page = page;
            }
        }

        public String getThemes() {
            return themes;
        }

        public String getVariant() {
            return variant;
        }

        public String getPage() {
            return page;
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

        public HashSet<Resource> getDepaux() {
            return depaux;
        }

        public void setDepaux(HashSet<Resource> in) {
            this.depaux = in;
        }

        public HashMap<String, String> getParams() {
            return params;
        }

        public void setParams(HashMap<String, String> in) {
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
            GlobalConfigurator.setDocroot(docroot.getPath());

            StringBuilder report = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                try {
                    FileResource file = ResourceUtil.getFileResourceFromDocroot(args[i]);
                    if (file.exists() && file.canRead() && file.isFile()) {
                        TargetGenerator gen = new TargetGenerator(file);
                        gen.setIsGetModTimeMaybeUpdateSkipped(false);
                        System.out.println("---------- Doing " + args[i] + "...");
                        gen.generateAll();
                        System.out.println("---------- ...done [" + args[i] + "]");
                        report.append(gen.getReportAsString());
                    } else {
                        LOG.error("Couldn't read configfile '" + args[i] + "'");
                        throw (new XMLException("Oops!"));
                    }
                } catch (Exception e) {
                    LOG.error("Oops! TargetGenerator exit!", e);
                    System.exit(-1);
                }
            }

            System.out.println(report.toString());

        } else {
            LOG.error("Need docroot and configfile(s) to work on");
        }
    }

    public void generateAll() throws Exception {
        for (Iterator<String> e = getAllTargets().keySet().iterator(); e.hasNext();) {
            Target current = getTarget(e.next());
            generateTarget(current);
            /* if all listeners want to stop, 
             * there is no point in continuing ... */
            if (needsToStop()) {
                break;
            }
        }
    }

    public void generateTarget(Target target) throws Exception {
        if (target.getType() != TargetType.XML_LEAF && target.getType() != TargetType.XSL_LEAF) {
            String path = getDisccachedir().toURI().toString();
            System.out.println(">>>>> Generating " + path + File.separator + target.getTargetKey() + " from " + target.getXMLSource().getTargetKey() + " and " + target.getXSLSource().getTargetKey());

            boolean needs_update = false;
            needs_update = target.needsUpdate();
            if (needs_update) {
                try {
                    target.getValue();
                    notifyListenerTargetDone(target);
                } catch (TargetGenerationException tgex) {
                    notifyListenerTargetException(target, tgex);
                    report.addError(tgex);
                    tgex.printStackTrace();
                }
            } else {
                notifyListenerTargetDone(target);
            }
            System.out.println("done.");
        } else {
            notifyListenerTargetDone(target);
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
        if (generationListeners.size() > 0) {
            result = true;
            for (Iterator<TargetGeneratorListener> it = generationListeners.iterator(); it.hasNext();) {
                TargetGeneratorListener listener = it.next();
                if (listener.needsStop()) {
                    result = result && true;
                    it.remove();
                } else {
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
        for (Iterator<TargetGeneratorListener> it = generationListeners.iterator(); it.hasNext();) {
            TargetGeneratorListener listener = it.next();
            listener.finishedTarget(target);
        }
    }

    /**
     * This calls the generationException method of all registered listeners
     * @param target the finished target
     * @param tgex the exception!
     */
    private void notifyListenerTargetException(Target target, TargetGenerationException tgex) {
        for (Iterator<TargetGeneratorListener> it = generationListeners.iterator(); it.hasNext();) {
            TargetGeneratorListener listener = it.next();
            listener.generationException(target, tgex);
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
    public String getReportAsString() {
        return report.toString();
    }

    public boolean errorsReported() {
        return report.hasError();
    }
    
    public void resetGenerationReport() {
        report = new TargetGenerationReport();
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
      
    public static boolean isRenderKey(String key) {
        return key.indexOf(RENDER_KEY_SEPARATOR) > -1;
    }
    
    private static String createRenderKey(String href, String part, String module, String variant) {
        if(href == null || href.equals("")) throw new IllegalArgumentException("Argument 'href' must not be empty");
        if(part == null || part.equals("")) throw new IllegalArgumentException("Argument 'part' must not be empty");
        if(module == null) module = "";
        if(variant == null) variant = "";
        String targetKey = encode(href) + RENDER_KEY_SEPARATOR + encode(part) + 
                            RENDER_KEY_SEPARATOR + encode(module) + RENDER_KEY_SEPARATOR + encode(variant);
        return targetKey;
    }
        
    public static String encode(String str) {
        str = str.replace("%", "%" + Integer.toHexString('%'));
        str = str.replace("" + RENDER_KEY_SEPARATOR, "%" + Integer.toHexString(RENDER_KEY_SEPARATOR));
        str = str.replace("+", "%" + Integer.toHexString('+'));
        str = str.replace("/", "+");
        return str;
    }
        
    public static String decode(String str) {
        str = str.replace("+", "/");
        str = str.replace("%" + Integer.toHexString('+'), "+");
        str = str.replace("%" + Integer.toHexString(RENDER_KEY_SEPARATOR), "" + RENDER_KEY_SEPARATOR);
        str = str.replace("%" + Integer.toHexString('%'), "%");
        return str;
    }

}
