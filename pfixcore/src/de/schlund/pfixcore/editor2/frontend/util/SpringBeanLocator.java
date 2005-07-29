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
 */

package de.schlund.pfixcore.editor2.frontend.util;

import org.springframework.context.ApplicationContext;

import de.schlund.pfixcore.editor2.core.spring.PageFactoryService;
import de.schlund.pfixcore.editor2.core.spring.ProjectFactoryService;
import de.schlund.pfixcore.editor2.core.spring.SecurityManagerService;
import de.schlund.pfixcore.editor2.core.spring.UserManagementService;

public abstract class SpringBeanLocator {
    private static ApplicationContext getApplicationContext() {
        return EditorApplicationContextFactory.getInstance()
                .getApplicationContext();
    }

    /**
     * Returns reference to UserManagementService
     * 
     * @return Bean retrieved from Spring ApplicationContext
     */
    public static UserManagementService getUserManagementService() {
        return (UserManagementService) getApplicationContext().getBean(
                "usermanagement");
    }

    /**
     * Returns reference to ProjectFactoryService
     * 
     * @return Bean retrieved from Spring ApplicationContext
     */
    public static ProjectFactoryService getProjectFactoryService() {
        return (ProjectFactoryService) getApplicationContext().getBean(
                "projectfactory");
    }

    /**
     * Returns reference to SecurityManagerService
     * 
     * @return Bean retrieved from Spring ApplicationContext
     */
    public static SecurityManagerService getSecurityManagerService() {
        return (SecurityManagerService) getApplicationContext().getBean(
                "securitymanager");
    }
    
    /**
     * Returns reference to PageFactoryService
     * 
     * @return Bean retrieved from Spring ApplicationContext
     */
    public static PageFactoryService getPageFactoryService() {
        return (PageFactoryService) getApplicationContext().getBean(
                "pagefactory");
    }
}
