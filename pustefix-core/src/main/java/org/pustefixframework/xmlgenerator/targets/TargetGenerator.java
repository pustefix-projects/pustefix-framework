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

package org.pustefixframework.xmlgenerator.targets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.customization.RuntimeProperties;
import org.pustefixframework.resource.FileResource;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.LastModifiedInfoResource;
import org.pustefixframework.resource.Resource;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.xmlgenerator.cachestat.CacheStatistic;
import org.pustefixframework.xmlgenerator.config.model.Configuration;
import org.pustefixframework.xmlgenerator.config.model.IncludeDef;
import org.pustefixframework.xmlgenerator.config.model.ModelChangeEvent;
import org.pustefixframework.xmlgenerator.config.model.ModelChangeListener;
import org.pustefixframework.xmlgenerator.config.model.ModelElement;
import org.pustefixframework.xmlgenerator.config.model.NamespaceDeclaration;
import org.pustefixframework.xmlgenerator.config.model.Page;
import org.pustefixframework.xmlgenerator.config.model.Parameter;
import org.pustefixframework.xmlgenerator.config.model.ParameterConfig;
import org.pustefixframework.xmlgenerator.config.model.StandardMaster;
import org.pustefixframework.xmlgenerator.config.model.StandardMetatags;
import org.pustefixframework.xmlgenerator.config.model.StandardPage;
import org.pustefixframework.xmlgenerator.config.model.TargetDef;
import org.pustefixframework.xmlgenerator.config.model.ThemeConfig;
import org.pustefixframework.xmlgenerator.config.model.VariantConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.parser.OSGiAwareParser;
import com.marsching.flexiparse.parser.Parser;

import de.schlund.pfixcore.exception.PustefixRuntimeException;
import de.schlund.pfixcore.util.Meminfo;
import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.event.ConfigurationChangeEvent;
import de.schlund.pfixxml.event.ConfigurationChangeListener;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltVersion;

/**
 * The TargetGenerator holds all the targets belonging to a certain
 * project (as defined by the config file used to init the Generator).
 *
 */

public class TargetGenerator implements ModelChangeListener, InitializingBean, BundleContextAware {

    public static final String XSLPARAM_TG = "__target_gen";
    public static final String XSLPARAM_TKEY = "__target_key";
    public static final String XSLPARAM_NAVITREE = "__navitree";
    public static final String XSLPARAM_NAMESPACES = "__namespaces";

    public static final String CACHEDIR = ".cache";

    protected final Log logger = LogFactory.getLog(TargetGenerator.class);
    
    private static TargetGenerationReport report = new TargetGenerationReport();

    private PageTargetTree pagetree = new PageTargetTree();
    
    private Map<String, Target> alltargets = new HashMap<String, Target>();

    private boolean isGetModTimeMaybeUpdateSkipped = false;

    private long config_mtime = 0;

    private Themes globalThemes;
    
    private String defaultTheme = "default";

    /* All registered TargetGenerationListener */
    private Set<TargetGeneratorListener> generationListeners = new HashSet<TargetGeneratorListener>();

    private Set<ConfigurationChangeListener> configurationListeners = Collections.synchronizedSet(new HashSet<ConfigurationChangeListener>());

    private Resource configFile;
    
    private FileResource cacheDir;
    
    private ResourceLoader resourceLoader;
    
    private BundleContext bundleContext;
    
    private Configuration configuration;
    
    private Element namespaces;
    private Element navigation;
    
    private Map<ModelElement,List<Target>> modelToTargets = new HashMap<ModelElement,List<Target>>();
    
    private SPCache<Object, Object> targetCache;
    private SPCache<String, IncludeDocument> includeCache;
    private TreeMap<String, SharedLeaf> sharedLeaves;
    
    private CacheStatistic cacheStatistic;
    
    private AuxDependencyFactory auxDependencyFactory;
    private TargetDependencyRelation targetDependencyRelation;
    
    //--

    public void afterPropertiesSet() throws Exception {
    	Meminfo.print("TG: Before loading " + configFile.toString());
        loadConfig(configFile);
        Meminfo.print("TG: after loading targets for " + configFile.toString());
    }
    
