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
import org.pustefixframework.config.project.SessionTimeoutInfo;
import org.pustefixframework.config.project.SessionTrackingStrategyInfo;
import org.pustefixframework.http.BotSessionTrackingStrategy;
import org.pustefixframework.http.CookieOnlySessionTrackingStrategy;
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
public class SessionInfoParsingHandler implements ParsingHandler {
    
    public void handleNode(HandlerContext context) throws ParserException {
        
        SessionTrackingStrategyInfo strategyInfo = new SessionTrackingStrategyInfo();
        
        Class<? extends SessionTrackingStrategy> clazz;
        
        Element elem = (Element) context.getNode();
        NodeList nodes = elem.getElementsByTagNameNS(Constants.NS_PROJECT, "session-tracking-strategy");
        if(nodes.getLength() == 0) {
            clazz = CookieSessionTrackingStrategy.class;
        } else if(nodes.getLength() == 1) {
            String content = nodes.item(0).getTextContent().trim().toUpperCase();
            if(content.equals("COOKIE")) {
                clazz = CookieSessionTrackingStrategy.class;
            } else if(content.equals("URL")) {
                clazz = URLRewriteSessionTrackingStrategy.class;
            } else if(content.equals("BOT")) {
                clazz = BotSessionTrackingStrategy.class;
            } else if(content.equals("COOKIEONLY")) {
            	clazz = CookieOnlySessionTrackingStrategy.class;
            } else {
                throw new ParserException("Session tracking strategy '" + content + "' not supported.");
            }
        } else throw new ParserException("Multiple session-tracking-strategy elements found");
       
        strategyInfo.setSessionTrackingStrategy(clazz);
        context.getObjectTreeElement().addObject(strategyInfo);
        
        nodes = elem.getElementsByTagNameNS(Constants.NS_PROJECT, "initial-session-timeout");
        if(nodes.getLength() == 1) {
            Element node = (Element)nodes.item(0);
            int timeout = Integer.parseInt(node.getAttribute("value"));
            int limit = Integer.parseInt(node.getAttribute("requestlimit"));
            SessionTimeoutInfo info = new SessionTimeoutInfo();
            info.setInitialTimeout(timeout);
            info.setRequestLimit(limit);
            context.getObjectTreeElement().addObject(info);
        } else if(nodes.getLength() > 1) {
            throw new ParserException("Multiple initial-session-timeout elements found");
        }
        
    }
    
}
