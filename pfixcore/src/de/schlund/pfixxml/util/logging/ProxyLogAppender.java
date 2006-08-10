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
 */

package de.schlund.pfixxml.util.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Special log4j appender, that passes the logging event to the
 * {@link de.schlund.pfixxml.util.logging.ProxyLogUtil} class.
 * This appender should only if the webapplication's instance
 * of log4j cannot log itself (e.g. because the application is in
 * a packed WAR file}.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProxyLogAppender extends AppenderSkeleton {
    private ProxyLogUtil util = ProxyLogUtil.getInstance();

    protected void append(LoggingEvent event) {
        String name = event.getLoggerName();
        Level lvl = event.getLevel();
        Throwable ex = null;
        if (event.getThrowableInformation() != null) {
            ex = event.getThrowableInformation().getThrowable();
        }
        Object msg = event.getMessage();
        
        if (lvl.isGreaterOrEqual(Level.FATAL)) {
            if (ex == null)
                util.doLogLog4j(name, ProxyLogUtil.Level.FATAL, msg);
            else
                util.doLogLog4j(name, ProxyLogUtil.Level.FATAL, msg, ex);
        } else if (lvl.isGreaterOrEqual(Level.ERROR)) {
            if (ex == null)
                util.doLogLog4j(name, ProxyLogUtil.Level.ERROR, msg);
            else
                util.doLogLog4j(name, ProxyLogUtil.Level.ERROR, msg, ex);
        } else if (lvl.isGreaterOrEqual(Level.WARN)) {
            if (ex == null)
                util.doLogLog4j(name, ProxyLogUtil.Level.WARN, msg);
            else
                util.doLogLog4j(name, ProxyLogUtil.Level.WARN, msg, ex);
        } else if (lvl.isGreaterOrEqual(Level.INFO)) {
            if (ex == null)
                util.doLogLog4j(name, ProxyLogUtil.Level.INFO, msg);
            else
                util.doLogLog4j(name, ProxyLogUtil.Level.INFO, msg, ex);
        } else if (lvl.isGreaterOrEqual(Level.DEBUG)) {
            if (ex == null)
                util.doLogLog4j(name, ProxyLogUtil.Level.DEBUG, msg);
            else
                util.doLogLog4j(name, ProxyLogUtil.Level.DEBUG, msg, ex);
        } else {
            if (ex == null)
                util.doLogLog4j(name, ProxyLogUtil.Level.TRACE, msg);
            else
                util.doLogLog4j(name, ProxyLogUtil.Level.TRACE, msg, ex);
        }
    }

    public boolean requiresLayout() {
        return false;
    }

    public void close() {
        // Intentionally left blank
    }

}