    public void setBundleContext(BundleContext bundleContext) {
    	this.bundleContext = bundleContext;
    }
    
    
    //-- attributes

    public String getName() {
        return configuration.getProject();
    }
    
    public void setConfigFile(InputStreamResource configFile) {
    	this.configFile = configFile;
    }
    
    public void setResourceLoader(ResourceLoader resourceLoader) {
    	this.resourceLoader = resourceLoader;
    }
    
    public XsltVersion getXsltVersion() {
    	return configuration.getXsltVersion();
    }

    public Themes getGlobalThemes() {
    	if(globalThemes == null) {
    		String[] themes = configuration.getThemes();
    		if(themes == null || themes.length == 0) {
    			themes = new String[] {getName(), "default"};
    		} else if(!themes[themes.length-1].equals("default")) {
    			defaultTheme = themes[themes.length-1];
    			themes = Arrays.copyOf(themes, themes.length+1);
    			themes[themes.length-1] = "default";
    		}
    		globalThemes = new Themes(themes);
    	}
        return globalThemes;
    }
    
    public String getDefaultTheme() {
        return defaultTheme;
    }

    public String getLanguage() {
        return configuration.getLanguage();
    }

    public FileResource getDisccachedir() {
        return cacheDir;
    }
    
    public SPCache<Object,Object> getTargetCache() {
    	return targetCache;
    }
    
    public void setTargetCache(SPCache<Object,Object> targetCache) {
    	this.targetCache = targetCache;
    }
    
    public SPCache<String,IncludeDocument> getIncludeCache() {
    	return includeCache;
    }
    
    public void setIncludeCache(SPCache<String,IncludeDocument> includeCache) {
    	this.includeCache = includeCache;
    }
    
    public void setCacheStatistic(CacheStatistic cacheStatistic) {
    	this.cacheStatistic = cacheStatistic;
    }
    
    public CacheStatistic getCacheStatistic() {
    	return cacheStatistic;
    }
    
    public AuxDependencyFactory getAuxDependencyFactory() {
    	return auxDependencyFactory;
    }
    
    public PageTargetTree getPageTargetTree() {
        return pagetree;
    }
    
    public ResourceLoader getResourceLoader() {
    	return resourceLoader;
    }
    
    

    //-- targets

    public TreeMap<String, Target> getAllTargets() {
        synchronized (alltargets) {
            return new TreeMap<String, Target>(alltargets);
        }
    }

    public Target getTarget(String key) {
        synchronized (alltargets) {
            return (Target) alltargets.get(key);
        }
    }

    public Target createXMLLeafTarget(String key) {
        return createLeafTarget(TargetType.XML_LEAF, key, null);
    }

    public Target createXSLLeafTarget(String key) {
        return createLeafTarget(TargetType.XSL_LEAF, key, null);
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
        return "[TG: " + getName() + "; " + alltargets.size() + " targets defined.]";
    }

    // *******************************************************************************************

    public synchronized boolean tryReinit() throws Exception {
        if (needsReload()) {
            logger.warn("\n\n###############################\n" + "#### Reloading depend file: " + this.configFile.toString() + "\n" + "###############################\n");
            synchronized (alltargets) {
                if (alltargets != null && !alltargets.isEmpty()) {
                    targetDependencyRelation.resetAllRelations((Collection<Target>) alltargets.values());
                }
            }
            pagetree = new PageTargetTree();
            alltargets = new HashMap<String, Target>();
            loadConfig(this.configFile);
            this.fireConfigurationChangeEvent();
            return true;
        } else {
            return false;
        }
    }

    private boolean needsReload() {
    	//TODO: check if underlying resources changed (in devel mode, when bundle is exploded or life-jar
    	return false;
    }
    
    public long getConfigMaxModTime() {
    	return config_mtime;
    }
    
    private void fireConfigurationChangeEvent() {
        for (Iterator<ConfigurationChangeListener> i = this.configurationListeners.iterator(); i.hasNext();) {
            ConfigurationChangeListener listener = i.next();
            listener.configurationChanged(new ConfigurationChangeEvent(this));
        }
    }

