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

package de.schlund.pfixxml;

import net.sf.saxon.expr.XPathContext;
import de.schlund.pfixxml.util.XsltContext;
import de.schlund.pfixxml.util.xsltimpl.XsltContextSaxon2;

/**
 * @author mleidig@schlund.de
 */
public class ImageThemedSrcSaxon2 {

    public static String getSrc(XPathContext context,String src,String themed_path,String themed_img,
            String parent_part_in,String parent_product_in,String targetGen,String targetKey,String module,String search) throws Exception {
        XsltContext xsltContext=new XsltContextSaxon2(context);
        return ImageThemedSrc.getSrc(xsltContext,src,themed_path,themed_img,parent_part_in,
                parent_product_in,targetGen,targetKey,module,search);
    }
    
}
