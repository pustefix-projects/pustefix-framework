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

package org.pustefixframework.http;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.pustefixframework.http.internal.FactoryInitWorker;

/**
 * Takes care of initializing factories and logging when the servlet context
 * is started.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class FactoryInitServletContextListener implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent event) {
        // No actions are performed on shutdown
    }

    public void contextInitialized(ServletContextEvent event) {
        try {
            FactoryInitWorker.init(event.getServletContext());
        } catch (ServletException e) {
            // Ignore the exception - cause will be saved and presented
            // on first request
        }
    }

}
