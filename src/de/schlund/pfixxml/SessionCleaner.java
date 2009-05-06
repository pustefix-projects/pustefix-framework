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

package de.schlund.pfixxml;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;

/**
 * The <code>SessionCleaner</code> class is used to remove stored SPDocuments from the session
 * after a timout. This helps in reducing the memory usage as those documents
 * are only stored for possible reuse by following subrequests (for frames). After the timeout one should
 * be reasonable sure that no subrequests will follow (During development, the AbstractXMLServer
 * should make sure to call storeSPDocument() with the <code>timeoutsec</code> parameter set to
 * <b>a very high value</b>, to be able to get the stored SPDocument for debugging purposes).
 *
 * Created: Thu Mar 20 16:45:31 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version $Id$
 */
public class SessionCleaner {
    private static SessionCleaner instance     = new SessionCleaner();
    private        Timer          timer;
    private        String         TASK_POSTFIX = "__TIMER_TASK";
    Category       CAT          = Category.getInstance(this.getClass());
    
    private SessionCleaner() {}

    /**
     * @return The <code>SessionCleaner</code> singleton.
     */
    public static SessionCleaner getInstance() {
        return instance;
    }

    private synchronized Timer getTimer() {
        if(timer == null) timer = new Timer("Timer-SessionCleaner", true);
        return timer;
    }
    
    private synchronized void resetTimer(Timer oldTimer) {
        if(timer == oldTimer) {
            timer = null;
            CAT.warn("Reset timer.");
        }
    }
    
    /**
     * Called from the AbstractXMLServer to store a SPDocument into the supplied HttpSession.
     * This will also start a TimerTask that removes the stored SPDocument after the given timeout.
     *
     * @param spdoc a <code>SPDocument</code> value
     * @param session a <code>HttpSession</code> value
     * @param conutil a <code>ContainerUtil</code> value
     * @param key a <code>String</code> value. The key under which the SPDocument will be stored in the session.
     * @param timeoutsecs a <code>int</code> value. The timeout when the document should be removed.
     */
    public void storeSPDocument(SPDocument spdoc, HttpSession session,String key, int timeoutsecs) {
        String taskkey = key + TASK_POSTFIX; 

        synchronized (session) {
            SessionCleanerTask task   = (SessionCleanerTask)session.getAttribute(taskkey);
            if (task != null) {
                CAT.info("*** Found old TimerTask, trying to cancel... ");
                try {
                    task.cancel();
                    CAT.info("*** DONE. ***");
                } catch (IllegalStateException e) {
                    CAT.info("*** Could not cancel: " + e.getMessage() + " ***");
                }
            }
            CAT.info("*** Create new TimerTask with timeout: " + timeoutsecs);
            task = new SessionCleanerTask(session,key);
            Timer t = getTimer();
            try {
                t.schedule(task, timeoutsecs * 1000);
            } catch(IllegalStateException x) {
                resetTimer(t);
                getTimer().schedule(task, timeoutsecs * 1000);
            }
            session.setAttribute(taskkey, task);
            
            session.setAttribute(key, spdoc);
        }
    }

    private class SessionCleanerTask extends TimerTask {
        String        key;
        HttpSession   session;
        
        public SessionCleanerTask(HttpSession session,String key) {
            this.session = session;
            this.key     = key;
        }

        public void run() {
            try {
                CAT.info("*** CALLING TIMERTASK: Removing SPDoc '" + key + "' from session " + session.getId());
                synchronized (session) { session.setAttribute(key, null); }
            } catch (IllegalStateException e) {
                CAT.info("*** Couldn't remove from session... " + e.getMessage() + " ***");
            } catch (Exception x) {
                //Catch all exceptions, because throwing them will cancel the timer
                CAT.warn("Error while cleaning session", x);
            }
            session = null; // we don't want to hold any spurious references to the session that may prohibit it being gc'ed
            key     = null;
        }
    }
} // SessionCleaner
