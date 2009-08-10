package org.pustefixframework.xmlgenerator.config.model;

import java.util.HashMap;
import java.util.Map;

import org.pustefixframework.extension.ExtensionPoint;


/**
 * Interface for xml-generator extension points
 * 
 * @author mleidig@schlund.de
 *
 * @param <T> configuration data object type
 */
public interface XMLExtensionPoint<T extends ModelElement> extends ExtensionPoint<XMLExtension<T>> {

	public enum Type {
		
		NAMESPACE_DECLARATION ("xml.namespace"),
		NAVIGATION_PAGE       ("xml.page"),
		STANDARD_MASTER       ("xml.standardmaster"),
		STANDARD_METATAGS     ("xml.standardmetatags"),
		STANDARD_PAGE         ("xml.standardpage"),
		TARGET_DEFINITION     ("xml.target"),
		XSL_INCLUDE           ("xml.include"),
		XSL_PARAMETER         ("xml.param"),
		DEPENDENCY_AUX        ("xml.depaux"),
		DEPENDENCY_XML        ("xml.depxml"),
		DEPENDENCY_XSL        ("xml.depxsl");
	
		private final String xmlType;
		private static final Map<String,Type> reverse = new HashMap<String,Type>();
		
		static {
			for(Type type: Type.values()) reverse.put(type.xmlType, type);
		}
		
		Type(String xmlType) {
			this.xmlType = xmlType;
		}
		
		String xmlType() {
			return xmlType;
		}
		
		public static Type get(String xmlType) {
			return reverse.get(xmlType);
		}
		
	}
	
}
