package org.pustefixframework.xmlgenerator.config.parser;

import java.lang.reflect.Method;
import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.extension.Extension;
import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtension;
import org.pustefixframework.extension.support.AbstractExtensionPoint;
import org.pustefixframework.extension.support.ExtensionTargetInfo;
import org.pustefixframework.util.VersionUtils;
import org.pustefixframework.xmlgenerator.config.model.XMLExtension;
import org.pustefixframework.xmlgenerator.config.model.XMLExtensionPoint;
import org.pustefixframework.xmlgenerator.config.model.XMLExtensionPointImpl;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class XMLExtensionParsingHandler implements ParsingHandler {

	public void handleNode(HandlerContext context) throws ParserException {
	       
		Element element = (Element)context.getNode();
		ParsingUtils.checkAttributes(element, new String[] {"type"}, null);
	        
		String type = element.getAttribute("type").trim();
	        
		NodeList nl = element.getElementsByTagNameNS(element.getNamespaceURI(), "extend");
		LinkedList<ExtensionTargetInfo> infos = new LinkedList<ExtensionTargetInfo>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element extendElement = (Element) nl.item(i);
			ParsingUtils.checkAttributes(extendElement, new String[] {"extension-point"}, new String[] {"version"});
			String extensionPoint = extendElement.getAttribute("extension-point").trim();
			String version = extendElement.getAttribute("version").trim();
			if (version.length() == 0) {
				version = "*";
			}
			if (!VersionUtils.isVersionOrRange(version)) {
				throw new ParserException("Not a valid version or version range: " + version);
			}
			ExtensionTargetInfo info = new ExtensionTargetInfo();
			info.setExtensionPoint(extensionPoint);
			info.setVersion(version);
			infos.add(info);
		}
		
		
		Class<?> clazz = getExtensionClass(type);
		if(clazz == null) throw new ParserException("Can't get extension implementation class for extension point type '" + type +"'");
		AbstractExtension<?, ? extends Extension> extension;
		try {
			 extension = (AbstractExtension<?, ? extends Extension>)clazz.newInstance();
		} catch (Exception x) {
			throw new ParserException("Can't create instance of extension class '" + clazz.getName() + "'", x);
		}    
		extension.setType(type);
		extension.setExtensionTargetInfos(infos);
		BundleContext bundleContext = ParsingUtils.getSingleTopObject(BundleContext.class, context);
		extension.setBundleContext(bundleContext);
		
		//TODO: make generics work without reflection
		try {
			Method method = extension.getClass().getMethod("setExtensionPointType", Class.class);
			method.invoke(extension, getExtensionPointInterface(type));
		} catch(Exception x) {
			throw new RuntimeException("Can't set extension point type", x);
		}
		try {
			extension.afterPropertiesSet();
		} catch(Exception x) {
			throw new RuntimeException("Error initializing extension", x);
		}
		
		context.getObjectTreeElement().addObject(extension);
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends AbstractExtensionPoint<?,?>> getdExtensionPointClass(String type) {
		Class<? extends AbstractExtensionPoint<?,? extends Extension>> clazz = (Class<? extends AbstractExtensionPoint<?,? extends Extension>>)XMLExtensionPointImpl.class.asSubclass(AbstractExtensionPoint.class);
		return clazz;
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends ExtensionPoint<? extends Extension>> getExtensionPointInterface(String type) {
		Class<? extends ExtensionPoint<? extends Extension>> clazz = (Class<? extends ExtensionPoint<? extends Extension>>)XMLExtensionPoint.class.asSubclass(ExtensionPoint.class);
		return clazz;
	}
	
	
	protected Class<? extends Extension> getExtensionClass(String type) {
		return XMLExtension.class;
	}
	
}
