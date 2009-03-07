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

package org.pustefixframework.editor.webui.handlers;

import org.pustefixframework.container.annotations.Inject;
import org.pustefixframework.editor.webui.resources.CommonIncludesResource;
import org.pustefixframework.editor.webui.resources.IncludesResource;

import de.schlund.pfixcore.workflow.Context;

/**
 * Handles include part restore from backup
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class RestoreIncludePartHandler extends CommonRestoreIncludePartHandler {

    private IncludesResource includesResource;

    protected CommonIncludesResource getResource(Context context) {
        return includesResource;
    }

    @Inject
    public void setIncludesResource(IncludesResource includesResource) {
        this.includesResource = includesResource;
    }

}
