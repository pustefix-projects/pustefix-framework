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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import de.schlund.pfixcore.workflow.Context;

/**
 * Stores references to all {@link de.schlund.pfixcore.workflow.Context}
 * objects used by the editor. This is used too retrieve a list of all
 * include parts being edited by anyone. The Context objects are internally
 * stored as keys in a WeakHashMap to make sure, they can be garbage
 * collected if the are not used by any other part of Pustefix any longer.
 * This will usually happen, when the HttpSession the context is stored in
 * expires.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class ContextStore {
    private static ContextStore instance = new ContextStore();

    /**
     * Returns single instance (singleton pattern)
     * 
     * @return instance of ContextStore
     */
    public static ContextStore getInstance() {
        return instance;
    }

    private WeakHashMap<Context, String> contextmap = new WeakHashMap<Context, String>();

    /**
     * Returns a Map containing the known Context objects as keys and the
     * name of the editor users using the corresponding Context as the value.
     * This information can be used to construct a list of all active
     * editor sessions.
     * 
     * @return Map containing Context objects of editor sessions and usernames
     */    
    public synchronized Map<Context, String> getContextMap() {
        return new HashMap<Context, String>(contextmap);
    }

    /**
     * Registers a Context with the specified username.
     * This method should be triggered during user login.
     * 
     * @param ctx editor's session context
     * @param username name of the user this context is used by 
     */
    public synchronized void registerContext(Context ctx, String username) {
        contextmap.put(ctx, username);
    }

    /**
     * Unregisters the specified context.
     * This method should be triggered on a logout action to make sure
     * the unused session is not registered any more.
     * 
     * @param ctx the context that should be removed from the map
     */
    public synchronized void unregisterContext(Context ctx) {
        contextmap.remove(ctx);
    }
}
