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
package org.pustefixframework.cdi;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Adapter to Spring BeanFactory used to look up concrete
 * object instances of Spring beans injected via CDI.
 * 
 * @author mleidig@schlund.de
 *
 */
public class BeanFactoryAdapter {
    
    private ConfigurableBeanFactory beanFactory;
    
    public Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }
    
    public void destroyBean(String beanName, Object beanInstance) {
        beanFactory.destroyBean(beanName, beanInstance);
    }
    
    public void setBeanFactory(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
        
}
