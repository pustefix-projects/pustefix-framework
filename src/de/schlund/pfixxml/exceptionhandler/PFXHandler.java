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

import java.util.Enumeration;
import java.util.Timer;
import java.util.Vector;

import org.apache.log4j.Category;

import de.schlund.pfixcore.util.email.EmailSender;
import de.schlund.pfixcore.util.email.EmailSenderException;

/** 
 * This class handles all incoming exceptions. It decides if an exception is delivered directly or 
 * is queued for later delivery.  
 * <br/>
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 *
 */
class PFXHandler {

    //~ Instance/static variables ..............................................

    private ExceptionConfig[] econf_ = null;
    private boolean errorflag_ = false;
    private GeneralConfig gconf_ = null;
    private boolean initialised_ = false;
    private Vector instancecontainerlist_ = null;
    private PFUtil pfutil_ = null;
    private PropertyManager propman_ = null;
    private Timer timer_ = null;
    private static Category CAT = Category.getInstance(PFXHandler.class.getName());

    //~ Constructors ...........................................................

    /**
     * Creates a new PFXHandler object.
     */
    protected PFXHandler() {
        instancecontainerlist_ = new Vector();
        timer_ = new Timer();
        pfutil_ = PFUtil.getInstance();
        propman_ = PropertyManager.getInstance();
        pfutil_.debug("Creating new PFXHandler object");
    }

    //~ Methods ................................................................

    /**
     * All containing InstanceCheckerContainer are asked if they can handle the incoming
     * exception.
     * @param e The incoming exception.
     * @param req The request belonging with the exception.
     * @param properties The properties.
     */
    protected void xhandle(ExceptionContext excontext) {
        pfutil_.debug("handling exception...");
        int result = 0;
        Throwable th = excontext.getThrowable();

        if (errorflag_ == true) {
            pfutil_.error("exceptionhandler not configured! Logging to file only.");
            pfutil_.error(excontext.getMessage());
            return;
        }
        // everything to log file
        pfutil_.error(excontext.getMessage());
        if (propman_.isInitialised() && gconf_.isUseme()) {
            for (Enumeration enum = instancecontainerlist_.elements(); enum.hasMoreElements();) {
                InstanceCheckerContainer checker = (InstanceCheckerContainer) enum.nextElement();
                result = checker.doesMatch(th);
                pfutil_.debug("The InstanceCheckerContainers returned: " + pfutil_.strByResult(result));
                if (result == PFUtil.MATCH) {
                    ruleMatch(excontext);
                    break;
                }
                if (result == PFUtil.TRIGGER_MATCH) {
                    ruleTriggerMatch(excontext);
                    break;
                }
                if (result == PFUtil.FULL) {
                    ruleFull(excontext);
                    break;
                }
            }
            // this is default !
            if (result == PFUtil.NO_MATCH) {
                ruleNoMatch(excontext);
            }
        } else {
            // Something went wrong !!!
            ruleError(excontext);
        }
        pfutil_.debug("handling exception...done");
    }

    void setErrorFlag(boolean error) {
        errorflag_ = error;
    }

    /**
     * Read properties.
     * @param propfile a String containing the path to the properties file.
     * @return the modification time of the propertie file
     */
    void init() {
        STraceCleanupTask sctask = null;
        ReportGeneratorTask rgtask = null;
        long delay = 0l;
        long period = 0l;
        /* are we reinitializing? if so delete elements in instancecontainerlist and handle timer */
        if (initialised_) {
            pfutil_.debug("Reinitialising PFXHandler...");
            // Run a report generator before total cleanup and wait until its finished
            String info = "AUTO before reinitialisation";
            ReportGeneratorTask rg = new ReportGeneratorTask(instancecontainerlist_, info);
            Thread th = new Thread(rg);
            th.start();
            try {
                th.join();
            } catch (InterruptedException e) {
            }
            instancecontainerlist_.removeAllElements();
            timer_.cancel();
            timer_ = null;
            timer_ = new Timer();
        }
        gconf_ = propman_.getGeneralConfig();
        sctask = new STraceCleanupTask(instancecontainerlist_);
        rgtask = new ReportGeneratorTask(instancecontainerlist_, "AUTO");
        period = new Long(gconf_.getCleanupSchedule() * pfutil_.getRate(gconf_.getCleanupScheduledim())).longValue();
        timer_.schedule(sctask, delay, period);
        period = new Long(gconf_.getReportSchedule() * pfutil_.getRate(gconf_.getReportScheduledim())).longValue();
        timer_.schedule(rgtask, delay, period);
        ExceptionConfig[] econf = propman_.getExceptionConfig();
        for (int i = 0; i < econf.length; i++) {
            InstanceCheckerContainer ic = null;
            ic =
                new InstanceCheckerContainer(
                    econf[i].getType(),
                    econf[i].getMatch(),
                    econf[i].getLimit(),
                    econf[i].getLimitDimension(),
                    econf[i].getBurst(),
                    gconf_.getStraceObsolete(),
                    gconf_.getStraceObsoleteDim());
            instancecontainerlist_.add(ic);
        }
        propman_.doMailConfig();

        initialised_ = true;
    }

