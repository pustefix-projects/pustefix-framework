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
package org.pustefixframework.tutorial.caster;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.tutorial.StatusCodeLib;

import de.schlund.pfixcore.generator.IWrapperParamCaster;
import de.schlund.pfixcore.generator.SimpleCheck;
import de.schlund.pfixxml.RequestParam;
import de.schlund.util.statuscodes.StatusCode;

public class ToURL extends SimpleCheck implements IWrapperParamCaster {

    private URL[] value = null;
    private StatusCode scode;
    
    public ToURL() {
        scode = StatusCodeLib.CASTER_URL_INVALID;
    }
    
    public void castValue(RequestParam[] requestParams) {
        List<URL> out = new ArrayList<URL>();
        URL url;
        for (RequestParam param : requestParams) {
            try {
                url = new URL(param.getValue());
                out.add(url);
            } catch(MalformedURLException ex) {
                addSCode(scode);
            }
        }
        if (!errorHappened()) {
            value = out.toArray(new URL[] {});
        }
    }

    public Object[] getValue() {
        return value;
    }
}
