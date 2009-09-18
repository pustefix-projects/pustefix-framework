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

import de.schlund.pfixcore.workflow.Context;

/**
 * Simple wrapper around {@link ThreadLocal}, that stores a {@link Context}
 * instance for the current thread.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextStore {

    private static ThreadLocal<Context> tlContext = new ThreadLocal<Context>();

    /**
     * Stores the context for the current thread.
     * 
     * @param context context instance which shall be stored or 
     *  <code>null</code> to delete the current reference
     */
    public static void setContextForCurrentThread(Context context) {
        tlContext.set(context);
    }

    /**
     * Returns the context that has been stored for the current thread.
     * 
     * @return context instance or <code>null</code> if no context has been
     *  stored for the current thread
     */
    public static Context getContextForCurrentThread() {
        return tlContext.get();
    }

}
