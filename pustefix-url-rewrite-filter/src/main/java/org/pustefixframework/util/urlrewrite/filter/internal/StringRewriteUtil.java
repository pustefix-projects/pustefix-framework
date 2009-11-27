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

package org.pustefixframework.util.urlrewrite.filter.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.pustefixframework.util.urlrewrite.io.ByteNode;
import org.pustefixframework.util.urlrewrite.io.ByteSequenceReplacingOutputStream;

/**
 * Wrapper around {@link ByteSequenceReplacingOutputStream} for rewriting
 * strings.   
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public class StringRewriteUtil {

    private ByteArrayOutputStream baos;

    private ByteSequenceReplacingOutputStream bsros;

    public StringRewriteUtil(ByteNode<byte[]> replacementTree) {
        baos = new ByteArrayOutputStream();
        bsros = new ByteSequenceReplacingOutputStream(baos, replacementTree);
    }

    public String rewriteString(String string) {
        try {
            byte[] bytes = string.getBytes("UTF-8");
            try {
                bsros.write(bytes);
                bsros.flush();
            } catch (IOException e) {
                throw new RuntimeException("Unexpected IOException", e);
            }
            string = baos.toString("UTF-8");
            baos.reset();
            return string;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not use encoding UTF-8", e);
        }
    }
}
