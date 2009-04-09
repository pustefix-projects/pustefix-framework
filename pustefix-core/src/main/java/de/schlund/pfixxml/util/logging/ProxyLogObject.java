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

package de.schlund.pfixxml.util.logging;

import org.apache.commons.logging.Log;

import de.schlund.pfixxml.util.logging.ProxyLogUtil.Level;

/**
 * Logger for commons-logging that logs through ProxyLogUtil.
 * Created by {@link de.schlund.pfixxml.util.logging.ProxyLogFactory}
 * 
 * @see de.schlund.pfixxml.util.logging.ProxyLogFactory
 * @see de.schlund.pfixxml.util.logging.ProxyLogUtil
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProxyLogObject implements Log {

    private String name;
    private ProxyLogUtil factory = ProxyLogUtil.getInstance();

    public ProxyLogObject (String name) {
        this.name = name;
    }

    public boolean isDebugEnabled() {
        return factory.doCheckEnabledCl(name, Level.DEBUG);
    }

    public boolean isErrorEnabled() {
        return factory.doCheckEnabledCl(name, Level.ERROR);
    }

    public boolean isFatalEnabled() {
        return factory.doCheckEnabledCl(name, Level.FATAL);
    }

    public boolean isInfoEnabled() {
        return factory.doCheckEnabledCl(name, Level.INFO);
    }

    public boolean isTraceEnabled() {
        return factory.doCheckEnabledCl(name, Level.TRACE);
    }

    public boolean isWarnEnabled() {
        return factory.doCheckEnabledCl(name, Level.WARN);
    }

    public void trace(Object msg) {
        factory.doLogCl(name, Level.TRACE, msg);
    }

    public void trace(Object msg, Throwable e) {
        factory.doLogCl(name, Level.TRACE, msg, e);
    }

    public void debug(Object msg) {
        factory.doLogCl(name, Level.DEBUG, msg);
    }

    public void debug(Object msg, Throwable e) {
        factory.doLogCl(name, Level.DEBUG, msg, e);
    }

    public void info(Object msg) {
        factory.doLogCl(name, Level.INFO, msg);
    }

    public void info(Object msg, Throwable e) {
        factory.doLogCl(name, Level.INFO, msg, e);
    }

    public void warn(Object msg) {
        factory.doLogCl(name, Level.WARN, msg);
    }

    public void warn(Object msg, Throwable e) {
        factory.doLogCl(name, Level.WARN, msg, e);
    }

    public void error(Object msg) {
        factory.doLogCl(name, Level.ERROR, msg);
    }

    public void error(Object msg, Throwable e) {
        factory.doLogCl(name, Level.ERROR, msg, e);
    }

    public void fatal(Object msg) {
        factory.doLogCl(name, Level.FATAL, msg);
    }

    public void fatal(Object msg, Throwable e) {
        factory.doLogCl(name, Level.FATAL, msg, e);
    }
}