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
import java.util.HashMap;

import org.apache.log4j.Category;

import de.schlund.pfixxml.XMLException;

/**
 *
 *
 */

public class TargetGeneratorFactory {
    private static TargetGeneratorFactory instance     = new TargetGeneratorFactory();
    private static HashMap                generatormap = new HashMap();
    private static Category               CAT          = Category.getInstance(TargetGeneratorFactory.class.getName());
    
    public static TargetGeneratorFactory getInstance() {
        return instance;
    }

    public synchronized TargetGenerator createGenerator(File cfile) throws Exception {
        if (cfile.exists() && cfile.isFile() && cfile.canRead()) {
            String key = genKey(cfile);
            TargetGenerator generator = (TargetGenerator) generatormap.get(key);
            if (generator == null) {
                CAT.debug("-- Init TargetGenerator --");
                generator = new TargetGenerator(cfile);
                generatormap.put(key, generator);
            } else {
                generator.tryReinit();
            }
            return generator;
        } else {
            throw (new XMLException("\nConfigfile '" + cfile.getPath() +
                                    "' isn't a file, can't be read or doesn't exist"));
        }
    }

    public void remove(File genfile) {
        generatormap.remove(genKey(genfile));
    }

    private String genKey(File genfile) {
        return genfile.getAbsolutePath();
    }
}
