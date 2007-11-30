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

package de.schlund.pfixcore.editor2.frontend.resources;

import java.util.HashMap;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.ResultDocument;

public class SpringHelperResourceImpl implements SpringHelperResource {
    private HashMap values;
    
    public Object get(String key) {
        synchronized (this.values) {
            return this.values.get(key);
        }
    }

    public void set(String key, Object value) {
        synchronized (this.values) {
            this.values.put(key, value);
        }
    }

    public void unset(String key) {
        synchronized (this.values) {
            this.values.remove(key);
        }
    }

    public void init(Context context) throws Exception {
        this.values = new HashMap();
    }

    public void insertStatus(ResultDocument resdoc, Element elem) throws Exception {
        // Do nothing
    }

    public void reset() throws Exception {
        this.values.clear();
    }

}
