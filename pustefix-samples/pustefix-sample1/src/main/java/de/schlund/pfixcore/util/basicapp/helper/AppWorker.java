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

package de.schlund.pfixcore.util.basicapp.helper;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Basic work like e.g. initializing log4j will happen
 * here
 * 
 * @author <a href="mailto:rapude@schlund.de">Ralf Rapude</a>
 * @version $Id$
 */

public final class AppWorker {

    private static final Logger LOG = Logger.getLogger(AppWorker.class);

    /** Configuring log4j */
    public static void initLogging() {
        // Use basic logging without any configuration file
        String pattern = "%-5  %c: %m%n";
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(
                pattern), "System.err"));
        LOG.debug("Log4j has been sussessfully initialized");
    }
}
