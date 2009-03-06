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
 *
 */
package de.schlund.pfixcore.util;

import java.io.File;

import org.apache.tools.ant.BuildException;

/**
 * @author adam
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class XsltIWrapperTask extends XsltGenericTask {

    protected String packagename;
    protected String classname;

    protected void doTransformation() throws BuildException {
        try {
            int sepPos = infilenameNoExt.lastIndexOf(File.separatorChar);
            if (sepPos > 0) {
                // qualified package
                packagename = infilenameNoExt.substring(0, sepPos);
                classname = infilenameNoExt.substring(sepPos+1);
            } else {
                // default package
                packagename = "";
                classname = infilenameNoExt;
            }
            packagename = packagename.replace(File.separatorChar,'.');
        } catch (IndexOutOfBoundsException ioobe) {
            // This should never happen
            throw new BuildException("Input filename \""+inname+"\" is of unexpected form",ioobe);
        }
        
        // For parameter names see example/core/build/iwrapper.xsl 
        // <xsl:param name="classname"/>
        // <xsl:param name="package"/>
        getTransformer().setParameter(new XsltParam("classname", classname));
        getTransformer().setParameter(new XsltParam("package", packagename));                
        try {
            getTransformer().transform(in, out);
        } finally {
            getTransformer().clearParameters();
        }
    }

}