    /**
     * Utility method to handle internal errors.
     * @param message a String containing a message.
     */
    private void internalError(String message) {
        StringBuffer buf = new StringBuffer();
        buf.append("Message: ");
        buf.append(message + "\n");
        buf.append("Useme flag=" + new Boolean(gconf_.isUseme()).toString() + "\n");
        pfutil_.error("Internal Error during ExceptionHandling!!!\n" + buf.toString());
    }

    /**
     * Action to take if rules are not initialized properly or you do not use them.
     * @param e the current Exception.
     * @param req the current <see>PfixServletRequest</see>.
     * @param properties the current properties.
     */
    private void ruleError(ExceptionContext excontext) {
        String subject = excontext.getHeader();
        excontext.addComment("New ExceptionHandler is OFF due to an initialisation error or you do not want to use it!");
        String message = excontext.getMessage();
        try {
            MailConfig mailconfig = MailConfig.getInstance();
            EmailSender.sendMail(subject, message, mailconfig.getTo(), mailconfig.getFrom(), mailconfig.getHost());
        } catch (EmailSenderException e) {
            pfutil_.fatal("Sending of errormail failed!!! " + e.getMessage());
        }
    }

    /**
     * Action to take if resonsible rule sayed 'full'.
     * @param e the current Exception.
     * @param req the current <see>PfixServletRequest</see>.
     * @param properties the current properties.
     */
    private void ruleFull(ExceptionContext excontext) {
        //nop
    }

    /**
     * Action to take if responsible rule sayed 'match'.
     * @param e the current Exception.
     * @param req the current <see>PfixServletRequest</see>.
     * @param properties the current properties.
     */
    private void ruleMatch(ExceptionContext excontext) {
        String subject = excontext.getHeader();
        String message = excontext.getMessage();
        try {
            MailConfig mailconfig = MailConfig.getInstance();
            if (mailconfig.isSend()) {
                EmailSender.sendMail(subject, message, mailconfig.getTo(), mailconfig.getFrom(), mailconfig.getHost());
            } else {
                if (CAT.isDebugEnabled())
                    CAT.debug("MailSending is disabled");
            }
        } catch (EmailSenderException e) {
            pfutil_.fatal("Sending of errormail failed!!! " + e.getMessage());
        }
    }
    /**
     * Action to take if no responsible rule is found.
     * @param e the current Exception.
     * @param req the current <see>PfixServletRequest</see>.
     * @param properties the current properties.
     */
    private void ruleNoMatch(ExceptionContext excontext) {
        String subject = excontext.getHeader();
        excontext.addComment("No rule found to match this exception. Please check your configuration!");
        String message = excontext.getMessage();
        try {
            MailConfig mailconfig = MailConfig.getInstance();
            if(mailconfig.isSend()) {
                EmailSender.sendMail(subject, message, mailconfig.getTo(), mailconfig.getFrom(), mailconfig.getHost());
            } else {
                if(CAT.isDebugEnabled()) 
                    CAT.debug("MailSending is disabled");
            }
        } catch (EmailSenderException e) {
            pfutil_.fatal("Sending of errormail failed!!! " + e.getMessage());
        }
    }

    /**
     * Action to take if responsible rule sayed 'trigger_match'.
     * @param e the current Exception.
     * @param req the current <see>PfixServletRequest</see>.
     * @param properties the current properties.
     */
    private void ruleTriggerMatch(ExceptionContext excontext) {
        Throwable th = excontext.getThrowable();
        ReportGeneratorTask rgtask = null;
        pfutil_.debug(" ReportGeneratorTask triggert from " + th.toString() + " at " + excontext.getDate());
        // schedule the report generator task right now
        rgtask = new ReportGeneratorTask(instancecontainerlist_, "TRIGGER");
        timer_.schedule(rgtask, new Long(0).longValue());
        /* but its a match, could call ruleMatch, but i want exactly the same date */
        ruleMatch(excontext);
    }
} //PFXHandler
