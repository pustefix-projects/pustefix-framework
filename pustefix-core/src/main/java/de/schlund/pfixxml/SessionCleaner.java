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

package de.schlund.pfixxml;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.schlund.pfixxml.util.CacheValueLRU;

/**
 * The <code>SessionCleaner</code> class is used to remove stored SPDocuments from the session
 * after a timout. This helps in reducing the memory usage as those documents
 * are only stored for possible reuse by following subrequests (for frames). After the timeout one should
 * be reasonable sure that no subrequests will follow (During development, the AbstractXMLServlet
 * should make sure to call storeSPDocument() with the <code>timeoutsec</code> parameter set to
 * <b>a very high value</b>, to be able to get the stored SPDocument for debugging purposes).
 *
 * Created: Thu Mar 20 16:45:31 2003
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version $Id$
 */
public class SessionCleaner {
    private static SessionCleaner instance = new SessionCleaner();
    private        Timer          timer    = new Timer(true);
    private final static Logger   LOG      = Logger.getLogger(SessionCleaner.class);
    
    private SessionCleaner() {}

    /**
     * @return The <code>SessionCleaner</code> singleton.
     */
    public static SessionCleaner getInstance() {
        return instance;
    }

    /**
     * Called from the AbstractXMLServlet to store a SPDocument into the supplied SPCache structure
     * (which in turn is stored in the HTTPSession).  This will also start a TimerTask that removes
     * the stored SPDocument after the given timeout.
     *
     * @param spdoc a <code>SPDocument</code> value
     * @param storeddoms a <code>Map</code> value
     * @param timeoutsecs a <code>int</code> value. The timeout when the document should be removed.
     */
    public void storeSPDocument(SPDocument spdoc, CacheValueLRU<String,SPDocument> storeddoms, int timeoutsecs) {
        storeSPDocument(spdoc, null, storeddoms, timeoutsecs);
    }
    
    /**
     * Called from the AbstractXMLServlet to store a SPDocument into the supplied SPCache structure
     * (which in turn is stored in the HTTPSession).  This will also start a TimerTask that removes
     * the stored SPDocument after the given timeout.
     *
     * @param spdoc a <code>SPDocument</code> value
     * @param storeddoms a <code>Map</code> value
     * @param timeoutsecs a <code>int</code> value. The timeout when the document should be removed.
     */
    public void storeSPDocument(SPDocument spdoc, String frameName, CacheValueLRU<String,SPDocument> storeddoms, int timeoutsecs) {
        String key = spdoc.getTimestamp() + "";
        if (frameName != null) {
            key = key + "." + frameName;
        }

        // Save the needed info
        if (LOG.isDebugEnabled()) {
            LOG.debug("Store SPDocument " + spdoc.getTimestamp() + " under key " + key);
        }
        storeddoms.put(key, spdoc);
        LOG.info("*** Create new TimerTask with timeout: " + timeoutsecs);
        TimerTask task = new SessionCleanerTask(storeddoms, key);
        timer.schedule(task, timeoutsecs * 1000);

    }

    private class SessionCleanerTask extends TimerTask {
        String key;
        CacheValueLRU<String,SPDocument> storeddoms;
        
        public SessionCleanerTask(CacheValueLRU<String,SPDocument> storeddomcache, String key) {
            this.storeddoms = storeddomcache;
            this.key        = key;
        }

        @Override
        public void run() {
            try {
                if (storeddoms.containsKey(key)) {
                    storeddoms.remove(key);
                    LOG.info("*** CALLING TIMERTASK: Removing SPDoc Reference for '" + key + 
                             "' in session from cache (Curr. Size (= All keys counted!): " + storeddoms.size() + ")");
                } else {
                    LOG.info("*** CALLING TIMERTASK: nothing to do.");
                }

            } catch (IllegalStateException e) {
                LOG.warn("*** Couldn't remove from cache... " + e.getMessage() + " ***");
            }
            key        = null;
            storeddoms = null;
        }
    }
    
    private class SessionInvalidateTask extends TimerTask {
        HttpSession session;
        
        private SessionInvalidateTask(HttpSession session) {
            this.session = session;
        }

        @Override
        public void run() {
            try {
                this.session.invalidate();
            } catch (IllegalStateException e) {
                // Ignore IllegalStateException
            }
        }
    }

    public void invalidateSession(HttpSession session, int timeoutsecs) {
        timer.schedule(new SessionInvalidateTask(session), timeoutsecs * 1000);
    }
    
} // SessionCleaner
