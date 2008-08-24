/*
 * Place license here
 */

package org.pustefixframework.config.contextxmlservice.parser;

import org.pustefixframework.config.contextxmlservice.parser.internal.ContextConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.ContextXMLServletConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * 
 * @author mleidig
 *
 */
public class ContextParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"defaultpage"}, new String[] {"synchronized"});
        
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);
        
        ContextConfigImpl ctxConfig = new ContextConfigImpl();
        // Navigation is stored in depend.xml
        ctxConfig.setNavigationFile(config.getDependFile());
        ctxConfig.setDefaultState(config.getDefaultStaticState());
        ctxConfig.setDefaultPage(element.getAttribute("defaultpage"));
        String syncStr = element.getAttribute("synchronized");
        if (syncStr != null) {
            ctxConfig.setSynchronized(Boolean.parseBoolean(syncStr));
        } else {
            ctxConfig.setSynchronized(true);
        }
        config.setContextConfig(ctxConfig);
        context.getObjectTreeElement().addObject(ctxConfig);
    }

}
