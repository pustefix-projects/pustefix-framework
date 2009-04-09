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
 *
 */

package de.schlund.pfixcore.example.test;

import java.util.ArrayList;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;

/**
 * @author mleidig
 */
public class DataCaster extends SimpleCheck implements IWrapperParamCaster {

    private Data[] value = null;

    public DataCaster() {
    }

    public Object[] getValue() {
        return value;
    }

    public void castValue(RequestParam[] param) {
        reset();
        ArrayList<Data> out = new ArrayList<Data>();
        for (int i = 0; i < param.length; i++) {
            String text = param[i].getValue();
            Data data = new Data();
            data.setText(text);
            out.add(data);
        }
        if (!errorHappened()) {
            value = (Data[]) out.toArray(new Data[] {});
        }
    }

}
