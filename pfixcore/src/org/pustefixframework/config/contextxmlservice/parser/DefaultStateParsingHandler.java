/*
 * Place license here
 */

package org.pustefixframework.config.contextxmlservice.parser;

import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.ConfigurableState;

/**
 * 
 * @author mleidig
 *
 */
public class DefaultStateParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"class"}, null);
   
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
        
        String className = element.getAttribute("class");
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ParserException("Could not load class \"" + className + "\"!", e);
        }
        if (!ConfigurableState.class.isAssignableFrom(clazz)) {
            throw new ParserException("Default state class " + clazz + " does not implement " + ConfigurableState.class + " interface!");
        }
        config.setDefaultStaticState(clazz.asSubclass(ConfigurableState.class));
        
    }

}
