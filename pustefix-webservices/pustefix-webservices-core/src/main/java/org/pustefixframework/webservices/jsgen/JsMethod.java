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
package org.pustefixframework.webservices.jsgen;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Javascript method representation.
 * 
 * @author mleidig@schlund.de
 */
public class JsMethod {

    JsClass jsClass;
    String name;
    JsParam[] params;
    JsParam retParam;
    JsBlock body;

    public JsMethod(JsClass jsClass, String name) {
        this.jsClass = jsClass;
        this.name = name;
        params = new JsParam[0];
        body = new JsBlock();
    }

    public JsClass getJsClass() {
        return jsClass;
    }

    public String getName() {
        return name;
    }

    public void addParam(JsParam param) {
        JsParam[] upd = new JsParam[params.length + 1];
        for (int i = 0; i < params.length; i++)
            upd[i] = params[i];
        upd[upd.length - 1] = param;
        params = upd;
    }

    public JsParam[] getParams() {
        return params;
    }

    public String getParamList() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i].getName());
            if (i < params.length - 1) sb.append(",");
        }
        return sb.toString();
    }

    public JsBlock getBody() {
        return body;
    }

    public void printCode(String indent, OutputStream out) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append(getJsClass().getName() + ".prototype." + getName() + "=function(" + getParamList() + ") {\n");
        out.write(sb.toString().getBytes());
        getBody().printCode(indent, out);
        out.write("}\n".getBytes());
    }

}
