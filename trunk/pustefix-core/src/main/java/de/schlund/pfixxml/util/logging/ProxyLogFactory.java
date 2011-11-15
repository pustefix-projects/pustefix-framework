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

import java.lang.reflect.Constructor;

import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.logging.impl.LogFactoryImpl;

/**
 * Commons-Logging LogFactory returning the {@link de.schlund.pfixxml.util.logging.ProxyLogObject} 
 * logger or {@link org.apache.commons.logging.impl.Log4JLogger}.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ProxyLogFactory extends LogFactoryImpl {

    @SuppressWarnings("rawtypes")
    @Override
    protected Constructor getLogConstructor() throws LogConfigurationException {
        try {
            if (ProxyLogUtil.getInstance().isConfiguredForCl()) {
                // Commons-logging is available in the container
                // so we want to use it
                return ProxyLogObject.class.getConstructor(new Class[] {String.class});
            } else {
                // Commons-logging is not available, so use log4j
                // ignoring how it is configured
                return Log4JLogger.class.getConstructor(new Class[] {String.class});
            }
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
