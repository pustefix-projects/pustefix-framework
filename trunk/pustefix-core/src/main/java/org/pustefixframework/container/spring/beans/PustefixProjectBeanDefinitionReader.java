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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.pustefixframework.config.customization.CustomizationInfo;
import org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo;
import org.pustefixframework.config.project.ProjectInfo;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.marsching.flexiparse.objecttree.ObjectTreeElement;
import com.marsching.flexiparse.parser.ClasspathConfiguredParser;
import com.marsching.flexiparse.parser.Parser;
import com.marsching.flexiparse.parser.exception.ParserException;

import de.schlund.pfixxml.config.EnvironmentProperties;
import de.schlund.pfixxml.config.includes.IncludesResolver;
import de.schlund.pfixxml.resources.ModuleResource;

public class PustefixProjectBeanDefinitionReader extends AbstractBeanDefinitionReader {
    
    public PustefixProjectBeanDefinitionReader(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
        // TODO Use BeanFactory's class loader
        ObjectTreeElement projectConfigTree;
        Properties envProperties = EnvironmentProperties.getProperties();
        CustomizationInfo info = new PropertiesBasedCustomizationInfo(envProperties);
        try {
            Parser projectConfigParser = new ClasspathConfiguredParser("META-INF/org/pustefixframework/config/project/parser/project-config.xml");
            String definingModule = null;
            if(resource instanceof ModuleResource) {
                definingModule = resource.getURI().getAuthority();
            }
            ProjectInfo projectInfo = new ProjectInfo(definingModule);
            
            //Resolve config-includes
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setXIncludeAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(resource.getInputStream()); 
            IncludesResolver resolver = new IncludesResolver("http://www.pustefix-framework.org/2008/namespace/project-config", "config-include");
            resolver.resolveIncludes(doc);
            projectConfigTree = projectConfigParser.parse(doc, info, getRegistry(), projectInfo);
        } catch (ParserException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + resource + ": " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + resource + ": " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + resource + ": " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new BeanDefinitionStoreException("Error while parsing " + resource + ": " + e.getMessage(), e);
        }
        Collection<? extends BeanDefinitionHolder> beanDefinitions = projectConfigTree.getObjectsOfTypeFromSubTree(BeanDefinitionHolder.class);
        int count = 0;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            count++;
            String beanName = holder.getBeanName();
            this.getBeanFactory().registerBeanDefinition(beanName, holder.getBeanDefinition());
            String[] aliases = holder.getAliases();
            if (aliases != null) {
                for (int j = 0; j < aliases.length; j++) {
                    String alias = aliases[j];
                    this.getBeanFactory().registerAlias(beanName, alias);
                }
            }
        }
        return count;
    }

}