    private void loadConfig(Resource configFile) throws XMLException, IOException, SAXException {
        config_mtime = System.currentTimeMillis();
        logger.warn("\n***** CAUTION! ***** loading config " + configFile.toString() + "...");
        
        targetDependencyRelation = new TargetDependencyRelation();
        auxDependencyFactory = new AuxDependencyFactory(targetDependencyRelation);
        sharedLeaves = new TreeMap<String, SharedLeaf>();
        
        
        try {
        	Parser configParser = new OSGiAwareParser(bundleContext, "META-INF/org/pustefixframework/xmlgenerator/config/parser/xml-generator-config.xml");
        	Properties buildTimeProperties = RuntimeProperties.getProperties();
        	CustomizationInfo cusInfo = new PropertiesBasedCustomizationInfo(buildTimeProperties);
        	InputStream in = ((InputStreamResource)configFile).getInputStream();
        	configuration = new Configuration();
        	configuration.addModelChangeListener(this);
        	
        	configParser.parse(in, cusInfo, configuration, bundleContext, resourceLoader);
        	
        	//TODO: configurable cachedir location
            cacheDir = getFileResourceFromPersistentStorage("");
            if (!cacheDir.getFile().exists()) {
                cacheDir.getFile().mkdirs();
            } else if (!cacheDir.getFile().isDirectory() || !cacheDir.getFile().canRead()) {
                throw new XMLException("Directory " + cacheDir + " is not readeable or is no directory");
            } else if (!cacheDir.getFile().canWrite()) {
                // When running in WAR mode this is okay
                logger.warn("Directory " + cacheDir + " is not writable!");
            }
        	
        	//test(configuration);
        	
        	
        	
        } catch(Exception x) {
        	throw new XMLException("Can't read configuration", x);
        }
    	
    }
    
    public void modelChanged(ModelChangeEvent event) {
    	ModelElement modelElement = event.getSource();
    	if(modelElement == configuration.getNamespaceDeclarations()) {
    		namespaces = null;
    		if(logger.isDebugEnabled()) logger.debug("Invalidate namespaces");
    	} else if(modelElement == configuration.getPages()) {
    		navigation = null;
    		if(logger.isDebugEnabled()) logger.debug("Invalidate navigation");
    	} else if(modelElement == configuration.getStandardPages() || modelElement == configuration.getStandardMasters() ||
    			modelElement == configuration.getStandardMetatags() || modelElement == configuration.getTargetDefs()) {
    		Iterator<ModelElement> it = event.getAffectedElements().iterator();
    		while(it.hasNext()) {
    			ModelElement elem = it.next();
    			if(event.getType() == ModelChangeEvent.Type.REMOVE) {
    				removeModelElement(elem);
        		} else if(event.getType() == ModelChangeEvent.Type.ADD) {
        			addModelElement(elem);
        		} else if(event.getType() == ModelChangeEvent.Type.UPDATE) {
        			removeModelElement(elem);
        			addModelElement(elem);
        		}
    		}
    	}
    }
    
    private void removeModelElement(ModelElement modelElement) {
    	List<Target> depTargets = modelToTargets.get(modelElement);
		for(Target depTarget:depTargets) {
			//TODO: invalidate targets
			alltargets.remove(depTarget.getTargetKey());
		}
    	if(modelElement instanceof StandardPage) {
    		StandardPage page = (StandardPage)modelElement;
			pagetree.removeEntry(page.getName(), page.getVariant());
    	}
    }
  
