package org.pustefixframework.config.project.parser;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.ProjectInfo;
import org.w3c.dom.Element;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.util.ModuleFilterInfo;

public class DynamicIncludesParsingHandler extends CustomizationAwareParsingHandler {
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        Element element = (Element)context.getNode();
        String filter = element.getAttribute("filter").trim();
        if(filter.length() == 0) throw new ParserException("Missing 'filter' attribute value at 'dynamic-includes' element");
        ProjectInfo projectInfo = ParsingUtils.getSingleTopObject(ProjectInfo.class, context);
        ModuleFilterInfo filterInfo = ModuleFilterInfo.getInstance();
        filterInfo.addModuleFilter(projectInfo.getProjectName(), filter);
    }

}
