package org.pustefixframework.webservices.spring;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.webservices.fault.Parameterizable;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Parses parameters and sets them at configuration object implementing Parameterizable
 * 
 * @author mleidig@schlund.de
 *
 */
public class ParamParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        String name = element.getAttribute("name").trim();
        if(name.length() == 0) throw new ParserException("Element 'param' requires 'name' attribute.");
        String value = element.getAttribute("value").trim();
        if(value.length() == 0) throw new ParserException("Element 'param' requires 'value' attribute.");
        
        Parameterizable params = ParsingUtils.getFirstTopObject(Parameterizable.class, context, true);
        params.addParam(name, value);
    }
    
}