/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.impl.PageRequestConfigImpl;

public class PageRequestOutputResourceParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"node"}, new String[] {"class"});
        
        PageRequestConfigImpl pageConfig = ParsingUtils.getSingleTopObject(PageRequestConfigImpl.class, context);
        String node = element.getAttribute("node").trim();
        if (node.isEmpty()) {
            throw new ParserException("Mandatory attribute \"node\" is missing!");
        }
        String className = element.getAttribute("class").trim();
        if (className.isEmpty()) {
            throw new ParserException("Mandatory attribute \"class\" is missing!");
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ParserException("Could not load resource interface \"" + className + "\"!");
        }
        pageConfig.addContextResource(node, clazz);
        
    }

}
