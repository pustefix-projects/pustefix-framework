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
package org.pustefixframework.util.i18n;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;

/**
 * {@link ReloadableResourceBundleMessageSource} implementation supporting the
 * processing of messages using a {@link MessageSourcePreProcessor}.
 */
public class PreProcessableMessageSource extends ReloadableResourceBundleMessageSource {

    private MessageSourcePreProcessor processor;

    public void setMessagePreProcessor(MessageSourcePreProcessor processor) {
        this.processor = processor;
    }

    @Override
    protected Properties loadProperties(Resource arg0, String arg1) throws IOException {
        Properties props = super.loadProperties(arg0, arg1);
        if(processor != null) {
            Enumeration<?> names = props.propertyNames();
            while(names.hasMoreElements()) {
                String name = (String)names.nextElement();
                String value = props.getProperty(name);
                props.setProperty(name, processor.process(value));
            }
        }
        return props;
    }

}
