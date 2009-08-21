/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.config.contextxmlservice.parser;

import java.util.Collection;

import org.pustefixframework.config.contextxmlservice.PageFlowStepConfig;
import org.pustefixframework.config.contextxmlservice.PageFlowStepHolder;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowConfigImpl;
import org.pustefixframework.config.contextxmlservice.parser.internal.PageFlowStepConfigImpl;
import org.pustefixframework.config.generic.ParsingUtils;
import org.w3c.dom.Element;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixcore.workflow.FlowStep;


/**
 * 
 * @author mleidig
 *
 */
public class PageFlowStepParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        if (context.getRunOrder() == RunOrder.START) {
            handleNodeStart(context);
        } else if (context.getRunOrder() == RunOrder.END) {
            handleNodeEnd(context);
        }
    }

    public void handleNodeStart(HandlerContext context) throws ParserException {
        Element element = (Element)context.getNode();
        ParsingUtils.checkAttributes(element, new String[] {"name"}, new String[] {"stophere"});
        
        PageFlowConfigImpl flowConfig = null;
        Collection<PageFlowConfigImpl> flowConfigs = context.getObjectTreeElement().getObjectsOfTypeFromTopTree(PageFlowConfigImpl.class);
        if (flowConfigs.size() > 0) {
            flowConfig = flowConfigs.iterator().next();
        }
        
        String pageName = element.getAttribute("name").trim();
        PageFlowStepConfigImpl stepConfig = new PageFlowStepConfigImpl();
        stepConfig.setPage(pageName);
        String stophere = element.getAttribute("stophere").trim();
        if (stophere.length()>0) {
            stepConfig.setStopHere(Boolean.parseBoolean(stophere));
        } else {
            // Check whether we have a flow config. If the flow step
            // is defined within an extension, the flow configuration
            // will not be available.
            if (flowConfig != null) {
                stepConfig.setStopHere(flowConfig.isStopNext());
            }
        }
        context.getObjectTreeElement().addObject(stepConfig);
    }

    public void handleNodeEnd(HandlerContext context) throws ParserException {
        PageFlowStepConfig stepConfig = ParsingUtils.getSingleObject(PageFlowStepConfig.class, context);
        
        final FlowStep flowStep = new FlowStep(stepConfig);
        context.getObjectTreeElement().addObject(new PageFlowStepHolder() {
    
            public Object getPageFlowStepObject() {
                return flowStep;
            }
            
        });
    }

}
