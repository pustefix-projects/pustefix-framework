/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.impl.SSLOption;

/**
 * 
 * @author mleidig
 *
 */
public class SSLParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"force"}, null);
   
        SSLOption sslOption = ParsingUtils.getSingleTopObject(SSLOption.class, context);     
        sslOption.setSSL(Boolean.parseBoolean(element.getAttribute("force")));

    }

}
