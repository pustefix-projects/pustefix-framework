/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;

/**
 * 
 * @author mleidig
 *
 */
public class ServletInfoParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name", "depend"}, null);
   
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
        
        String servletName = element.getAttribute("name");
        config.setServletName(servletName);
        String dependFile = element.getAttribute("depend");
        config.setDependFile(dependFile);
        
    }

}
