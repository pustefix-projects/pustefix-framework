package org.pustefixframework.xmlgenerator.view;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.pustefixframework.resource.ResourceLoader;
import org.pustefixframework.resource.URLResource;
import org.pustefixframework.xmlgenerator.view.ViewExtensionContentHandler.UnsupportedDoctypeException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Parser which reads all extension and extension points from all XML resource
 * within a bundle.
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensionParser {

	private Log logger = LogFactory.getLog(ViewExtensionParser.class);
	
	private int fileCount;
	private List<ViewExtension> extensions;
	private List<ViewExtensionPoint> extensionPoints;
	
	public List<ViewExtension> getExtensions() {
		return extensions;
	}
	
	public List<ViewExtensionPoint> getExtensionPoints() {
		return extensionPoints;
	}
	
	public int getFileCount() {
		return fileCount;
	}
	
	public void parse(BundleContext bundleContext, ResourceLoader resourceLoader) {
		fileCount = 0;
		extensions = new ArrayList<ViewExtension>();
		extensionPoints = new ArrayList<ViewExtensionPoint>();
		Enumeration<?> e = bundleContext.getBundle().findEntries("PUSTEFIX-INF", "*.xml", true);
		if(e != null) {
		while(e.hasMoreElements()) {
			URL url = (URL)e.nextElement();
			URLResource resource;
			try {
				URI uri = new URI("bundle://" + bundleContext.getBundle().getSymbolicName() + "/" + url.toURI().getPath());
				resource = resourceLoader.getResource(uri, URLResource.class);
			} catch (URISyntaxException x) {
				throw new RuntimeException("Illegal resource URI: " + url.toString(), x);
			}
			try {
				read(resource, bundleContext, extensions, extensionPoints);
				fileCount++;
			} catch(UnsupportedDoctypeException x) {
				//ignore XML files containing no Pustefix include parts
			} catch(Exception x) {
				throw new RuntimeException("Error reading include part file: " + url.toString(), x);
			}
		}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Found " + extensions.size() + " extensions in bundle '" + bundleContext.getBundle().getSymbolicName() + "'");
			logger.debug("Found " + extensionPoints.size() + " extension points in bundle '" + bundleContext.getBundle().getSymbolicName() + "'");		
		}
	}
	
	private void read(URLResource resource, BundleContext bundleContext, List<ViewExtension> extensions, List<ViewExtensionPoint> extensionPoints) throws Exception {
		XMLReader xr = XMLReaderFactory.createXMLReader();
		ViewExtensionContentHandler handler = new ViewExtensionContentHandler(resource, bundleContext);
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);
		InputSource in = new InputSource();
		in.setByteStream(resource.getInputStream());
		in.setSystemId(resource.getURI().toASCIIString());
		xr.parse(in);
		extensions.addAll(handler.getExtensions());
		extensionPoints.addAll(handler.getExtensionPoints());
	}

}