    private void addModelElement(ModelElement modelElement) {
    	if(modelElement instanceof StandardPage) {
	    	try {
				createTarget(configuration, modelElement);
			} catch(Exception x) {
				throw new PustefixRuntimeException("Can't create targets for model element", x);
			}
    	}
    }
    
    
    private Target createTarget(Configuration configuration, TargetType type, String key) throws Exception {
    	Target target = alltargets.get(key);
    	if(target == null) {
    		for(TargetDef targetDef: configuration.getTargetDefs()) {
    			if(key.equals(targetDef.getName())) return createTarget(configuration,targetDef);
    		}
    		for(StandardMetatags metatags: configuration.getStandardMetatags()) {
    			String xslKey = "metatags.xsl";
    	    	if(metatags.getName() != null) {
    	    		xslKey = "metatags-" + metatags.getName() + ".xsl";
    	    	}
    			if(key.equals(xslKey)) return createTarget(configuration, metatags);
    		}
    		for(StandardMaster master: configuration.getStandardMasters()) {
    			String xslKey = "master.xsl";
    			if(master.getName() != null) {
    	    		xslKey = "master-" + master.getName() + ".xsl";
    	    	}
    			if(key.equals(xslKey)) return createTarget(configuration, master);
    		}
    		for(StandardPage page: configuration.getStandardPages()) {
    			String targetNamePrefix = page.getName();
    	    	if(page.getVariant() != null) {
    	    		targetNamePrefix += "::" + page.getVariant();
    	    	}
    	    	String xslKey = targetNamePrefix + ".xsl";
    	    	if(key.equals(xslKey)) return createTarget(configuration, page);
    		}
    		
    		List<String> themeList = new ArrayList<String>();
        	if(configuration.getThemes() != null) {
        		for(String theme:configuration.getThemes()) {
        			themeList.add(theme);
        		}
        	} else {
        		themeList.add(configuration.getProject());
        		themeList.add("default");
        	}
        	if(!themeList.get(themeList.size()-1).equals("default")) {
    			themeList.add("default");
    		}
        	String[] themeArray = new String[themeList.size()];
        	Themes themes = new Themes(themeList.toArray(themeArray));
    		
    		target = createLeafTarget(type, key, themes);
    		alltargets.put(key,target);
    		return target;
    	
    	} else {
    		return target;
    	}
    }
    
    private TargetRW createLeafTarget(TargetType type, String key, Themes themes) {
        Resource targetRes;
        FileResource targetAuxRes;
		try {
			URI uri;
			if (key.contains(":")) {
			    uri = new URI(key);
			} else {
				//TODO: replace workaround
				if(key.startsWith("core")) {
					int ind = key.indexOf('/');
					String str = key.substring(ind+1);
					uri = new URI("pustefixcore:/"+str);
				} else {
			    // TODO Relativ URIs should be resolved relative to the 
			    // configuration file
			    uri = new URI("bundle:/PUSTEFIX-INF/" + key);
				}
			}
			targetRes = resourceLoader.getResource(uri);
			targetAuxRes = getFileResourceFromPersistentStorage(key + ".aux");
		} catch (URISyntaxException e) {
			throw new RuntimeException("Illegal URI: " + key, e);
		}
		if(targetRes == null) throw new RuntimeException("Can't get resource: "+key);
		TargetRW tmp;
		try {
		if(type == TargetType.XML_LEAF) {
			tmp = new XMLLeafTarget(TargetType.XML_LEAF, this, targetRes, targetAuxRes, key, themes);
		} else {
			tmp = new XSLLeafTarget(TargetType.XSL_LEAF, this, targetRes, targetAuxRes, key, themes);
		}
		} catch(Exception x) {
			throw new RuntimeException("Can't create leaf target: " + key , x);
		}
        return tmp;
    }
    
    private TargetRW createTarget(Configuration configuration, TargetDef targetDef) throws Exception {
    	
    	Themes themes = createThemeList(targetDef, targetDef);
    	
    	String key = targetDef.getName();
    	
    	FileResource targetRes = getFileResourceFromPersistentStorage(key);
    	FileResource targetAuxRes = getFileResourceFromPersistentStorage(key + ".aux");
    	
    	VirtualTarget target = null;
    	if(targetDef.getType() == TargetDef.Type.XML) {
    		target = new XMLVirtualTarget(TargetType.XML_VIRTUAL, this, targetRes, targetAuxRes, key, themes);
    	} else {
    		target = new XSLVirtualTarget(TargetType.XSL_VIRTUAL, this, targetRes, targetAuxRes, key, themes);
    	}
    	
    	addStandardParameters(target, targetDef);
    	if(doProhibitEdit()) target.addParam("prohibitEdit", "yes");
    	if(targetDef.getPage() != null) target.addParam("page", targetDef.getPage());
    	target.addParam("product", configuration.getProject());
    	target.addParam("lang", configuration.getLanguage());
    	
    	alltargets.put(key, target);
    	
    	return target;
    }
    
