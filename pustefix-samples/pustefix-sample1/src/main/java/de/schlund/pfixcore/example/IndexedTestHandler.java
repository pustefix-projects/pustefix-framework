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

import java.util.HashMap;
import java.util.Iterator;

import org.pustefixframework.web.mvc.InputHandler;
import org.springframework.beans.factory.annotation.Autowired;

import de.schlund.pfixcore.example.iwrapper.IndexedTest;

/**
 * Describe class IndexedTestHandler here.
 *
 *
 * Created: Mon Jul 11 14:01:39 2005
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 */
public class IndexedTestHandler implements InputHandler<IndexedTest> {

    private ContextAdultInfo cai;
    
    public final void handleSubmittedData(IndexedTest itest) {
        String[]         keys  = itest.getKeysValue();

        HashMap<String, String> inmap = new HashMap<String, String>();
        for (int i = 0; i < keys.length ; i++) {
            inmap.put(keys[i], itest.getValue(keys[i]));
        }
        cai.setIndexedTest(inmap);
    }

    public final void retrieveCurrentStatus(IndexedTest itest) {
        HashMap<String, String> outmap = cai.getIndexedTest();
        
        for (Iterator<String> i = outmap.keySet().iterator(); i.hasNext(); ) {
            String key = i.next();
            if (outmap.get(key) != null) {
                itest.setValue(outmap.get(key), key);
            } else {
                itest.setValue("", key);
            }

        }
    }

    public final boolean prerequisitesMet() {
        return true;
    }

    public final boolean isActive() {
        return true;
    }

    public final boolean needsData() {
        return false;
    }

    @Autowired
    public void setContextAdultInfo(ContextAdultInfo cai) {
        this.cai = cai;
    }

}
