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
 */
package de.schlund.pfixcore.workflow.app;

import org.pustefixframework.config.contextxmlservice.StateConfig;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;


/**
 * All classes which want to act as a container for classes
 * which implement the {@link IHandler} interface must
 * implement this interface. A IHandlerContainer contains
 * all IHandler belonging to a single page and it is shared
 * between sessions.
 */
public interface IHandlerContainer {

    /**
     * Initialize all IHandlers in this container.
     * @param config Configuration for pagerequest
     */
    void initIHandlers(StateConfig config);

    /**
     * Determine if the associated page is accessible.
     * @param context the context identifying the current page
     * @return true if page is accessible, else false
     * @throws Exception on errors
     */
    boolean isAccessible(Context context) throws Exception;
    
    /**
     * Determine if any of the IHandler in this container needs data.
     * @param context the context identifying the current page
     * @return true if handler need data, else false
     * @throws Exception on errors 
     */
    boolean needsData(Context context) throws Exception;
    
    /**
     * create a new IWrapperContainer instance that is used during handling of the actual request data.
     * This will set up a new copy of IWrapper objects and load them with the submitted values.
     * @return an fresh instance of IWrapperContainer
     */
    IWrapperContainer createIWrapperContainerInstance(Context context, PfixServletRequest preq, ResultDocument resdoc) throws Exception;

}