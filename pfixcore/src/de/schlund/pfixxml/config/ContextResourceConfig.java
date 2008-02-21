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

package de.schlund.pfixxml.config;

import java.util.Properties;
import java.util.Set;

import de.schlund.pfixcore.workflow.ContextResource;

public interface ContextResourceConfig {

    /**
     * Returns class context resource instance is created from.
     * 
     * @return class of context resource
     */
    Class<? extends ContextResource> getContextResourceClass();

    /**
     * Returns all Interfaces the context resource is implementing and should
     * be used for.
     * 
     * @return list of interfaces
     */
    Set<Class<? extends ContextResource>> getInterfaces();

    /**
     * Returns configuration paramters for the context resource.
     * 
     * @return configuration parameters
     */
    Properties getProperties();

}