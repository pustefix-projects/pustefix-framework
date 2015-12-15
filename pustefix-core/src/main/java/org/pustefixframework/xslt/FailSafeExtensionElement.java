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

package org.pustefixframework.xslt;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.icl.saxon.Context;
import com.icl.saxon.style.StyleElement;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;

/**
 * XSL extension element which catches exceptions thrown
 * during the processing of its child elements and writes
 * out the exception's stacktrace to the result document.
 *
 */
public class FailSafeExtensionElement extends StyleElement {

    public boolean mayContainTemplateBody() {
        return true;
    }

    @Override
    public void prepareAttributes() throws TransformerConfigurationException {
    }

    @Override
    public void process(Context context) throws TransformerException {

        try {
            processChildren(context);
        } catch(TransformerException x) {
            //check if exception was thrown by an extension function to get the original
            //stacktrace instead of the cut off one from the wrapping TransformerException
            Throwable throwable = ExtensionFunctionUtils.getExtensionFunctionError();
            if(throwable == null) {
                throwable = x;
            } else {
                ExtensionFunctionUtils.resetExtensionFunctionError();
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.close();
            context.getOutputter().write(sw.toString());    
        }
    }

}
