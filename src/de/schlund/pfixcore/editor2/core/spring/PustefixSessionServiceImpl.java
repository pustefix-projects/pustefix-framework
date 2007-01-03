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

package de.schlund.pfixcore.editor2.core.spring;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;

import de.schlund.pfixcore.editor2.frontend.resources.SpringHelperResource;
import de.schlund.pfixcore.workflow.Context;

/**
 * Implementation of
 * {@link de.schlund.pfixcore.editor2.core.spring.SessionService} using a
 * Pustefix context resource to store data. This class also implements
 * {@link de.schlund.pfixcore.editor2.core.spring.PustefixContextService} to
 * retrieve the current context for each request.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.frontend.util.SpringHelperStartContextInterceptor
 */
public class PustefixSessionServiceImpl implements SessionService,
        PustefixContextService {
    ThreadLocal<WeakReference<Context>> context = new ThreadLocal<WeakReference<Context>>();

    private SpringHelperResource getHelperResource() {
        if (this.getPustefixContext() == null) {
            String err = "Pustefix context is not set, though it should be. Are you sure the code was called within a Pustefix request?";
            Logger.getLogger(this.getClass()).error(err);
            throw new RuntimeException(err);
        }
        return (SpringHelperResource) this.getPustefixContext()
                .getContextResourceManager().getResource(
                        SpringHelperResource.class.getName());
    }

    public Object get(String key) {
        return this.getHelperResource().get(key);
    }

    public void set(String key, Object value) {
        this.getHelperResource().set(key, value);
    }

    public void unset(String key) {
        this.getHelperResource().unset(key);
    }

    public void setPustefixContext(Context context) {
        // Store the context in a thread local variable
        // Use a weak-reference as the context object should
        // be removed when the session is deleted - even
        // if it is still attached to a thread.
        if (context == null) {
            this.context.set(null);
        } else {
            WeakReference<Context> wr = new WeakReference<Context>(context);
            this.context.set(wr);
        }
    }

    public Context getPustefixContext() {
        try {
            return this.context.get().get();
        } catch (NullPointerException e) {
            return null;
        }
    }

}
