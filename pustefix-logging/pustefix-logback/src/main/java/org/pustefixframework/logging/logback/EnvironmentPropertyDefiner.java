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
package org.pustefixframework.logging.logback;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.util.OptionHelper;
import de.schlund.pfixxml.config.EnvironmentProperties;

/**
 * Makes Pustefix environment properties available as Logback context variables
 * by implementing a Logback PropertyDefiner.
 *
 * <p>
 * Note that in order to work correctly, the Pustefix environment properties have to be initialized 
 * before Logback is initialized, i.e. within a servlet container the PustefixServletContainerInitializer 
 * has to be called before the LogbackServletContainerInitializer, standalone, the EnvironmentProperties
 * have to be set up before Logback gets initialized.
 * </p>
 *
 * <h3>Example usage</h3>
 *
 * <pre>
 * {@code
 * <define name="logroot" class="org.pustefixframework.logging.logback.EnvironmentPropertyDefiner">
 *   <key>logroot</key>
 * </define>
 * <define name="mode" class="org.pustefixframework.logging.logback.EnvironmentPropertyDefiner">
 *   <key>mode</key>
 * </define>
 * }
 *
 * &lt;appender name="EXAMPLE" class="ch.qos.logback.core.rolling.RollingFileAppender"&gt;
 *   &lt;file&gt;${logroot}/example.log&lt;/file&gt;
 *   ...
 * &lt;/appender&gt;
 * </pre>
 */
public class EnvironmentPropertyDefiner extends PropertyDefinerBase {

    private String key;

    @Override
    public String getPropertyValue() {
        if (OptionHelper.isEmpty(key)) {
            addError("The 'key' property must be set.");
            return null;
        }
        String value = EnvironmentProperties.getProperties().getProperty(key);
        if (OptionHelper.isEmpty(value)) {
            addError("Environment property '" + key + "' is not set");
        }
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
