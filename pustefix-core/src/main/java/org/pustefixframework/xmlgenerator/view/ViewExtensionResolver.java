package org.pustefixframework.xmlgenerator.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.osgi.framework.BundleContext;
import org.pustefixframework.xmlgenerator.targets.Target;
import org.pustefixframework.xmlgenerator.targets.TargetGenerator;
import org.pustefixframework.xmlgenerator.targets.VirtualTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.schlund.pfixxml.IncludeDocument;
import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.util.XPath;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.XsltContext;

/**
 * This class handles the XML content retrieval of extension points by getting and
 * assembling the XML fragments of all registered view extensions
 * 
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensionResolver {

	private TargetGenerator targetGenerator;
	private BundleContext bundleContext;
	private Map<String, ViewExtensionPointReference> extensionPointReferences;
	private Map<String, List<Target>> dependentTargets;
	
	public ViewExtensionResolver(TargetGenerator targetGenerator, BundleContext bundleContext) {
		this.targetGenerator = targetGenerator;
		this.bundleContext = bundleContext;
		extensionPointReferences = new HashMap<String, ViewExtensionPointReference>();
		dependentTargets = new HashMap<String, List<Target>>();
	}
	
	public Node getExtensionNodes(XsltContext context, String targetKey,
			String extensionPointId, String extensionPointVersion) throws Exception {
		
		if(extensionPointId != null) extensionPointId = extensionPointId.trim();
		if(extensionPointId == null || extensionPointId.equals("")) throw new IllegalArgumentException("Missing extension point ID");
		
		if(extensionPointVersion != null) extensionPointVersion = extensionPointVersion.trim();
		if(extensionPointVersion == null || extensionPointVersion.equals("")) extensionPointVersion = "0.0.0";
		
		String refKey = extensionPointId + "@" + extensionPointVersion;
		ViewExtensionPointReference ref;
		synchronized(extensionPointReferences) {
			ref = extensionPointReferences.get(refKey);
			if(ref == null) {
				ref = new ViewExtensionPointReference(bundleContext, this, extensionPointId, extensionPointVersion);
				ref.open();
				extensionPointReferences.put(refKey, ref);
			}
		}
		
		VirtualTarget target = (VirtualTarget)targetGenerator.getTarget(targetKey);
		synchronized(dependentTargets) {
			List<Target> depList = dependentTargets.get(refKey);
			if(depList == null) {
				depList = new ArrayList<Target>();
				dependentTargets.put(refKey, depList);
			}
			if(!depList.contains(target)) depList.add(target);
		}
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		Document extDoc  = docBuilderFactory.newDocumentBuilder().newDocument();
    	Element extElem = extDoc.createElement("extension-point");
    	extElem.setAttribute("id", extensionPointId);
    	extElem.setAttribute("version", extensionPointVersion);
    	//TODO: remove this workaround and check why namespaces of imported nodes don't get declared
    	extElem.setAttribute("xmlns:ixsl","http://www.w3.org/1999/XSL/Transform");
    	extElem.setAttribute("xmlns:pfx","http://www.schlund.de/pustefix/core");
    	extDoc.appendChild(extElem);
    	
    	try {
    		Collection<ViewExtension> extensions = ref.getExtensions();
    		
    		String[] themes = target.getThemes().getThemesArr();
    		if(themes.length == 0) themes = targetGenerator.getGlobalThemes().getThemesArr();
    		
    		for(ViewExtension ext: extensions) {
    			Element elem = extDoc.createElement("extension");
    			extElem.appendChild(elem);
    			elem.setAttribute("part", ext.getPartName());
    			elem.setAttribute("module", ext.getModule());
    			String path = ext.getResource().getOriginalURI().getPath();
    			int ind = path.indexOf("PUSTEFIX-INF");
    			path = path.substring(ind + "PUSTEFIX-INF".length() + 1);
    			elem.setAttribute("path", path);
    			elem.setAttribute("uri", ext.getResource().getOriginalURI().toASCIIString());
    			IncludeDocument doc = targetGenerator.getIncludeDocument(targetGenerator.getXsltVersion(), ext.getResource(), false);
    			Node themeNode = getThemeNode(context, doc, themes, ext.getPartName(), targetKey, target);
    			if(themeNode != null) {
    				Node impNode = extDoc.importNode(themeNode, true);
    				elem.appendChild(impNode);
    			} else {
    		    	StringBuilder sb = new StringBuilder();
    		    	for(String theme:themes) sb.append(theme + " ");
    		    	Element errorElem = extDoc.createElement("missing-theme");
    		    	errorElem.setAttribute("themes", sb.toString());
    		    	elem.appendChild(errorElem);
    			}
    		}
    	} catch(IllegalStateException x) {
    		Element errorElem = extDoc.createElement("missing-extension");
    		extElem.appendChild(errorElem);
    	}
    	
		extDoc = Xml.parse(context.getXsltVersion(), extDoc);
		return extDoc;
		
	}
	
	private Node getThemeNode(XsltContext context, IncludeDocument includeDoc, String[] themes, String part, String targetKey, VirtualTarget target) throws TransformerException, XMLException {
        
		Document doc = includeDoc.getDocument();
		
		//Extract part
		Node partNode;
        List<Node> nodes;
        try {
            nodes = XPath.select(doc, "/include_parts/part[@name='" + part + "']");
        } catch(TransformerException ex) {
        	//TODO: dependency logging
            throw ex;
        }
        if(nodes.size() == 0) {
        	//TODO: dependency logging
            XMLException ex = new XMLException("Part '" + part + "' doesn't exist.");
            if(target!=null) target.setStoredException(ex);
            throw ex;
        } else if(nodes.size() > 1) {
        	//TODO: dependency logging
            XMLException ex = new XMLException("Part '" + part + "' occurss more than once.");
            if(target!=null) target.setStoredException(ex);
            throw ex;
        } else {
        	partNode = nodes.get(0);
        }

        //Extract theme
        for(String theme:themes) {
            try {
                nodes = XPath.select(partNode, "theme[@name='" + theme + "']");
            } catch(TransformerException ex) {
                //TODO: dependency logging
                throw ex;
            }
            if(nodes.size() == 1) {
            	//TODO: dependency logging
            	return nodes.get(0);
            } else if(nodes.size() > 1) {
            	//TODO: dependency logging
                XMLException ex = new XMLException("Theme '" + theme + "' occurs more than once under part'" + part + "'.");                          
                target.setStoredException(ex);
                throw ex;
            }
        }
        //TODO dependency logging
        return null;
	}
        
    	
	public void invalidate(ViewExtensionPointReference reference) {
		String refKey = reference.getExtensionPointId() + "@" + reference.getExtensionPointVersion();
		synchronized(dependentTargets) {
			List<Target> targetList = dependentTargets.get(refKey);
			if(targetList != null) {
				Iterator<Target> it = targetList.iterator();
				while(it.hasNext()) {
					Target target = it.next();
					target.invalidate();
					it.remove();
				}
			}
		}
	}
	
	public void destroy() throws Exception {
		for(ViewExtensionPointReference ref:extensionPointReferences.values()) {
			ref.close();
		}
		
	}
	
}
