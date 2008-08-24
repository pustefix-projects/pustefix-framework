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

import de.schlund.pfixcore.workflow.ContextInterceptor;

public class ContextInterceptorParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
       
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"class"}, null);
        
        ContextXMLServletConfigImpl config = ParsingUtils.getSingleTopObject(ContextXMLServletConfigImpl.class, context);     
        
        String className = element.getAttribute("class").trim();
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ParserException("Could not load interceptor class " + className, e);
        }
        if (!ContextInterceptor.class.isAssignableFrom(clazz)) {
            throw new ParserException("Context interceptor " + clazz + " does not implement " + ContextInterceptor.class + " interface!");
        }
        
        Element parent = (Element)element.getParentNode();
        if (parent.getNodeName().equals("start")) {
            config.getContextConfig().addStartInterceptor(clazz.asSubclass(ContextInterceptor.class));
        }
        if (parent.getNodeName().equals("end")) {
            config.getContextConfig().addEndInterceptor(clazz.asSubclass(ContextInterceptor.class));
        }
        if (parent.getNodeName().equals("postrender")) {
            config.getContextConfig().addPostRenderInterceptor(clazz.asSubclass(ContextInterceptor.class));
        }
        
    }

}
