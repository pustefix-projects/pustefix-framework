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

package de.schlund.pfixxml;

import net.sf.saxon.expr.XPathContext;

import org.w3c.dom.Node;

import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon2;

/**
 * @author mleidig@schlund.de
 */
public class IncludeDocumentExtensionSaxon2 {

    public static Node get(XPathContext context,String path_str,String part,TargetGenerator targetgen,String targetkey,
            String parent_part_in,String parent_theme_in,String computed_inc,String module,String search,
            String tenant, String language) throws Exception {    
        try {
            XsltContext xsltContext=new XsltContextSaxon2(context);
            return (Node)IncludeDocumentExtension.get(xsltContext,path_str,part,targetgen,targetkey,
                    parent_part_in,parent_theme_in,computed_inc,module,search, tenant, language);
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static boolean exists(XPathContext context,String path_str,String part,TargetGenerator targetgen,String targetkey,
            String module,String search, String tenant, String language) throws Exception {    
        try {
            XsltContext xsltContext=new XsltContextSaxon2(context);
            return IncludeDocumentExtension.exists(xsltContext,path_str,part,targetgen,targetkey,
                                                   module,search, tenant, language);
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }
    
    public static String getSystemId(XPathContext context) {
        XsltContext xsltContext=new XsltContextSaxon2(context);
        return IncludeDocumentExtension.getSystemId(xsltContext);
    }
    
    public static String getRelativePathFromSystemId(XPathContext context) {
        XsltContext xsltContext=new XsltContextSaxon2(context);
        return IncludeDocumentExtension.getRelativePathFromSystemId(xsltContext);
    }
    
    public static final String getModuleFromSystemId(XPathContext context) {
        XsltContext xsltContext = new XsltContextSaxon2(context);
        return IncludeDocumentExtension.getModuleFromSystemId(xsltContext);
    }
    
    public static boolean isIncludeDocument(XPathContext context) {
        XsltContext xsltContext=new XsltContextSaxon2(context);
        return IncludeDocumentExtension.isIncludeDocument(xsltContext);
    }
    
    public static String getResolvedURI() {
        return IncludeDocumentExtension.getResolvedURI();
    }
    
    public static String getDynIncInfo(String part, String theme, String path, String resolvedModule, String requestedModule, String tenant, String language) {
        return IncludeDocumentExtension.getDynIncInfo(part, theme, path, resolvedModule, requestedModule, tenant, language);
    }
    
}
