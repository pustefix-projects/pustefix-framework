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

import com.icl.saxon.Context;

import de.schlund.pfixxml.targets.TargetGenerator;
import de.schlund.pfixxml.util.ExtensionFunctionUtils;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon1;

/**
 * @author mleidig@schlund.de
 */
public class ImageThemedSrcSaxon1 {

    public static String getSrc(Context context,String src,String themed_path,String themed_img,
            String parent_part_in,String parent_product_in,TargetGenerator targetGen,String targetKey,
            String module,String search, String tenant, String language) throws Exception {
        try {
            XsltContext xsltContext=new XsltContextSaxon1(context);
            return ImageThemedSrc.getSrc(xsltContext,src,themed_path,themed_img,parent_part_in,
                    parent_product_in,targetGen,targetKey,module,search, tenant, language);
        } catch(Exception x) {
            ExtensionFunctionUtils.setExtensionFunctionError(x);
            throw x;
        }
    }

}
