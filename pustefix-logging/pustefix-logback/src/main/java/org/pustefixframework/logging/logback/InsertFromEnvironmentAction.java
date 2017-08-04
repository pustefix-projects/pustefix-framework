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

import org.xml.sax.Attributes;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.action.ActionUtil;
import ch.qos.logback.core.joran.action.ActionUtil.Scope;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.util.OptionHelper;
import de.schlund.pfixxml.config.EnvironmentProperties;

/**
 * Makes Pustefix environment properties available as Logback context variables
 * by implementing a Logback Action.
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
 * &lt;newRule pattern="&#42;/insertFromEnvironment"
 *          actionClass="org.pustefixframework.logging.logback.InsertFromEnvironmentAction"/&gt;
 *
 * &lt;insertFromEnvironment name="logroot"/&gt;
 * &lt;insertFromEnvironment name="mode"/&gt;
 * &lt;!--&lt;insertFromEnvironment name="mode" as="mymode" scope="local"/&gt;--&gt;
 *
 * &lt;appender name="EXAMPLE" class="ch.qos.logback.core.rolling.RollingFileAppender"&gt;
 *   &lt;file&gt;${logroot}/example.log&lt;/file&gt;
 *   ...
 * &lt;/appender&gt;
 * </pre>
 */
public class InsertFromEnvironmentAction extends Action {

    @Override
    public void begin(InterpretationContext ec, String name, Attributes attributes) throws ActionException {

        String propName = ec.subst(attributes.getValue("name"));
        String asKey = ec.subst(attributes.getValue("as"));
        Scope scope = ActionUtil.stringToScope(attributes.getValue(SCOPE_ATTRIBUTE));

        if (OptionHelper.isEmpty(propName)) {
            addError("Attribute 'name' missing at " + getLineColStr(ec));
        } else {
            if (OptionHelper.isEmpty(asKey)) {
                asKey = propName;
            }
            String propVal = EnvironmentProperties.getProperties().getProperty(propName);
            if (OptionHelper.isEmpty(propVal)) {
                addError("Environment property '" + propName + "' is not set");
            } else {
                ActionUtil.setProperty(ec, asKey, propVal, scope);
            }
        }
    }

    @Override
    public void end(InterpretationContext ec, String name) throws ActionException {
    }

}
