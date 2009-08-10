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

package org.pustefixframework.xmlgenerator.targets;



import java.io.IOException;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;

import org.pustefixframework.resource.FileResource;
import org.pustefixframework.resource.InputStreamResource;
import org.pustefixframework.resource.Resource;
import org.w3c.dom.Document;

import de.schlund.pfixxml.util.ResourceUtils;
import de.schlund.pfixxml.util.Xml;
import de.schlund.pfixxml.util.Xslt;

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

    public XSLLeafTarget(TargetType type, TargetGenerator gen, Resource targetRes, FileResource targetAuxRes, String key, Themes themes) throws Exception {
    	if(!(targetRes instanceof InputStreamResource)) throw new IllegalArgumentException("Expected InputStreamResource");
    	this.type      = type;
        this.generator = gen;
        this.targetkey = key;
        this.targetRes = targetRes;
        this.targetAuxRes = targetAuxRes;
        this.themes    = themes;
        this.sharedleaf = gen.getSharedLeaf(generator.getXsltVersion(), targetRes);
        this.auxdepmanager = new AuxDependencyManager(this);
        this.auxdepmanager.tryInitAuxdepend();
    }

    /**
     * @see org.pustefixframework.xmlgenerator.targets.TargetImpl#getValueFromDiscCache()
     */
    @Override
    protected Object getValueFromDiscCache() throws TransformerException {
        if (ResourceUtils.exists(targetRes)) {
            // reset the target dependency list as they will be set up again
            this.getAuxDependencyManager().reset();
            
            Templates tmpl = Xslt.loadTemplates(generator.getXsltVersion(), (InputStreamResource)targetRes, this);
            
            // save aux dependencies
            try {

                this.getAuxDependencyManager().saveAuxdepend();
            } catch (IOException e) {
                throw new TransformerException("Error while writing auxdependency information");
            }
            
            return tmpl;
        } else {
            return null;
        }
    }

    public Document getDOM() throws TargetGenerationException {
        if (ResourceUtils.exists(targetRes)) {
            try {
                return Xml.parse(generator.getXsltVersion(), (InputStreamResource)targetRes);
            } catch (TransformerException e) {
                throw new TargetGenerationException("Error while reading DOM from disccache for target "
                                                    + getTargetKey(), e);
            }
        } else {
            return null;
        }
    }
    
}// XSLLeafTarget
