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

package org.pustefixframework.config.application.parser;

import org.pustefixframework.config.application.parser.internal.StaticResourceExtensionPointImpl;
import org.pustefixframework.config.generic.AbstractExtensionPointParsingHandler;
import org.pustefixframework.extension.ExtensionPoint;
import org.pustefixframework.extension.StaticResourceExtensionPoint;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * Handles the declaration of a static resource extension point.
 */
public class StaticResourceExtensionPointParsingHandler extends AbstractExtensionPointParsingHandler {

    @Override
    protected ExtensionPoint<?> createExtensionPoint(String id, String type, String version, String cardinality, HandlerContext context) throws ParserException {
        StaticResourceExtensionPointImpl extensionPoint = new StaticResourceExtensionPointImpl();
        extensionPoint.setId(id);
        extensionPoint.setType(type);
        extensionPoint.setVersion(version);
        extensionPoint.setCardinality(cardinality);
        return extensionPoint;
    }

    @Override
    protected Class<?>[] getExportedInterfaces(String id, String type, String version, String cardinality, HandlerContext context) throws ParserException {
        return new Class<?>[] { StaticResourceExtensionPoint.class };
    }

}