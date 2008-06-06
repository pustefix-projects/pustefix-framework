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

import de.schlund.pfixcore.editor2.core.spring.PustefixContextService;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextInterceptor;
import de.schlund.pfixxml.PfixServletRequest;

/**
 * This interceptor is used to supply the current context to the spring 
 * bean which needs it.
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 * @see de.schlund.pfixcore.editor2.frontend.util.SpringHelperEndContextInterceptor
 */
public class SpringHelperStartContextInterceptor implements ContextInterceptor {

    public void process(Context context, PfixServletRequest preq) {
        PustefixContextService service = (PustefixContextService) EditorApplicationContextFactory
                .getInstance().getApplicationContext().getBean("session");
        service.setPustefixContext(context);
    }

}