    private void createTarget(Configuration configuration, ModelElement modelElement) throws Exception {
    	if(modelElement instanceof StandardPage) {
    		createTarget(configuration, (StandardPage)modelElement);
    	} else if(modelElement instanceof StandardMetatags) {
    		createTarget(configuration, (StandardMetatags)modelElement);
    	} else if(modelElement instanceof StandardMaster) {
    		createTarget(configuration, (StandardMaster)modelElement);
    	} else if(modelElement instanceof TargetDef) {
    		createTarget(configuration, (TargetDef)modelElement);
    	} else {
    		throw new IllegalArgumentException("Model element type not supported: " + modelElement.getClass().getName());
    	}
    }
    
    private TargetRW createTarget(Configuration configuration, StandardMaster standardMaster) throws Exception {
    	
    	String xslKey = "master.xsl";
    	if(standardMaster.getName() != null) {
    		xslKey = "master-" + standardMaster.getName() + ".xsl";
    	}
    	FileResource targetRes = getFileResourceFromPersistentStorage(xslKey);
    	FileResource targetAuxRes = getFileResourceFromPersistentStorage(xslKey + ".aux");
    	
    	Target xmlSource = createTarget(configuration, TargetType.XML_LEAF, "pustefixcore:/xsl/master.xsl");
    	Target xslSource = createTarget(configuration, TargetType.XSL_LEAF, "pustefixcore:/xsl/customizemaster.xsl");
    	
    	XSLVirtualTarget xslTarget = new XSLVirtualTarget(TargetType.XSL_VIRTUAL, this, targetRes, targetAuxRes, xslKey, getGlobalThemes());
    	
    	xslTarget.setXMLSource(xmlSource);
    	xslTarget.setXSLSource(xslSource);
    	
    	addStandardParameters(xslTarget, standardMaster);
    	xslTarget.addParam("product", configuration.getProject());
    	xslTarget.addParam("lang", configuration.getLanguage());
    	String includes = "";
    	for(IncludeDef include: standardMaster.getIncludes()) {
    		includes += include.getURI().toString() + " ";
    	}
    	xslTarget.addParam("stylesheets_to_include", includes);
    	
    	alltargets.put(xslKey, xslTarget);
    	
    	return xslTarget;
    	
    }
    
    private TargetRW createTarget(Configuration configuration, StandardMetatags standardMetatags) throws Exception {
    	
    	String xslKey = "metatags.xsl";
    	if(standardMetatags.getName() != null) {
    		xslKey = "metatags-" + standardMetatags.getName() + ".xsl";
    	}
    	FileResource targetRes = getFileResourceFromPersistentStorage(xslKey);
    	FileResource targetAuxRes = getFileResourceFromPersistentStorage(xslKey + ".aux");
    	
    	Target xmlSource = createTarget(configuration, TargetType.XML_LEAF, "pustefixcore:/xsl/metatags.xsl");
    	Target xslSource = createTarget(configuration, TargetType.XSL_LEAF, "pustefixcore:/xsl/customizemaster.xsl");
    	
    	XSLVirtualTarget xslTarget = new XSLVirtualTarget(TargetType.XSL_VIRTUAL, this, targetRes, targetAuxRes, xslKey, getGlobalThemes());
    	
    	xslTarget.setXMLSource(xmlSource);
    	xslTarget.setXSLSource(xslSource);
    	
        addStandardParameters(xslTarget, standardMetatags);
        xslTarget.addParam("product", configuration.getProject());
    	xslTarget.addParam("lang", configuration.getLanguage());
    	String includes = "";
    	for(IncludeDef include: standardMetatags.getIncludes()) {
    		includes += include.getURI().toString() + " ";
    	}
    	xslTarget.addParam("stylesheets_to_include", includes);
        
        AuxDependencyManager manager = xslTarget.getAuxDependencyManager();
        manager.addDependencyFile(configFile);
        
    	alltargets.put(xslKey, xslTarget);
    	
    	return xslTarget;
    }
    
