package org.pustefixframework.xmlgenerator.config.parser;

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.extension.Extension;
import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.support.AbstractExtensionPoint;
import org.pustefixframework.xmlgenerator.config.model.Configuration;
import org.pustefixframework.xmlgenerator.config.model.ExtensibleList;
import org.pustefixframework.xmlgenerator.config.model.IncludeConfig;
import org.pustefixframework.xmlgenerator.config.model.ModelElement;
import org.pustefixframework.xmlgenerator.config.model.ParameterConfig;
import org.pustefixframework.xmlgenerator.config.model.XMLExtension;
import org.pustefixframework.xmlgenerator.config.model.XMLExtensionPoint;
import org.pustefixframework.xmlgenerator.config.model.XMLExtensionPointImpl;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class XMLExtensionPointParsingHandler implements ParsingHandler {

	public void handleNode(HandlerContext context) throws ParserException {
		
		Element element = (Element)context.getNode();
	
		String id = ParsingUtils.getAttribute(element, "id", true);
		String type = ParsingUtils.getAttribute(element, "type", true);
		String version = ParsingUtils.getAttribute(element, "version", "0.0.0");
		String cardinality = ParsingUtils.getAttribute(element, "cardinality" , "0..n");
		
		Class<? extends AbstractExtensionPoint<?, ? extends Extension>> clazz = getExtensionPointClass(type);
		if(clazz == null) throw new ParserException("Can't get implementation class for extension point type '" + type +"'");
		AbstractExtensionPoint<?, ? extends Extension> extensionPoint;
		try {
			 extensionPoint = (AbstractExtensionPoint<?, ? extends Extension>)clazz.newInstance();
		} catch (Exception x) {
			throw new ParserException("Can't create instance of extension point class '" + clazz.getName() + "'", x);
		}
		
		extensionPoint.setId(id);
		extensionPoint.setType(type);
		extensionPoint.setVersion(version);
		extensionPoint.setCardinality(cardinality);
		
		Hashtable<String,String> properties = new Hashtable<String,String>();
		properties.put("extension-point", id);
        properties.put("type", type);
        properties.put("version", version);
		
		BundleContext bundleContext = ParsingUtils.getSingleTopObject(BundleContext.class, context);
		bundleContext.registerService(new String[] {ExtensionPoint.class.getName(), getExtensionPointInterface(type).getName()}, extensionPoint, properties);
		
		registerExtensionPoint(extensionPoint, context);
		
	}
	
	

	protected Class<? extends AbstractExtensionPoint<?,? extends Extension>> getExtensionPointClass(String type) {
		Class<? extends AbstractExtensionPoint<?,? extends Extension>> clazz = (Class<? extends AbstractExtensionPoint<?,? extends Extension>>)XMLExtensionPointImpl.class.asSubclass(AbstractExtensionPoint.class);
		return clazz;
	}
	
	
	protected Class<? extends ExtensionPoint<? extends Extension>> getExtensionPointInterface(String type) {
		Class<? extends ExtensionPoint<? extends Extension>> clazz = (Class<? extends ExtensionPoint<? extends Extension>>)XMLExtensionPoint.class.asSubclass(ExtensionPoint.class);
		return clazz;
	}

	
	protected void registerExtensionPoint(ExtensionPoint<? extends Extension> extensionPoint, HandlerContext context) throws ParserException {
		
		XMLExtensionPoint.Type type = XMLExtensionPoint.Type.get(extensionPoint.getType());
		if(type == null) throw new ParserException("Extension point type not supported: " + extensionPoint.getType());
		
		ExtensibleList<?> list = ParsingUtils.getFirstTopObject(ExtensibleList.class, context, false);
		if(list!=null) {
			list.addExtensionPoint((XMLExtensionPointImpl)extensionPoint);
		} else {
			XMLExtension<? extends ModelElement> ext = ParsingUtils.getFirstTopObject(XMLExtension.class, context, false);
			if(ext != null) {
				ext.addExtensionPoint((XMLExtensionPointImpl)extensionPoint);
			} else {
				Configuration config = ParsingUtils.getSingleTopObject(Configuration.class, context);
				if(config != null) {
					list = null;
					if(type == XMLExtensionPoint.Type.STANDARD_PAGE) {
						list = (ExtensibleList<?>)config.getStandardPages();
					} else if(type == XMLExtensionPoint.Type.NAVIGATION_PAGE) {
						list = (ExtensibleList<?>)config.getPages();
					} else if(type == XMLExtensionPoint.Type.NAMESPACE_DECLARATION) {
						list = (ExtensibleList<?>)config.getNamespaceDeclarations();
					} else if(type == XMLExtensionPoint.Type.STANDARD_MASTER) {
						list = (ExtensibleList<?>)config.getStandardMasters();
					} else if(type == XMLExtensionPoint.Type.STANDARD_METATAGS) {
						list = (ExtensibleList<?>)config.getStandardMetatags();
					} else if(type == XMLExtensionPoint.Type.TARGET_DEFINITION) {
						list = (ExtensibleList<?>)config.getTargetDefs();
					} else if(type == XMLExtensionPoint.Type.XSL_INCLUDE) {
						IncludeConfig incConfig = ParsingUtils.getFirstTopObject(IncludeConfig.class, context, false);
						if(incConfig != null) {
							list = (ExtensibleList<?>)incConfig.getIncludes();
						} else {
							list = (ExtensibleList<?>)config.getIncludes();
						}
					} else if(type == XMLExtensionPoint.Type.XSL_PARAMETER) {
						ParameterConfig paramConfig = ParsingUtils.getFirstTopObject(ParameterConfig.class, context, false);
						if(paramConfig != null) {
							list = (ExtensibleList<?>)paramConfig.getParameters();
						} else {
							list = (ExtensibleList<?>)config.getParameters();
						}
					} else {
						throw new ParserException("Extension point not supported: " + extensionPoint.getId()+" "+extensionPoint.getType());
					}
					list.addExtensionPoint((XMLExtensionPointImpl)extensionPoint);
				} else throw new ParserException("Can't register extension point");
			}
		}
		
	}
	
}
