package de.schlund.pfixxml.exceptionhandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;

/**
 * Creates a tree-like message and logs it. Uses Standard classes only to avoid
 * classes shared between different apps.
 */
public class MessageBuilder implements Serializable {
    private static final String LOGGER_NAME = "de.schlund.pfixxml.MESSAGES";
    private static Logger LOGGER;
    private static boolean enabled = false;
    
    public static synchronized void log(Throwable t, PfixServletRequest req) {
        if (LOGGER == null) {
            LOGGER = Logger.getLogger(LOGGER_NAME);
            enabled = LOGGER.isEnabledFor(Level.ERROR); 
        }
        if (enabled) {
            LOGGER.error(MessageBuilder.run(t, req));
        }
    }
    

    public static List run(Throwable t, PfixServletRequest req) {
        List result;
        HttpSession session;
        
        result = new ArrayList();
        session = req.getSession(false);
        add(result, "url", extractURL(req));
        add(result, "session", extractSession(session));
        add(result, "trail", extractTrail(session).toString());
        add(result, "exception", t.toString());
        addParameter(addNode(result, "parameter"), req);
        addData(addNode(result, "data"), session);
        return result;
    }

    private static void add(List dest, String name, String value) {
        dest.add(name);
        dest.add(value);
    }

    private static List addNode(List dest, String name) {
        List result;
        
        result = new ArrayList();
        dest.add(name);
        dest.add(result);
        return result;
    }

    //--
    
    public static void addData(List dest, HttpSession session) {
        Enumeration enm;
        Object o;
        String name;
        String value;
        
        enm = session.getAttributeNames();
        while (enm.hasMoreElements()) {
            name = (String) enm.nextElement();
            try {
                o = session.getAttribute(name);
                value = "[" + o.getClass().getName() + "]\n" + o.toString();
            } catch (Exception e) {
                value = "[exception: " + e.getMessage() + "]";
            }
            add(dest, name, value);
        }
    }
    
    public static void addParameter(List dest, PfixServletRequest req) {
        String[] names;
        String name;
        int i;
        
        names = req.getRequestParamNames();
        for (i = 0; i < names.length; i++) {
            name = names[i];
            add(dest, name, req.getRequestParam(name).toString());
        }
    }

    public static String extractSession(HttpSession session) {
        return session == null ? null : session.getId();
    }
    
    public static String extractURL(PfixServletRequest pfrequest_) {
        StringBuffer err = new StringBuffer();
        HttpSession session = pfrequest_.getSession(false);
        String server = null;
        String que = null;
        String uri = null;
        String scheme = null;
        int port = -1;
        server = pfrequest_.getServerName();
        if (server == null) {
            /* a relocate? let us use the pfixservletrequest.getOrginalXXX methods */
            server = pfrequest_.getOriginalServerName();
            que = pfrequest_.getOriginalQueryString();
            uri = pfrequest_.getOriginalRequestURI();
            scheme = pfrequest_.getOriginalScheme();
            port = pfrequest_.getOriginalServerPort();
        } else {
            /* no relocate? let us use the pfixservletrequest.getCurrentXXX methods */
            que = pfrequest_.getQueryString();
            uri = pfrequest_.getRequestURI();
            scheme = pfrequest_.getScheme();
            port = pfrequest_.getServerPort();
        }
        if (session != null) {
            err.append("[SessionId: " + session.getId() + "]\n");
        }
        return scheme + "://" + server + ":" + port + uri + (((que != null) && !que.equals("")) ? "?" + que : ""); 
    }

    public static List extractTrail(HttpSession session) {
        List result;
        List trail;
        
        result = new ArrayList();
        SessionInfoStruct info = SessionAdmin.getInstance().getInfo(session.getId());
        if (info != null) {
            trail = info.getTraillog();
            if (trail != null && trail.size() > 0) {
                for (Iterator j = trail.listIterator(); j.hasNext();) {
                    SessionInfoStruct.TrailElement step = (SessionInfoStruct.TrailElement) j.next();
                    result.add("[" + step.getCounter() + "] " + step.getStylesheetname() + " [" + step.getServletname() + "]");
                }
            }
        }
        return result;
    }
}
