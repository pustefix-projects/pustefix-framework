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

/**
 * Provides configuration for an {@link de.schlund.pfixxml.AbstractXMLServer} instance or one of its
 * child classes.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface AbstractXMLServletConfig extends ServletManagerConfig {

    /**
     * Returns name for the servlet instance. This name is used to build attribute
     * names which are used to store data within the servlet context and servlet
     * session. This name has to be unique within the servlet context.
     * 
     * @return Name for the servlet instance
     */
    String getServletName();

    /**
     * Returns the path to the configuration file for the {@link de.schlund.pfixxml.targets.TargetGenerator} 
     * used by the {@link de.schlund.pfixxml.AbstractXMLServer}. The path has to be specified relative
     * to the Pustefix docroot.
     * 
     * @return path to target generator configuration file
     */
    String getDependFile();

    /**
     * If true the {@link de.schlund.pfixxml.AbstractXMLServer} or its children allow the user to
     * switch to "editmode" which provides extra debugging information.
     * 
     * @return flag specifying whether to activate the edit mode 
     */
    boolean isEditMode();

}