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

package de.schlund.pfixxml.targets;


import javax.xml.transform.TransformerException;

import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.Resource;
import org.w3c.dom.Document;

import de.schlund.pfixxml.util.Xml;

/**
 * XMLLeafTarget.java
 *
 *
 * Created: Mon Jul 23 21:53:06 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 *
 */

public class XMLLeafTarget extends LeafTarget {

    public XMLLeafTarget(TargetType type, TargetGenerator gen, Resource targetRes, Resource targetAuxRes, String key, Themes themes) throws Exception {
    	if(!(targetRes instanceof InputStreamResource)) throw new IllegalArgumentException("Expected InputStreamResource");
    	this.type      = type;
        this.generator = gen;
        this.targetkey = key;
        this.targetRes = targetRes;
        this.targetAuxRes = targetAuxRes;
        this.themes    = themes;
        this.sharedleaf = SharedLeafFactory.getInstance().getSharedLeaf(generator.getXsltVersion(), targetRes);
        // Create empty manager to avoid null pointer exceptions
        this.auxdepmanager = new AuxDependencyManager(this);
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getValueFromDiscCache()
     */
    @Override
    protected Object getValueFromDiscCache() throws TransformerException {
        if (targetRes.exists()) {
            return Xml.parse(generator.getXsltVersion(), (InputStreamResource)targetRes);
        } else {
            return null;
        }
    }

    public Document getDOM() throws TargetGenerationException {
        return (Document) this.getValue();
    }

}// XMLLeafTarget
