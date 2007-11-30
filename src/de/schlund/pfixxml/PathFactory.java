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

import java.io.File;

import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Path;

/**
 * Describe class PathFactory here.
 *
 *
 * Created: Tue Jun 29 18:04:32 2004
 *
 * @author <a href="mailto:jtl@schlund.de">Jens Lautenbacher</a>
 * @version 1.0
 * @deprecated Use {@link ResourceUtil} instead 
 */
public class PathFactory {
    private static PathFactory instance = new PathFactory(); 
    private File docroot;
    
    private PathFactory() {}

    public static PathFactory getInstance() {
        return instance;
    }

    public Path createPath(String relative) {
        if (relative.startsWith(File.separator)) {
            throw new IllegalArgumentException("**** Need a relative path: " + relative);
        }
        return Path.create(docroot, relative);
    }

    public String makePathStringRelative(String absolute) {
        if (!absolute.startsWith(docroot.getPath() + File.separator)) {
            throw new IllegalArgumentException("**** Absolute path " + absolute +
                                               " can't be made relative to docroot " + docroot.getPath());
        }
        return absolute.substring((docroot.getPath() + File.separator).length());
    }

    public void init(String docrootstr) {
        docroot = new File(docrootstr);
        if (!docroot.isDirectory()) {
            throw new RuntimeException("**** docroot " + docrootstr + " must be a directory! ****");
        }
        if (!docroot.isAbsolute()) {
            throw new RuntimeException("**** docroot " + docrootstr + " must be a absolute! ****");
        }
        System.out.println("Docroot is: " + docroot.getPath());
        
        // Try to initialize the new resolution system for compatibility reasons
        // - old code might only know PathFactory.
        try {
            GlobalConfigurator.setDocroot(docrootstr);
        } catch (IllegalStateException e) {
            // Ignore exception - this is absolutely okay, as this
            // configuration might already have been performed by other code
        }
    }

}
