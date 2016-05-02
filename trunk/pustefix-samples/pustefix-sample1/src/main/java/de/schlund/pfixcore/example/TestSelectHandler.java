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

import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.example.iwrapper.TestSelect;

/**
 * Describe class TestSelectHandler here.
 *
 *
 * Created: Tue Jun 13 12:34:23 2006
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class TestSelectHandler implements InputHandler<TestSelect> {

    @Autowired
    private ContextSimpleData csd;
    
    public final void handleSubmittedData(TestSelect select) {
        csd.reset();
        if (select.getDo_B()) {
            csd.setValue("call_b", "true");
        }
    }

    public final void retrieveCurrentStatus(TestSelect select) {
        // nothing
    }

    public final boolean needsData() {
        return false;
    }

    public final boolean prerequisitesMet() {
        return true;
    }

    public final boolean isActive() {
        return true;
    }

}