    private TargetRW createTarget(Configuration configuration, StandardPage standardPage) throws Exception {
    	
    	List<Target> depTargets = new ArrayList<Target>();
    	
    	String targetNamePrefix = standardPage.getName();
    	if(standardPage.getVariant() != null) {
    		targetNamePrefix += "::" + standardPage.getVariant();
    	}
    	
    	Themes themes = createThemeList(standardPage, standardPage);
    	
    	//Create XML virtual target
    	
    	String xmlKey = targetNamePrefix + ".xml";
    	FileResource targetRes = getFileResourceFromPersistentStorage(xmlKey);
    	FileResource targetAuxRes = getFileResourceFromPersistentStorage(xmlKey + ".aux");
    	
    	Target xmlSource = createTarget(configuration, TargetType.XML_LEAF, standardPage.getXML());
    	
    	String metatags = "metatags.xsl";
    	if(standardPage.getMetatags() != null) {
    		metatags = "metatags-" + standardPage.getMetatags() + ".xsl";
    	}
    	Target xslSource = createTarget(configuration, TargetType.XSL_LEAF, metatags);
    	
    	XMLVirtualTarget xmlTarget = new XMLVirtualTarget(TargetType.XML_VIRTUAL, this, targetRes, targetAuxRes, xmlKey, themes);
    	
    	xmlTarget.setXMLSource(xmlSource);
    	xmlTarget.setXSLSource(xslSource);
    	
        addStandardParameters(xmlTarget, standardPage);
        xmlTarget.addParam("page", standardPage.getName());
        if(doProhibitEdit()) xmlTarget.addParam("prohibitEdit", "yes");
        if(!xmlTarget.getParams().containsKey("outputencoding")) xmlTarget.addParam("outputencoding", getDefaultEncoding());
        xmlTarget.addParam("module", standardPage.getSourceInfo().getBundleSymbolicName());
        
    	alltargets.put(xmlKey, xmlTarget);
    	depTargets.add(xmlTarget);
    	
    	//Create XSL target
    	
    	String xslKey = targetNamePrefix + ".xsl";
    	targetRes = getFileResourceFromPersistentStorage(xslKey);
    	targetAuxRes = getFileResourceFromPersistentStorage(xslKey + ".aux");
    	
    	xmlSource = xmlTarget;
    	
    	String master = "master.xsl";
    	if(standardPage.getMaster() != null) {
    		master = "master-" + standardPage.getMaster() + ".xsl";
    	}
    	xslSource = createTarget(configuration, TargetType.XSL_LEAF, master);
    	
    	XSLVirtualTarget xslTarget = new XSLVirtualTarget(TargetType.XSL_VIRTUAL, this, targetRes, targetAuxRes, xslKey, themes);
    	
    	xslTarget.setXMLSource(xmlSource);
    	xslTarget.setXSLSource(xslSource);
    	
        addStandardParameters(xslTarget, standardPage);
        xslTarget.addParam("page", standardPage.getName());
        if(doProhibitEdit()) xslTarget.addParam("prohibitEdit", "yes");
        if(!xslTarget.getParams().containsKey("outputencoding")) xslTarget.addParam("outputencoding", getDefaultEncoding());
        xslTarget.addParam("module", standardPage.getSourceInfo().getBundleSymbolicName());
        
    	alltargets.put(xslKey, xslTarget);
    	depTargets.add(xslTarget);
    	modelToTargets.put(standardPage, depTargets);
    	
    	pagetree.addEntry(standardPage.getName(), standardPage.getVariant(), xslTarget);
    	
    	return xslTarget;
    }
    
