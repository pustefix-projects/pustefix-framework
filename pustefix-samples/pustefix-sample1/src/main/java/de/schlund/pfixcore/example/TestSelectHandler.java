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

import de.schlund.pfixcore.example.iwrapper.TestSelect;
import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.workflow.Context;

/**
 * Describe class TestSelectHandler here.
 *
 *
 * Created: Tue Jun 13 12:34:23 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TestSelectHandler implements IHandler {

    public final void handleSubmittedData(final Context context, final IWrapper wrapper) throws Exception {
        ContextSimpleData csd = (ContextSimpleData) context.getContextResourceManager().
            getResource("de.schlund.pfixcore.example.ContextSimpleData");
        TestSelect tsw = (TestSelect) wrapper;

        csd.reset();
        
        if (tsw.getDo_B()) {
            csd.setValue("call_b", "true");
        }

    }

    public final void retrieveCurrentStatus(final Context context, final IWrapper IWrapper) throws Exception {
        // nothing
    }

    public final boolean needsData(final Context context) throws Exception {
        return false;
    }

    public final boolean prerequisitesMet(final Context context) throws Exception {
        return true;
    }

    public final boolean isActive(final Context context) throws Exception {
        return true;
    }

}
