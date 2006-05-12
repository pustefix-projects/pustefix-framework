package de.schlund.pfixxml.exceptionhandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.schlund.pfixcore.workflow.PageRequest;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;

/**
 * Creates an xml messsage and logs it. Note that log4j does not serialize 
 * objects passed as messages, it renders them to strings ...
 * 
 * Note that we don't share code with the actual Message class because
 * 1) building via strings is more efficient
 * 2) code modifications are more expensive 
 */
public class MessageBuilder {
    private static final String LOGGER_NAME = "de.schlund.pfixxml.MESSAGES";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

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

    public static String run(Throwable t, PfixServletRequest req) {
        MessageBuilder result;
        
        result = new MessageBuilder();
        result.startSection("message");
        result.add("date", DATE_FORMAT.format(new Date()));
        result.add("message", t.getMessage());
        result.add("exception", t.toString());
        request(result, req);
        session(result, req.getSession(false));
        result.endSection();
        return result.dest.toString();
    }

    private static void request(MessageBuilder result, PfixServletRequest req) {
        String servername;
        String servletname;
        PageRequest pr;
        
        result.startSection("request");
        result.add("remoteAddr", req.getRemoteAddr());
        result.add("serverName", req.getOriginalServerName());
        result.add("servletPath", req.getServletPath());
        result.add("page", req.getPageName());
        result.add("url", extractURL(req));
        result.addParameter(req);
        result.endSection();
    }
    
    private static void session(MessageBuilder result, HttpSession session) {
        if (session == null) {
            return;
        }
        result.startSection("session");
        result.add("id", session.getId());
        result.add("trail", extractTrail(session).toString());
        result.addData(session);
        result.endSection();
    }

    //--
    
    private final StringBuffer dest;
    
    public MessageBuilder() {
        dest = new StringBuffer();
    }
    
    public void add(String name, String value) {
        dest.append("<field");
        dest.append(" name='");
        escapeEntities(name, dest);
        dest.append("' value='");
        if (value == null) {
            dest.append("(null)");
        } else {
            escapeEntities(value, dest);
        }
        dest.append("'/>");
    }

    public void startSection(String name) {
        dest.append("<section name='");
        escapeEntities(name, dest);
        dest.append("'>"); 
    }
    
    public void endSection() {
        dest.append("</section>");
    }

    //--
    
    public void addData(HttpSession session) {
        Enumeration enm;
        Object o;
        String name;
        String value;

        startSection("data");
        enm = session.getAttributeNames();
        while (enm.hasMoreElements()) {
            name = (String) enm.nextElement();
            try {
                o = session.getAttribute(name);
                value = "[" + o.getClass().getName() + "]\n" + o.toString();
            } catch (Exception e) {
                value = "[exception: " + e.getMessage() + "]";
            }
            add(name, value);
        }
        endSection();
    }
    
    public void addParameter(PfixServletRequest req) {
        String[] names;
        String name;
        int i;
        
        startSection("parameter");
        names = req.getRequestParamNames();
        for (i = 0; i < names.length; i++) {
            name = names[i];
            add(name, req.getRequestParam(name).toString());
        }
        endSection();
    }

    //--
    
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
    
    //--
    
    // same method used for attributes and elements ...
    public static void escapeEntities(String str, StringBuffer dest) {
        char ch;
        String entity;

        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i);
            switch(ch) {
                case '<' :
                    entity = "&lt;";
                    break;
                case '>' :
                    entity = "&gt;";
                    break;
                case '\'' :
                    entity = "&apos;";
                    break;
                case '\"' :
                    entity = "&quot;";
                    break;
                case '&' :
                    entity = "&amp;";
                    break;
                default :
                    entity = null;
                    break;
            }
            if (entity == null) {
                dest.append(ch);
            } else {
                dest.append(entity);
            }
        }
    }
}
