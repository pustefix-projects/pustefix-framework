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

package de.schlund.pfixxml.targets;

import java.io.File;

import javax.xml.transform.TransformerException;

/**
 * XSLLeafTarget.java
 *
 *
 * Created: Mon Jul 23 21:53:06 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class XSLLeafTarget extends LeafTarget {

    public XSLLeafTarget(TargetType type, TargetGenerator gen, String key) throws Exception {
        this.type       = type;
        this.generator  = gen;
        this.targetkey  = key;
        this.sharedleaf = SharedLeafFactory.getInstance().getSharedLeaf(gen.getDocroot() + key);
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getValueFromDiscCache()
     */
    protected Object getValueFromDiscCache() throws TransformerException {
        File thefile = new File(getTargetGenerator().getDocroot() + getTargetKey());
        if (thefile.exists() && thefile.isFile()) {
            PustefixXSLTProcessor xsltproc = TraxXSLTProcessor.getInstance();
            Object                retval   = xsltproc.xslObjectFromDisc(generator.getDocroot(), thefile.getPath()); 
            return retval;
        } else {
            return null;
        }
    }

}// XSLLeafTarget
