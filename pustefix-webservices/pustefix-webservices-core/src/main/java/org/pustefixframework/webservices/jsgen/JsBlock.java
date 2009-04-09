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
package org.pustefixframework.webservices.jsgen;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Javascript code block representation.
 * 
 * @author mleidig@schlund.de
 */
public class JsBlock {

    JsStatement[] statements;

    public JsBlock() {
        statements = new JsStatement[0];
    }

    public void addStatement(JsStatement statement) {
        JsStatement[] upd = new JsStatement[statements.length + 1];
        for (int i = 0; i < statements.length; i++)
            upd[i] = statements[i];
        upd[upd.length - 1] = statement;
        statements = upd;
    }

    public JsStatement[] getStatements() {
        return statements;
    }

    public void printCode(String indent, OutputStream out) throws IOException {
        for (int i = 0; i < statements.length; i++) {
            statements[i].printCode(indent, out);
        }
    }

}
