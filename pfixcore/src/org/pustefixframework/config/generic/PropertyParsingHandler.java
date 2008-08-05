/*
 * Place license here
 */

package org.pustefixframework.config.generic;

import java.util.Collection;
import java.util.Properties;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class PropertyParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        Collection<Properties> propertiesCollection = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(Properties.class);
        if (propertiesCollection.size() == 0) {
            throw new ParserException("PropertyParsingHandler expects a Properties object in tree");
        }
        Properties properties = propertiesCollection.iterator().next();
        
        Element elem = (Element) context.getNode();
        String name = elem.getAttribute("name");
        if (name.length() == 0) {
            throw new ParserException("PropertyParsingHandler expects name attribte");
        }
        String value = elem.getTextContent();
        CustomizationInfo info = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(CustomizationInfo.class).iterator().next();
        value = info.replaceVariables(value);
        
        properties.setProperty(name, value);
    }

}
