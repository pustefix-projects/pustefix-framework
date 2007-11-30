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
 *
 */
package de.schlund.pfixcore.workflow.app;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.config.PageRequestConfig;


/**
 * All classes which want to act as a container for classes
 * which implement the {@link IHandler} interface must
 * implement this interface. A IHandlerContainer contains
 * all IHandler belonging to a single page and it is shared
 * between sessions.
 */
public interface IHandlerContainer {

    //~ Methods ....................................................................................

    /**
     * Initialize all IHandlers in this container.
     * @param config Configuration for pagerequest
     */
    void initIHandlers(PageRequestConfig config);

    /**
     * Determine if the requested page is accesible.
     * @param context the context identifying the current page
     * @return true if page is accesible, else false
     * @throws Exception on errors
     */
    boolean isPageAccessible(Context context) throws Exception;

    /**
     * Determine if the IHandler in this container are active.
     * @param context the context identifying the current page
     * @return true if handler are active, else false
     * @throws Exception on errors
     * 
     */
    boolean areHandlerActive(Context context) throws Exception;

    /**
     * Determine if any of the IHandler in this container needs data.
     * @param context the context identifying the current page
     * @return true if handler need data, else false
     * @throws Exception on errors 
     */
    boolean needsData(Context context) throws Exception;
}