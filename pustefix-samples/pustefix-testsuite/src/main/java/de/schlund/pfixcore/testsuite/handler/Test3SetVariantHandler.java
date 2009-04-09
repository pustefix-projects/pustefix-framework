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

package de.schlund.pfixcore.testsuite.handler;

import de.schlund.pfixcore.generator.IHandler;
import de.schlund.pfixcore.generator.IWrapper;
import de.schlund.pfixcore.testsuite.wrapper.Test3SetVariant;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.Variant;

public class Test3SetVariantHandler implements IHandler {

    public void handleSubmittedData(Context context, IWrapper wrapper) throws Exception {
        Test3SetVariant input = (Test3SetVariant) wrapper;
        if (input.getName().equals("default")) {
            context.setVariant(null);
        } else {
            context.setVariant(new Variant(input.getName()));
        }
    }

    public void retrieveCurrentStatus(Context context, IWrapper wrapper) throws Exception {
        // Do nothing here
    }

    public boolean prerequisitesMet(Context context) throws Exception {
        return true;
    }

    public boolean isActive(Context context) throws Exception {
        return true;
    }

    public boolean needsData(Context context) throws Exception {
        return false;
    }

}
