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
package org.pustefixframework.web.mvc;

import de.schlund.pfixcore.generator.IWrapper;

/**
 * MVC controller interface to be implemented by applications for processing 
 * of request/form data and participation in page accessibility and page flow checks.  
 *
 * Successor of the {@link de.schlund.pfixcore.generator.IHandler} interface.
 * 
 * Should be preferred because its generic (you don't have to cast the IWrapper objects)
 * and the methods don't declare to throw {@link Exception} (which enforces sensible 
 * handling of exceptions thrown in the application code).
 * 
 */
public interface InputHandler<T extends IWrapper> {

    /**
     * This method is the general entry point for calling the application
     * logic and updating the data model. 
     * 
     * It handles submitted request data, passed in as a wrapper object.
     * The method is only called if validating the request data and 
     * binding it to the wrapper object was successful.
     */
    public void handleSubmittedData(T wrapper);
    
    /**
     * This method is called to populate wrapper objects with data,
     * e.g. to fill in connected form fields on the view layer.
     * 
     * The method is only called when a handler is active and the
     * current request is no submit or the according wrapper is 
     * explicitly selected using according command parameters or 
     * configuration settings.
     */
    public void retrieveCurrentStatus(T wrapper);
    
    /**
     * Returns if all requirements are fulfilled for the handler to
     * work and take part in the request data and page flow processing.
     * 
     * Returning false means that a page referencing this handler can't
     * be accessed or displayed.
     */
    public boolean prerequisitesMet();
    
    /**
     * Returns if the handler is enabled and should take part in the
     * request data and page flow processing.
     * 
     * If all handlers on a page return false, the page itself won't be
     * accessible by default (can be overridden in configuration using
     * the input policy attribute).
     */
    public boolean isActive();
    
    /**
     * Returns if further data is needed by this handler in order to
     * satisfy the requirements of the backing data model.
     * 
     * This method is called within a page flow process, to detect if
     * the page containing this handler requires data and the page flow
     * process should stop and display the page.
     */
    public boolean needsData();

}
