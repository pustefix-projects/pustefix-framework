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

import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.util.*;

import java.io.File;
import java.util.TreeMap;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

/**
 * XSLVirtualTarget.java
 *
 * Created: Mon Jul 23 21:53:06 2001
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 *
 */

public class XSLVirtualTarget extends VirtualTarget {

    public XSLVirtualTarget(TargetType type, TargetGenerator gen, String key,
            Themes themes) throws Exception {
        this.type = type;
        this.generator = gen;
        this.targetkey = key;
        this.themes = themes;
        this.params = new TreeMap();
        this.auxdepmanager = new AuxDependencyManager(this);
        auxdepmanager.tryInitAuxdepend();
    }

    /**
     * @see de.schlund.pfixxml.targets.TargetImpl#getValueFromDiscCache()
     */
    protected Object getValueFromDiscCache() throws TransformerException {
        Path thepath = PathFactory.getInstance().createPath(
                getTargetGenerator().getDisccachedir().getRelative()
                        + File.separator + getTargetKey());
        File thefile = thepath.resolve();
        if (thefile.exists() && thefile.isFile()) {
            // reset the target dependency list as they will be set up again
            this.clearTargetDependencies();

            return Xslt.loadTemplates(thepath, this);
        } else {
            return null;
        }
    }

    public Document getDOM() throws TargetGenerationException {
        // Make sure we have an up-to-date version
        this.getValue();
        
        File thefile = new File(getTargetGenerator().getDisccachedir()
                .resolve(), getTargetKey());
        if (thefile.exists() && thefile.isFile()) {
            try {
                return Xml.parse(thefile);
            } catch (TransformerException e) {
                throw new TargetGenerationException(
                        "Error while reading DOM from disccache for target "
                                + getTargetKey(), e);
            }
        } else {
            return null;
        }
    }

} // XSLVirtualTarget
