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

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

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
    public void storeSPDocument(SPDocument spdoc, Map storeddoms, int timeoutsecs) {
        String key = spdoc.getTimestamp() + "";

        LOG.info("*** Create new TimerTask with timeout: " + timeoutsecs);
        TimerTask task = new SessionCleanerTask(storeddoms, key);
        timer.schedule(task, timeoutsecs * 1000);
        // Save the needed info
        storeddoms.put(key, spdoc);
    }

    private class SessionCleanerTask extends TimerTask {
        String key;
        Map    storeddoms;
        
        public SessionCleanerTask(Map storeddoms, String key) {
            this.storeddoms = storeddoms;
            this.key        = key;
        }

        public void run() {
            try {
                if (storeddoms.containsKey(key)) {
                    storeddoms.remove(key);
                    LOG.info("*** CALLING TIMERTASK: Removing SPDoc '" + key + 
                             "' in session from cache (Curr. Size: " + storeddoms.size() + ")");
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
} // SessionCleaner
