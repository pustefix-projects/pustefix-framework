package org.pustefixframework.config.application.parser;

import org.osgi.framework.BundleContext;
import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.resource.internal.DynamicIncludeModuleFilter;
import org.pustefixframework.resource.internal.DynamicIncludeModuleFilterImpl;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class DynamicIncludeModuleFilterParsingHandler extends CustomizationAwareParsingHandler {
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        
        Element element = (Element)context.getNode();
        String filterExpression = element.getAttribute("filter").trim();
        if(filterExpression.length() == 0) throw new ParserException("Missing 'filter' attribute value at 'dynamic-includes' element");
    
        ConfigurableOsgiBundleApplicationContext appContext = ParsingUtils.getSingleTopObject(ConfigurableOsgiBundleApplicationContext.class, context);
        BundleContext bundleContext = appContext.getBundleContext();
        
        String application = bundleContext.getBundle().getSymbolicName();
        DynamicIncludeModuleFilter filter = new DynamicIncludeModuleFilterImpl(application, filterExpression);
        bundleContext.registerService(DynamicIncludeModuleFilter.class.getName(), filter, null);
    }

}
