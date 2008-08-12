/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.State;
import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class DefaultIHandlerStateParsingHandler implements ParsingHandler {

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
        if (!State.class.isAssignableFrom(clazz)) {
            throw new ParserException("Default IHandler state class " + clazz + " does not implement " + State.class + " interface!");
        }
        config.setDefaultIHandlerState(clazz.asSubclass(State.class));
        
    }

}
