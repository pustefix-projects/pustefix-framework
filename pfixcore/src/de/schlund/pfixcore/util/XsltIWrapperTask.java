/*
 * Created on Sep 18, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
        getTransformer().transform(in, out);
        getTransformer().clearParameters();
    }

}
