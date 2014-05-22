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
package de.schlund.pfixcore.example;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.RequestParam;
import de.schlund.pfixxml.Variant;

/**
 * Describe class SampleStartIC here.
 *
 *
 * Created: Fri Apr  8 12:53:30 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class SampleStartIC implements ContextInterceptor {
    // Implementation of de.schlund.pfixcore.workflow.ContextInterceptor
    /**
     * Describe <code>process</code> method here.
     *
     * @param context a <code>Context</code> value
     * @param pfixServletRequest a <code>PfixServletRequest</code> value
     */
    public final void process(final Context context, final PfixServletRequest pfixServletRequest) {
        RequestParam param = pfixServletRequest.getRequestParam("SETVAR");
        if (param != null && param.getValue() != null && !param.getValue().equals("")) {
            context.setVariant(new Variant(param.getValue()));
        }
    }
}
