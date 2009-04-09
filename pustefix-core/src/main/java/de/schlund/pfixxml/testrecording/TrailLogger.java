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
 *
 */

package de.schlund.pfixxml.testrecording;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.pustefixframework.http.AbstractPustefixRequestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.SPDocument;
import de.schlund.pfixxml.util.Xml;

/**
 * Creates a trail by logging request/response pairs.
 *
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
public class TrailLogger extends NotificationBroadcasterSupport implements TrailLoggerMBean {
    private final static Logger LOG = Logger.getLogger(TrailLogger.class);
    
    public static final String NOTIFICATION_TYPE = "step";
    public static final String CLOSE_TYPE ="close";
    
    // TODO: ugly static thing code. 
    // maps visit_ids on TrailLogger
    public static final Map<String,TrailLogger> map = new HashMap<String,TrailLogger>();
    
    public static void log(PfixServletRequest preq, SPDocument resdoc, HttpSession session) {
        TrailLogger logger;
        String visit;
        
        if (session == null) {
            return; // no sessions, no trails ...
        }
        visit = lookupVisit(session);
        if (visit != null) {
            logger = (TrailLogger) map.get(visit);
            if (logger != null) {
                logger.log(preq, resdoc);
            }
        }
    }

    public static String getVisit(HttpSession session) {
        String visit;
        
        visit = lookupVisit(session);
        if (visit == null) {
            Enumeration<?> enm = session.getAttributeNames();
            System.out.println("session " + session);
            while (enm.hasMoreElements()) {
                String valName = (String)enm.nextElement();
                System.out.println("  " + valName + " -> " + session.getAttribute(valName));
            }
            throw new RuntimeException("no visit");
        }
        return visit;
    }
    
    public static String lookupVisit(HttpSession session) {
        return (String) session.getAttribute(AbstractPustefixRequestHandler.VISIT_ID);
    }
    
    //--

    private final String visit;
    private int sequenceNumber;
    
    public TrailLogger(String visit) throws IOException {
        this.visit = visit;
        this.sequenceNumber = 0;
        map.put(visit, this);
    }
    
    public void log(PfixServletRequest request, SPDocument response) {
        String step;
        Notification n;
        
        step = Xml.serialize(createStep(request, response), true, false);
        LOG.debug("step " + sequenceNumber + " "+ step);
        n = new Notification(NOTIFICATION_TYPE, this, sequenceNumber++);
        n.setUserData(step);
        sendNotification(n);
    }
    
    public void stop() {
        if (map.remove(visit) != this) {
            throw new IllegalStateException();
        }
        sendNotification(new Notification(CLOSE_TYPE, this, sequenceNumber++));
    }
    
    //-- create xml
    
    private static Document createStep(PfixServletRequest request, SPDocument spdoc) {
        Document doc = Xml.createDocument();
        Element step = doc.createElement("step");
        doc.appendChild(step);
        step.appendChild(doc.importNode(createRequest(request), true));
        step.appendChild(doc.importNode(createResponse(spdoc), true));
        return doc;
    }

    private static void addElement(Element root, String name, String text, String attr_name, String attr_value) {
        Element new_ele = root.getOwnerDocument().createElement(name);
        if (attr_name != null) {
            new_ele.setAttribute(attr_name, attr_value);
        }
        Text text_node = root.getOwnerDocument().createTextNode(text);
        new_ele.appendChild(text_node);
        root.appendChild(new_ele);
    }
    
    private static Node createRequest(PfixServletRequest request) {
        Document doc = Xml.createDocument();
        Element ele = doc.createElement("request");
        String uri = request.getRequestURI();
        String path = uri.substring(0, uri.indexOf(';'));
        addElement(ele, "path", path, null, null);
        
        Element ele_params = doc.createElement("params");
        String[] req_param_names = request.getRequestParamNames();
        for (int i = 0; i < req_param_names.length; i++) {
            String name = req_param_names[i];
            RequestParam[] values = request.getAllRequestParams(name);
            for (int j = 0; j < values.length; j++) {
                if (!values[j].isSynthetic()) {
                    addElement(ele_params, "param", values[j].getValue(), "name", name);
                }
            }
        }
        ele.appendChild(ele_params);
        return ele;
    }
    
    private static Node createResponse(SPDocument doc) {
        return doc.getDocument().getFirstChild();
    }
}
