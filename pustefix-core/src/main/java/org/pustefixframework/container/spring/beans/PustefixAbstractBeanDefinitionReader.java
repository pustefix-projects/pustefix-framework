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

package org.pustefixframework.container.spring.beans;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.pustefixframework.config.application.ProjectInfo;
import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.customization.RuntimeProperties;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.osgi.context.ConfigurableOsgiBundleApplicationContext;
import org.xml.sax.EntityResolver;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.parser.ClasspathConfiguredParser;
import com.marsching.flexiparse.parser.Parser;
import com.marsching.flexiparse.parser.exception.ParserException;


/**
 * Base class for OSGi-aware Pustefix BeanDefinitionReaders.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public abstract class PustefixAbstractBeanDefinitionReader extends AbstractBeanDefinitionReader {
    
    private EntityResolver entityResolver;
    private NamespaceHandlerResolver namespaceHandlerResolver;
    protected ConfigurableOsgiBundleApplicationContext osgiBundleApplicationContext;

    /**
     * Constructor.
     * 
     * @param registry bean definiton registry that bean definitions, read by
     *  this reader, are registered with. 
     * @param applicationContext OSGi-aware application context corresponding
     *  to the bean definition registry. The application context might e.g. be
     *  used to retrieve the corresponding bundle context.
     */
    public PustefixAbstractBeanDefinitionReader(BeanDefinitionRegistry registry, ConfigurableOsgiBundleApplicationContext applicationContext) {
        super(registry);
        this.osgiBundleApplicationContext = applicationContext;
    }
    
    /**
     * Returns path to configuration file within bundle. This method
     * is used to initialize the configuration parser with different
     * configurations depending on the sub-type of this class.
     * 
     * @return Path to configuration parser's configuration
     */
    protected abstract String getParserConfigurationPath();
    
    /**
     * Returns the application context associated with the bean definition 
     * registry this reader is associated with.
     * 
     * @return OSGi-aware application context
     */
    protected ConfigurableOsgiBundleApplicationContext getApplicationContext() {
        return this.osgiBundleApplicationContext;
    }
    
    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        // TODO Use BeanFactory's class loader
        ObjectTreeElement applicationConfigTree;
        Properties buildTimeProperties = RuntimeProperties.getProperties();
        CustomizationInfo info = new PropertiesBasedCustomizationInfo(buildTimeProperties);
        try {
            // TODO Use OSGi-aware configuration parser
            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            try {
                Parser applicationConfigParser = new ClasspathConfiguredParser(getParserConfigurationPath());
                ProjectInfo projectInfo = new ProjectInfo(resource.getURL());
                applicationConfigTree = applicationConfigParser.parse(resource.getInputStream(), info, getRegistry(), projectInfo);
            } finally {
                Thread.currentThread().setContextClassLoader(ccl);
            }
        } catch (ParserException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + resource + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + resource + ": " + e.getMessage(), e);
        }
        Collection<? extends BeanDefinitionHolder> beanDefinitions = applicationConfigTree.getObjectsOfTypeFromSubTree(BeanDefinitionHolder.class);
        int count = 0;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            count++;
            String beanName = holder.getBeanName();
            this.getRegistry().registerBeanDefinition(beanName, holder.getBeanDefinition());
            String[] aliases = holder.getAliases();
            if (aliases != null) {
                for (int j = 0; j < aliases.length; j++) {
                    String alias = aliases[j];
                    this.getRegistry().registerAlias(beanName, alias);
                }
            }
        }
        // TODO: Make path to spring configuration configurable
        Resource springConfig = this.getResourceLoader().getResource("osgibundle:/META-INF/pustefix/spring.xml");
        if (springConfig.exists()) {
            XmlBeanDefinitionReader springReader = new XmlBeanDefinitionReader(this.getRegistry());
            springReader.setBeanClassLoader(getBeanClassLoader());
            springReader.setBeanNameGenerator(getBeanNameGenerator());
            springReader.setResourceLoader(getResourceLoader());
            springReader.setEntityResolver(getEntityResolver());
            springReader.setNamespaceHandlerResolver(getNamespaceHandlerResolver());
            count += springReader.loadBeanDefinitions(springConfig);
        }
        return count;
    }
    
    /**
     * Returns the entity resolver that is used by this reader when delegating
     * to a standard Spring {@link BeanDefinitionReader}.
     * 
     * @return entity resolver for this reader
     */
    protected EntityResolver getEntityResolver() {
        return this.entityResolver;
    }
    
    /**
     * Set entity resolver being used by this reader when delegating to a 
     * standard Spring {@link BeanDefinitionReader}.
     * 
     * @param entityResolver entity resolver that should be used
     */
    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    /**
     * Returns the namespace handler resolver that is used by this reader
     * when delegating to a standard Spring {@link BeanDefinitionReader}.
     * 
     * @return namespace handler resolver for this reader
     */
    protected NamespaceHandlerResolver getNamespaceHandlerResolver() {
        return this.namespaceHandlerResolver;
    }
    
    /**
     * Sets the namespace handler resolver that is used by this reader when 
     * delegating to a standard Spring {@link BeanDefinitionReader}.
     * 
     * @param namespaceHandlerResolver namespace handler resolver that should
     *  be used
     */
    public void setNamespaceHandlerResolver(NamespaceHandlerResolver namespaceHandlerResolver) {
        this.namespaceHandlerResolver = namespaceHandlerResolver;
    }

}
