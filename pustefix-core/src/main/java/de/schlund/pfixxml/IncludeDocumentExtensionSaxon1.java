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

import org.pustefixframework.xmlgenerator.targets.TargetGenerator;

import com.icl.saxon.Context;

import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon1;

/**
 * @author mleidig@schlund.de
 */
public class IncludeDocumentExtensionSaxon1 {
    
    public static Object get(Context context,String path_str,String part,TargetGenerator targetGen,String targetkey,
            String parent_part_in,String parent_theme_in,String computed_inc,String module,String search) throws Exception {    
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.get(xsltContext,path_str,part,targetGen,targetkey,
                parent_part_in,parent_theme_in,computed_inc,module,search);
    }
    
    public static String getSystemId(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.getSystemId(xsltContext);
    }
    
    public static final String getRelativePathFromSystemId(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.getRelativePathFromSystemId(xsltContext);
    }
    
    public static boolean isIncludeDocument(Context context) {
        XsltContext xsltContext=new XsltContextSaxon1(context);
        return IncludeDocumentExtension.isIncludeDocument(xsltContext);
    }
    
    public static String getResolvedURI() {
        return IncludeDocumentExtension.getResolvedURI();
    }

}
