/*
* This file is part of PFIXCORE.
*
* PFIXCORE is free software; you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* PFIXCORE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with PFIXCORE; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

package de.schlund.pfixxml.exceptionhandler;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.apache.log4j.spi.ThrowableInformation;

import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.serverutil.SessionAdmin;
import de.schlund.pfixxml.serverutil.SessionInfoStruct;

/**
 * Class to to encapsulate the data needed to handle a throwable. It holds
 * various information needed in different parts of the exceptionhandler.
 * 
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */
class ExceptionContext {

    //~ Instance/static variables ..............................................

    private final String COUNTRY_ = "DE";
    private final String LANGUAGE_ = "de";
    private final String TIMEZONE_ = "Europe/Berlin";
    private static Category CAT = Category.getInstance(ExceptionContext.class.getName());
    private String date_ = null; // timestamp
    private Throwable throwable_ = null; //
    private String header_ = null; // header for message
    private String message_ = null; // textual message
    private PfixServletRequest pfrequest_ = null; // request
    private Properties props_ = null; // properties
   
    //~ Constructors ...........................................................

    /**
     * Create a new exception context object with the given throwable, request
     * and properties objects.
     */
    ExceptionContext(Throwable th, PfixServletRequest pfreq, Properties props) {
        this.throwable_ = th;
        this.pfrequest_ = pfreq;
        this.props_ = props;
    }

    //~ Methods ................................................................

    /**
     * Returns the timestamp of this exceptioncontext. 
     * @return the timestamp.
     */
    String getDate() {
        return date_;
    }

    /**
     * Sets the throwable of this exceptioncontext.
     * @param exception the exception to set.
     */
    void setThrowable(Throwable th) {
        this.throwable_ = th;
    }

    /**
     * Returns the throwable of this exceptioncontext.
     * @return Exception the exception.
     */
    Throwable getThrowable() {
        return throwable_;
    }

    /**
     * Returns the message-header of this exceptioncontext.
     * @return String the message-header.
     */
    String getHeader() {
        return header_;
    }

    /**
     * Returns the message of this exceptioncontext.
     * @return String the message.
     */
    String getMessage() {
        return message_;
    }

    /**
     * Sets the request of this exceptioncontext.
     * @param pfrequest the request to set.
     */
    void setPfrequest(PfixServletRequest pfrequest) {
        this.pfrequest_ = pfrequest;
    }

    /**
     * Returns the request of this exceptioncontext.
     * @return PfixServletRequest the request.
     */
    PfixServletRequest getPfrequest() {
        return pfrequest_;
    }

    /**
     * Returns the properties of this exceptioncontext.
     * @return Properties the properties.
     */
    Properties getProperties() {
        return props_;
    }

    /**
     * Sets the properties of this exceptioncontext.
     * @param props_ the properties to set.
     */
    void setProps(Properties props) {
        this.props_ = props;
    }

    /**
     * Add a comment to the message.
     * @param comment the comment.
     */
    void addComment(String comment) {
        message_ = comment + "\n" + message_;
    }

    /**
     *  Initialise the exceptioncontext. A timestamp, the message and its header are created. 
     */
    void init() {
        if(CAT.isDebugEnabled())
            CAT.debug("ExceptionContext init start.");
        TimeZone tz = TimeZone.getTimeZone(TIMEZONE_);
        Locale loc = new Locale(LANGUAGE_, COUNTRY_);
        Calendar cal = Calendar.getInstance(tz, loc);
        SimpleDateFormat df = new SimpleDateFormat("H:mm:ss");
        date_ = df.format(cal.getTime());
        createHeader();
        createMessage();
        if(CAT.isDebugEnabled())
            CAT.debug("ExceptionContext init end.");
    }

    /**
     * Creates textual information from internal fields.
     * @return a String containing text.
     */
    private String createErrorText() {
        if(CAT.isDebugEnabled())
            CAT.debug("Create error text start.");
        StringBuffer err = new StringBuffer();
        HttpSession session = pfrequest_.getSession(false);
        err.append(createInfoText());
        if (session != null) {
            StringBuffer sb = createLastSteps();
            err.append(sb);
            if (props_.getProperty("servlet.sessiondumponerror", "").equals("true")) {
                err.append(createSessionDump());
            }
            err.append("======================================================\n");
        }
        err.append(createSTraceText());
        if(CAT.isDebugEnabled())
            CAT.debug("Create error text end.");
        return err.toString();
    }

    /**
     * Create a header for the error-message, which is used as
     * mail-subject.
     */
    private void createHeader() {
        if(CAT.isDebugEnabled())
            CAT.debug("Create header start.");
        header_ = createMailSubject();
        if(CAT.isDebugEnabled())
            CAT.debug("Create header end.");
    }

