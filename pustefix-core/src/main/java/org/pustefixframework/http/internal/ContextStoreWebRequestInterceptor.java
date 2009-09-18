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

package org.pustefixframework.http.internal;

import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import de.schlund.pfixcore.workflow.Context;

/**
 * Web request interceptor that takes care of storing a reference to
 * the {@link Context} instance for each incoming request in a thread local.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextStoreWebRequestInterceptor implements WebRequestInterceptor {

    private Context context;

    public void afterCompletion(WebRequest request, Exception ex) throws Exception {
        ContextStore.setContextForCurrentThread(null);
    }

    public void postHandle(WebRequest request, ModelMap model) throws Exception {
        // Do nothing
    }

    public void preHandle(WebRequest request) throws Exception {
        ContextStore.setContextForCurrentThread(context);
    }

    /**
     * Sets the context object that is stored in the thread local for 
     * each request.
     * 
     * @param context context object being stored in the thread local
     */
    public void setContext(Context context) {
        this.context = context;
    }
}
