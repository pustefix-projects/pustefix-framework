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

import org.pustefixframework.config.Constants;
import org.pustefixframework.config.project.ApplicationInfo;
import org.pustefixframework.http.CookieSessionTrackingStrategy;
import org.pustefixframework.http.SessionTrackingStrategy;
import org.pustefixframework.http.URLRewriteSessionTrackingStrategy;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;

/**
 * 
 * @author mleidig@schlund.de
 *
 */
public class ApplicationInfoParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        
        ApplicationInfo appInfo = new ApplicationInfo();
        
        Element elem = (Element) context.getNode();
        
        Class<? extends SessionTrackingStrategy> clazz;
        NodeList nodes = elem.getElementsByTagNameNS(Constants.NS_PROJECT, "session-tracking-strategy");
        if(nodes.getLength() == 0) {
            clazz = CookieSessionTrackingStrategy.class;
        } else if(nodes.getLength() == 1) {
            String content = nodes.item(0).getTextContent().trim().toUpperCase();
            if(content.equals("COOKIE")) {
                clazz = CookieSessionTrackingStrategy.class;
            } else if(content.equals("URL")) {
                clazz = URLRewriteSessionTrackingStrategy.class;
            } else {
                throw new ParserException("Session tracking strategy '" + content + "' not supported.");
            }
        } else {
            throw new ParserException("Multiple session-tracking-strategy elements found.");
        }
        try {
            SessionTrackingStrategy strategy = clazz.newInstance();
            appInfo.setSessionTrackingStrategy(strategy);
        } catch(Exception x) {
            throw new ParserException("Can't create SessionTrackingStrategy instance", x);
        }
        
        context.getObjectTreeElement().addObject(appInfo);
        
    }
    
}
