/*
 * Place license here
 */

package org.pustefixframework.config.contextxml.parser;

import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.impl.ContextConfigImpl;
import de.schlund.pfixxml.config.impl.ContextXMLServletConfigImpl;
import de.schlund.pfixxml.config.impl.PageRequestConfigImpl;

public class PageRequestParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name"}, new String[] {"copyfrom"});
        
        ContextConfigImpl ctxConfig = ParsingUtils.getSingleSubObjectFromRoot(ContextConfigImpl.class, context);
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
       
        PageRequestConfigImpl pageConfig = new PageRequestConfigImpl();
        String pageName = element.getAttribute("name").trim();
        if (pageName.isEmpty()) {
            throw new ParserException("Mandatory attribute \"name\" is missing!");
        }
        System.out.println("ADD PAGE: "+pageName);
        pageConfig.setPageName(pageName);
        String copyfrom = element.getAttribute("copyfrom").trim();
        if (!copyfrom.isEmpty()) {
            pageConfig.setCopyFromPage(copyfrom);
        }
        
        pageConfig.setDefaultStaticState(config.getDefaultStaticState());
        pageConfig.setDefaultIHandlerState(config.getDefaultIHandlerState());
        ctxConfig.addPageRequest(pageConfig);
        
        context.getObjectTreeElement().addObject(pageConfig);
        
    }

}
