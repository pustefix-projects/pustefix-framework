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
        ParsingUtils.checkAttributes(element, new String[] {"node"}, new String[] {"class","bean-ref"});
        
        PageRequestConfigImpl pageConfig = ParsingUtils.getSingleTopObject(PageRequestConfigImpl.class, context);
        String node = element.getAttribute("node").trim();
       
        String className = element.getAttribute("class").trim();
        String beanRef = element.getAttribute("bean-ref").trim();
        if (className.length()==0 && beanRef.length()==0) {
            throw new ParserException("Either attribute 'class' or attribute 'bean-ref' required.");
        }
        //TODO: inject resources in state
        if(className.length()>0) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new ParserException("Could not load resource interface \"" + className + "\"!");
            }
            pageConfig.addContextResource(node, clazz);
        }
        
    }

}