    private Themes createThemeList(ThemeConfig themeConfig, VariantConfig variantConfig) {
    	Themes themes = null;
    	if(variantConfig.getVariant() == null) {
    		if(themeConfig.getThemes() == null) {
    			themes = getGlobalThemes();
    		} else {
    			String[] themeArray = themeConfig.getThemes();
    			if(themeArray.length>0 && themeArray[themeArray.length-1].equals("default")) {
    				themes = new Themes(themeArray);
    			} else {
    				themeArray = Arrays.copyOf(themeArray, themeArray.length+1);
    				themeArray[themeArray.length-1] = "default";
    				themes = new Themes(themeArray);
    			}
    		}
    	} else {
    		List<String> themeList = new ArrayList<String>();
    		if(variantConfig.getVariant() != null) {
    			String[] variantThemes = variantConfig.getVariant().split(":");
    			for(int i=variantThemes.length-1; i>0; i--) {
    				themeList.add(variantThemes[i]);
    			}
    		}
    		if(themeConfig.getThemes() != null) {
    			for(String theme:themeConfig.getThemes()) {
    				themeList.add(theme);
    			}
    			if(!themeList.get(themeList.size()-1).equals("default")) themeList.add("default");
    		} else {
    			for(String theme:getGlobalThemes().getThemesArr()) {
    				themeList.add(theme);
    			}
    		}
    		String[] themeArray = new String[themeList.size()];
        	themes = new Themes(themeList.toArray(themeArray));
    	}
    	return themes;
    }
    
    private void addStandardParameters(VirtualTarget target, ParameterConfig paramConfig) {
    	Map<String,String> commonParams = getParameterMap(configuration.getParameters());
    	Map<String,String> targetParams = getParameterMap(paramConfig.getParameters());
    	for(String paramName:commonParams.keySet()) {
    		if(!targetParams.containsKey(paramName)) {
        		target.addParam(paramName, commonParams.get(paramName));
    		}
    	}
    	for(String paramName:targetParams.keySet()) {
    		target.addParam(paramName, targetParams.get(paramName));
    	}
    	target.addParam(XSLPARAM_TG, this);
        target.addParam(XSLPARAM_TKEY, target.getTargetKey());
        target.addParam(XSLPARAM_NAVITREE, getNavigation());
        target.addParam(XSLPARAM_NAMESPACES, getNamespaces());
    }
    
    
    private boolean doProhibitEdit() {
    	  if (RuntimeProperties.getProperties().getProperty("mode").equals("prod")) return true;
    	  return false;
    }
    
    private String getDefaultEncoding() {
    	return "UTF-8";
    }

    /**
     * Make sure this target generator object is properly configured before calling this method.
     * To obtain a propely configured TargetGenerator Object follow these steps:
     * <ul>
     * <li/><code>String log4jconfig    = System.getProperty("log4jconfig"); DOMConfigurator.configure(log4jconfig);</code>
     * <li/>{@link TargetGenerator} gen = {@link TargetGeneratorFactory}.{@link TargetGeneratorFactory#getInstance()}.{@link TargetGeneratorFactory#createGenerator(String)};
     * <li/>gen.{@link TargetGenerator#setIsGetModTimeMaybeUpdateSkipped(boolean)};
     * </ul>
     * @throws Exception
     */
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
            String path = getDisccachedir().toString();
            System.out.println(">>>>> Generating " + path + File.separator + target.getTargetKey() + " from " + target.getXMLSource().getTargetKey() + " and " + target.getXSLSource().getTargetKey());

