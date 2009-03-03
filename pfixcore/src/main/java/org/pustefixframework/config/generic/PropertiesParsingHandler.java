/*
 * Place license here
 */

package org.pustefixframework.config.generic;

import java.util.Properties;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

public class PropertiesParsingHandler extends CustomizationAwareParsingHandler {

    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        context.getObjectTreeElement().addObject(new Properties());
    }

}
