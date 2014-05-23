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

package org.pustefixframework.config.project.parser;

import java.util.Properties;

import org.pustefixframework.config.customization.CustomizationAwareParsingHandler;
import org.pustefixframework.config.generic.ParsingUtils;
import org.pustefixframework.config.project.EditorInfo;

import com.marsching.flexiparse.configuration.RunOrder;
import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.EnvironmentProperties;


public class EditorInfoParsingHandler extends CustomizationAwareParsingHandler {
    
    @Override
    protected void handleNodeIfActive(HandlerContext context) throws ParserException {
        if(context.getNode().getLocalName().equals("editor")) {
            if(context.getRunOrder() == RunOrder.START) {
                EditorInfo info = new EditorInfo();
                context.getObjectTreeElement().addObject(info);
            } else {
                EditorInfo info = ParsingUtils.getSingleObject(EditorInfo.class, context);
                overrideByEnvironment(info);
            }
        } else if(context.getNode().getLocalName().equals("application")) {
            if(context.getRunOrder() == RunOrder.START) {
                EditorInfo info = ParsingUtils.getSingleSubObjectFromRoot(EditorInfo.class, context, false);
                if(info == null) {
                    info = new EditorInfo();
                    overrideByEnvironment(info);
                    if(info.isEnabled()) {
                        context.getObjectTreeElement().addObject(info);
                    }
                }
            }
        }
    }
    
    private void overrideByEnvironment(EditorInfo editorInfo) {
        Properties envProps = EnvironmentProperties.getProperties();
        String val = envProps.getProperty("editor.enabled");
        if(val != null) {
            boolean enabled = Boolean.parseBoolean(val);
            editorInfo.setEnabled(enabled);
        }
        val = envProps.getProperty("editor.location");
        if(val != null) {
            editorInfo.setLocation(val);
        }
    }
    
}
