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
 * This class is based on the JndiLookupBeanDefinitionParser and 
 * AbstractJndiLocatingBeanDefinitionParser classes from the Spring framework
 * distribution, copyright 2002-2007 the original author or authors.
 */

package org.pustefixframework.util.jndi.config;

import org.pustefixframework.util.jndi.JndiTemplate;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Simple {@link org.springframework.beans.factory.xml.BeanDefinitionParser} 
 * implementation that translates <code>jndi-lookup</code> tag into 
 * {@link JndiObjectFactoryBean} definitions, setting an instance of 
 * {@link JndiTemplate} as the <code>jndiTemplate</code> used by the factory 
 * bean.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class JndiLookupBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String ENVIRONMENT = "environment";

    public static final String ENVIRONMENT_REF = "environment-ref";

    public static final String JNDI_ENVIRONMENT = "jndiEnvironment";

    public static final String DEFAULT_VALUE = "default-value";

    public static final String DEFAULT_REF = "default-ref";

    public static final String DEFAULT_OBJECT = "defaultObject";

    public static final String JNDI_TEMPLATE = "jndiTemplate";

    public static final String JNDI_TEMPLATE_ATTR = "jndi-template";

    protected boolean isEligibleAttribute(String attributeName) {
        return (super.isEligibleAttribute(attributeName) && !ENVIRONMENT_REF.equals(attributeName) && !DEFAULT_VALUE.equals(attributeName) && !DEFAULT_REF.equals(attributeName));
    }

    protected void postProcess(BeanDefinitionBuilder definitionBuilder, Element element) {
        Object envValue = DomUtils.getChildElementValueByTagName(element, ENVIRONMENT);
        if (envValue != null) {
            // Specific environment settings defined, overriding any shared properties.
            definitionBuilder.addPropertyValue(JNDI_ENVIRONMENT, envValue);
        } else {
            // Check whether there is a reference to shared environment properties...
            String envRef = element.getAttribute(ENVIRONMENT_REF);
            if (StringUtils.hasLength(envRef)) {
                definitionBuilder.addPropertyValue(JNDI_ENVIRONMENT, new RuntimeBeanReference(envRef));
            }
        }

        // Set jndiTemplate
        if (!StringUtils.hasLength(element.getAttribute(JNDI_TEMPLATE_ATTR))) {
            definitionBuilder.addPropertyValue(JNDI_TEMPLATE, new JndiTemplate());
        }
    }

    protected Class<?> getBeanClass(Element element) {
        return JndiObjectFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String defaultValue = element.getAttribute(DEFAULT_VALUE);
        String defaultRef = element.getAttribute(DEFAULT_REF);
        if (StringUtils.hasLength(defaultValue)) {
            if (StringUtils.hasLength(defaultRef)) {
                parserContext.getReaderContext().error("<jndi-lookup> element is only allowed to contain either " + "'default-value' attribute OR 'default-ref' attribute, not both", element);
            }
            builder.addPropertyValue(DEFAULT_OBJECT, defaultValue);
        } else if (StringUtils.hasLength(defaultRef)) {
            builder.addPropertyValue(DEFAULT_OBJECT, new RuntimeBeanReference(defaultRef));
        }
    }

}
