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

import org.pustefixframework.config.contextxmlservice.PustefixContextXMLRequestHandlerConfig;
import org.pustefixframework.config.contextxmlservice.parser.internal.PustefixContextXMLRequestHandlerConfigImpl;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * Parsing handler for context XML module configuration.
 * Creates a dummy instance of {@link PustefixContextXMLRequestHandlerConfig}
 * for other handlers which depend on the existence of this object.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextXMLDummyParsingHandler implements ParsingHandler {

    public void handleNode(HandlerContext context) throws ParserException {
        PustefixContextXMLRequestHandlerConfigImpl ctxConfig = new PustefixContextXMLRequestHandlerConfigImpl();
        context.getObjectTreeElement().addObject(ctxConfig);
    }

}
