/*
 * Place license here
 */

package org.pustefixframework.config.project.parser;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.project.XMLGeneratorInfo;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class XMLGeneratorInfoParsingHandler extends CustomizationAwareParsingHandler {

    public void handleNodeIfActive(HandlerContext context) throws ParserException {
        String uri = context.getNode().getTextContent().trim();
        XMLGeneratorInfo info = new XMLGeneratorInfo(uri);
        context.getObjectTreeElement().addObject(info);
    }

}
