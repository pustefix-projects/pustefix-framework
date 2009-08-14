package org.pustefixframework.xmlgenerator.view;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.pustefixframework.resource.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX ContentHandler which reads in extension and extension point definitions
 * from include-part and pfx:document files
 * 
 * @author mleidig@schlund.de
 *
 */
public class ViewExtensionContentHandler extends DefaultHandler {

	public final static String PFX_NAMESPACE = "http://www.schlund.de/pustefix/core";
	
	private final Resource resource;
	private final BundleContext bundleContext;
	private final List<ViewExtension> extensions;
	private final List<ViewExtensionPoint> extensionPoints;
	
	private Locator locator;
	
	private boolean isIncludePartFile;
	private int level;
	private String partName;
	private List<ExtensionTargetInfo> extensionTargetInfos;
	
	public ViewExtensionContentHandler(Resource resource, BundleContext bundleContext) {
		this.resource = resource;
		this.bundleContext = bundleContext;
		extensions = new ArrayList<ViewExtension>();
		extensionPoints = new ArrayList<ViewExtensionPoint>();
	}
	
	public List<ViewExtension> getExtensions() {
		return extensions;
	}
	
	public List<ViewExtensionPoint> getExtensionPoints() {
		return extensionPoints;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		level++;	
		if(level == 1) {
			if(localName.equals("include_parts")) {
				isIncludePartFile = true;
			} else if(!(localName.equals("document") && PFX_NAMESPACE.equals(uri))) {
				throw new UnsupportedDoctypeException("Found no 'include_parts' or 'pfx:document' root element.", locator);
			}
		} else if(level == 2 && isIncludePartFile) {
			if(localName.equals("part")) {
				partName = attributes.getValue("name");
				if(partName != null) partName = partName.trim();
				if(partName == null || partName.equals("")) {
					throw new SAXParseException("Found 'part' element without a 'name' attribute value.", locator);
				}
			}
		} else if(level == 3 && isIncludePartFile) {
			if(partName != null && localName.equals("extends")) {
				String ext = attributes.getValue("extension-point");
				if(ext != null) ext = ext.trim();
				if(ext == null || ext.equals("")) {
					throw new SAXParseException("Found 'extends' without an 'extension-point' attribute value.", locator);
				}
				String version = attributes.getValue("version");
				if(version != null) version = version.trim();
				if(version == null || version.equals("")) {
					version = "*";
				}
				ExtensionTargetInfo info = new ExtensionTargetInfo();
				info.setExtensionPoint(ext);
				info.setVersion(version);
				if(extensionTargetInfos == null) extensionTargetInfos = new ArrayList<ExtensionTargetInfo>();
				extensionTargetInfos.add(info);
			}
		} else {
			if(localName.equals("extension-point") && PFX_NAMESPACE.equals(uri)) {
				String id = attributes.getValue("id");
				if(id != null) id = id.trim();
				if(id == null || id.equals("")) {
					throw new SAXParseException("Found 'extension-point' element without an 'id' attribute value.", locator);
				}
				String version = attributes.getValue("version");
				if(version != null) version = version.trim();
				if(version == null || version.equals("")) {
					version = "0.0.0";
				}
				String cardinality = attributes.getValue("cardinality");
				if(cardinality != null) cardinality = cardinality.trim();
				if(cardinality == null || cardinality.equals("")) {
					cardinality = "0..n";
				}
				
				ViewExtensionPointImpl ext = new ViewExtensionPointImpl();
				ext.setId(id);
				ext.setType(ViewExtensionPoint.TYPE);
				ext.setVersion(version);
				ext.setCardinality(cardinality);
				extensionPoints.add(ext);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(level == 2 && isIncludePartFile) {
			if(localName.equals("part")) {
				if(extensionTargetInfos != null) {
					ViewExtensionImpl ext = new ViewExtensionImpl();
					ext.setExtensionTargetInfos(extensionTargetInfos);
					ext.setType(ViewExtensionPoint.TYPE);
					ext.setPartName(partName);
					ext.setResource(resource);
					ext.setExtensionPointType(ViewExtensionPoint.class);
					ext.setBundleContext(bundleContext);
					try {
						ext.afterPropertiesSet();
					} catch(Exception x) {
						x.printStackTrace();
						throw new SAXException("Error while setting up view extension bean", x);
					}
					extensions.add(ext);
				}
				partName = null;
				if(extensionTargetInfos!=null) extensionTargetInfos.clear();
			}
		}
		level--;
	}

	@Override
    public void setDocumentLocator(Locator locator) {
    	this.locator = locator;
    }
	
	
	class UnsupportedDoctypeException extends SAXParseException {
		
		private static final long serialVersionUID = 4558887201326253781L;

		public UnsupportedDoctypeException(String msg, Locator locator) {
			super(msg, locator);
		}
		
	}
	
}