            boolean needs_update = false;
            needs_update = target.needsUpdate();
            if (needs_update) {
                try {
                    target.getValue();
                    notifyListenerTargetDone(target);
                } catch (TargetGenerationException tgex) {
                    notifyListenerTargetException(target, tgex);
                    report.addError(tgex, getName());
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
    public static String getReportAsString() {
        return report.toString();
    }

    public static boolean errorsReported() {
        return report.hasError();
    }
    
    public static void resetGenerationReport() {
        report = new TargetGenerationReport();
    }

    private FileResource getFileResourceFromPersistentStorage(String path) {
        if (!path.startsWith("/") && path.length() > 0) {
            path = "/" + path;
        }
        String uri = "persistentstorage:/" + CACHEDIR + "/" + getName() + path;
        Resource resource;
        try {
            resource = resourceLoader.getResource(new URI(uri));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not create URI from string: " + uri, e);
        }
        if (!(resource instanceof FileResource)) {
            throw new RuntimeException("Expected resource implementing " + FileResource.class.getName() + " but got instance of " + resource.getClass().getName());
        }
        return (FileResource) resource;
    }
    
    
	public synchronized Element getNamespaces() {
		if(namespaces == null) {
			namespaces = createNamespacesElement();
		}
		return namespaces;
	}
	
    private Element createNamespacesElement() {
    	Document doc;
    	try {
    		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    	} catch(javax.xml.parsers.ParserConfigurationException x) {
    		throw new RuntimeException("Can't create namespace document", x);
    	}
    	Element root = doc.createElement("namespaces");
    	doc.appendChild(root);
    	for(NamespaceDeclaration decl: configuration.getNamespaceDeclarations()) {
    		Element elem = doc.createElement("namespace-declaration");
    		elem.setAttribute("prefix", decl.getPrefix());
    		elem.setAttribute("url" , decl.getURL());
    		root.appendChild(elem);
    	}
    	doc = Xml.parse(getXsltVersion(), doc);
    	return doc.getDocumentElement();
    }
    
	public synchronized Element getNavigation() {
		if(navigation == null) {
			navigation = createNavigationElement();
		}
		return navigation;
	}
	
    private Element createNavigationElement() {
    	Document doc;
    	try {
    		doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    	} catch(javax.xml.parsers.ParserConfigurationException x) {
    		throw new RuntimeException("Can't create namespace document", x);
    	}
    	Element root = doc.createElement("navigation");
    	doc.appendChild(root);
    	for(Page page:configuration.getPages()) {
    		getNavigationElement(page, root);
    	}
    	doc = Xml.parse(getXsltVersion(), doc);
    	return doc.getDocumentElement();
    }
    
    private void getNavigationElement(Page page, Element parent) {
    	Element element = parent.getOwnerDocument().createElement("page");
    	element.setAttribute("name", page.getName());
    	element.setAttribute("handler", page.getHandler());
    	if(page.getAccessKey() != null) element.setAttribute("accesskey", page.getAccessKey());
    	parent.appendChild(element);
    	for(Page child: page.getChildPages()) {
    		getNavigationElement(child, element);
    	}
    }
    
    
	private Map<String,String> getParameterMap(List<Parameter> paramList) {
		Map<String,String> map = new HashMap<String,String>();
		for(Parameter param: paramList) {
			map.put(param.getName(), param.getValue());
		}
		return map;
	}
	
	public IncludeDocument getIncludeDocument(XsltVersion xsltVersion, Resource path, boolean mutable) throws SAXException, IOException, TransformerException {
		// TODO: change method signature (create multiple methods) to reflect
		// mutable vs. immutable document creation
		if (xsltVersion == null && !mutable) throw new IllegalArgumentException("XsltVersion has to be specified to create a immutable document.");
		IncludeDocument includeDocument = null;
		String key = mutable ? path.getURI().toString() + "_mutable" : path.getURI().toString() + "_imutable" + "_" + xsltVersion;
		synchronized (includeCache) {
			includeDocument = includeCache.getValue(key);
		}
		if (includeDocument == null || ((LastModifiedInfoResource) path).lastModified() > includeDocument.getModTime()) {
			includeDocument = new IncludeDocument();
			includeDocument.createDocument(xsltVersion, path, mutable);
			synchronized (includeCache) {
				includeCache.setValue(key, includeDocument);
			}
		}
		return includeDocument;
	}
	
	public SharedLeaf getSharedLeaf(XsltVersion xsltVersion, Resource path) {
		synchronized(sharedLeaves) {
			SharedLeaf ret = (SharedLeaf) sharedLeaves.get(xsltVersion+":"+path);
			if (ret == null) {
				ret =  new SharedLeaf(path);
				sharedLeaves.put(xsltVersion+":"+path, ret);
			}
			return ret;
		}
    }
    
}
