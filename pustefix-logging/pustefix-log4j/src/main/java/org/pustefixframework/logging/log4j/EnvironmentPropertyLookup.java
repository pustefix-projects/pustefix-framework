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
package org.pustefixframework.logging.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

import de.schlund.pfixxml.config.EnvironmentProperties;


/**
 * Makes Pustefix environment properties available as Log4j2 configuration
 * properties by implementing a Log4j Lookup plugin.
 *
 * <h3>Example usage</h3>
 *
 * <pre>
 * &lt;Configuration packages="org.pustefixframework.logging.log4j"&gt;
 *   &lt;Appenders&gt;
 *     &lt;File name="EXAMPLE" fileName="${pfx:logroot}/example.log"&gt;
 *     ...
 *     &lt;/File&gt;
 *   &lt;/Appenders&gt;
 * &lt;/Configuration&gt;
 * </pre>
 *
 */
@Plugin(name = "pfx", category = StrLookup.CATEGORY)
public class EnvironmentPropertyLookup implements StrLookup {

    @Override
    public String lookup(LogEvent event, String key) {
        return EnvironmentProperties.getProperties().getProperty(key);
    }

    @Override
    public String lookup(String key) {
        return lookup(null, key);
    }

}