    /**
     * Create info text with all parameters.
     * 
     * @return a StringBuffer containing text. 
     */
    private String createInfoText() {
        if(CAT.isDebugEnabled())
            CAT.debug("Create info text start.");
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
        String[] pnames = pfrequest_.getRequestParamNames();
        if (session != null) {
            err.append("[SessionId: " + session.getId() + "]\n");
        }
        err.append(scheme + "://" + server + ":" + port + uri);
        if ((que != null) && (que != "")) {
            err.append("?" + que);
        }
        err.append("\n");
        err.append("\n\nParameter: \n");
        if (pnames.length == 0) {
            err.append(" " + "None" + "\n");
        }
        for (int ii = 0; ii < pnames.length; ii++) {
            RequestParam param = pfrequest_.getRequestParam(pnames[ii]);
            err.append(" " + pnames[ii] + " = " + param.toString() + "\n");
        }
        err.append("\n");
        if(CAT.isDebugEnabled())
            CAT.debug("Create info text start.");
        return err.toString();
    }

    /**
     * Create a last step information.
     * 
     * @return a StringBuffer containing text. 
     */
    private StringBuffer createLastSteps() {
        if(CAT.isDebugEnabled())
            CAT.debug("Creating last step information start.");
        HttpSession session = pfrequest_.getSession(false);
        StringBuffer err = new StringBuffer();
        SessionAdmin sessadmin = SessionAdmin.getInstance();
        SessionInfoStruct info = sessadmin.getInfo(session.getId());
        LinkedList trail = info.getTraillog();
        if (trail != null && trail.size() > 0) {
            err.append("\n==== Last steps before error occured: ================\n");
            for (Iterator j = trail.listIterator(); j.hasNext();) {
                SessionInfoStruct.TrailElement step = (SessionInfoStruct.TrailElement) j.next();
                err.append(
                    "[" + step.getCounter() + "] " + step.getStylesheetname() + " [" + step.getServletname() + "]\n");
            }
        }
        if(CAT.isDebugEnabled()) 
            CAT.debug("Create last step information end.");
            
        return err;
    }

    /**
     * Create subject line to be sent in a mail.
     * 
     * @return a String containg text.
     */
    private String createMailSubject() {
        if(CAT.isDebugEnabled())
            CAT.debug("Create mail subject start.");
        String buf = null;
        String servername = pfrequest_.getServerName();
        if (servername == null) {
            /* a relocate? let us use the pfixservletrequest.getOrginalXXX methods */
            servername = pfrequest_.getOriginalServerName();
        }
        String exceptname = throwable_.getClass().getName();
        String message = throwable_.getMessage();
        if (message == null) {
            ThrowableInformation info = new ThrowableInformation(throwable_);
            String[] strace = info.getThrowableStrRep();
            
            if(strace.length > 1) {
                message = strace[1].trim();
            } else if (strace.length == 1){
                // This case can happen when handling a OutofMemoryError, where
                // the stracktrace has a length of only 1.
                message = strace[0].trim();
            } else {
                // what's this?
                message = "No information found.";
            }
        }
        String servletname = pfrequest_.getServletPath();
        if(servletname == null) {
            servletname = "NULL";
        } else if(servletname.charAt(0) == '/') {
            servletname = servletname.substring(1, servletname.length());
        }
        Object[] args = { servername, servletname, exceptname, message };
        buf = MessageFormat.format("{0}|{1}|{2}:{3}", args);
        if(CAT.isDebugEnabled())
            CAT.debug("Create mail subject end.");
        return buf;
    }

    //________PRIVATE_____________

    /**
     * DOCUMENT ME!
     */
    private void createMessage() {
        message_ = createErrorText();
    }

    /**
     * Create text containing a stack trace.
     * 
     * @return a Stringbuffer containing text. 
     */
    private StringBuffer createSTraceText() {
        StringBuffer err = new StringBuffer();
        ThrowableInformation info = new ThrowableInformation(throwable_);
        String[] strace = info.getThrowableStrRep();
        for (int i = 0; i < strace.length; i++) {
            err.append(strace[i].trim() + "\n");
        }
        return err;
    }

    /**
     * Create a session dump.
     * 
     * @return a StringBuffer containing text.
     */
    private StringBuffer createSessionDump() {
        HttpSession session = pfrequest_.getSession(false);
        StringBuffer err = new StringBuffer();
        err.append("\n==== Session keys and values: ========================\n");
        String[] snames = session.getValueNames();
        for (int i = 0; i < snames.length; i++) {
            err.append("Sessionkey: " + snames[i]);
            Object o = session.getValue(snames[i]);
            err.append(" [" + o.getClass().getName() + "]\n");
            err.append("Value:      " + o.toString());
            err.append("\n");
            if (i < (snames.length - 1))
                err.append("------------------------------------------------------\n");
        }
        return err;
    }
}