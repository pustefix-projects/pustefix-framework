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

import org.apache.log4j.Logger;
import org.pustefixframework.config.Constants;
import org.pustefixframework.config.project.SessionTimeoutInfo;
import org.pustefixframework.config.project.SessionTrackingStrategyInfo;
import org.pustefixframework.http.CookieOnlySessionTrackingStrategy;
import org.pustefixframework.http.CookieSessionTrackingStrategy;
import org.pustefixframework.http.SessionTrackingStrategy;
import org.pustefixframework.http.URLRewriteSessionTrackingStrategy;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marsching.flexiparse.parser.HandlerContext;
import com.marsching.flexiparse.parser.ParsingHandler;
import com.marsching.flexiparse.parser.exception.ParserException;


public class SessionInfoParsingHandler implements ParsingHandler {
    
    private Logger LOG = Logger.getLogger(SessionInfoParsingHandler.class);

    public void handleNode(HandlerContext context) throws ParserException {

        Element elem = (Element) context.getNode();
        NodeList nodes = elem.getElementsByTagNameNS(Constants.NS_PROJECT, "session-tracking-strategy");
        if(nodes.getLength() == 1) {
            String content = nodes.item(0).getTextContent().trim().toUpperCase();
            String warning;
            Class<? extends SessionTrackingStrategy> clazz;
            if(content.equals("COOKIE")) {
                clazz = CookieSessionTrackingStrategy.class;
                warning = getDeprecationWarning(new String[] {"COOKIE", "URL"});
            } else if(content.equals("URL")) {
                clazz = URLRewriteSessionTrackingStrategy.class;
                warning = getDeprecationWarning(new String[] {"URL"});
            } else if(content.equals("COOKIEONLY")) {
            	clazz = CookieOnlySessionTrackingStrategy.class;
                warning = getDeprecationWarning(new String[] {"COOKIE"});
            } else {
                throw new ParserException("Session tracking strategy '" + content + "' not supported.");
            }
            LOG.warn(warning);
            System.out.println(warning);
            SessionTrackingStrategyInfo strategyInfo = new SessionTrackingStrategyInfo();
            strategyInfo.setSessionTrackingStrategy(clazz);
            context.getObjectTreeElement().addObject(strategyInfo);
        } else if(nodes.getLength() > 1) {
            throw new ParserException("Multiple session-tracking-strategy elements found");
        }
        
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
    
    private String getDeprecationWarning(String[] modes) {
        StringBuilder sb = new StringBuilder();
        sb.append("################################## !!! DEPRECATION WARNING !!! #########################################\n");
        sb.append("#                                                                                                      #\n");
        sb.append("#  Configuration element <session-tracking-strategy> is deprecated and will be removed in the future.  #\n");
        sb.append("#  You should use the standard mechanism for session tracking mode configuration in web.xml instead.   #\n");
        sb.append("#  The currently selected session tracking strategy can be set in your web.xml as follows:             #\n");
        sb.append("#                                                                                                      #\n");
        sb.append("#    <session-config>                                                                                  #\n");
        for(String mode: modes) {
            if(mode.equals("COOKIE")) {
                sb.append("#      <tracking-mode>COOKIE</tracking-mode>                                                           #\n");
            } else if(mode.equals("URL")) {
                sb.append("#      <tracking-mode>URL</tracking-mode>                                                              #\n"); 
            }
        }
        sb.append("#    </session-config>                                                                                 #\n");
        sb.append("#                                                                                                      #\n");
        sb.append("########################################################################################################");
        return sb.toString();
    }

}
